package net.flexberry.services.server.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.flexberry.services.edm.TypeParser;
import net.flexberry.services.server.edmprovider.OdataHibernateEdmProvider;
import net.flexberry.services.util.HibernateUtil;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.data.EntitySetImpl;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.core.edm.provider.EdmEntitySetImpl;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;



public class OdataHibernateDataProvider {
  private Map<String, Class<?>> mapEntitySet=new HashMap<String, Class<?>>();
  private List<String> classesNames;
  private String namespace;
  private ServiceMetadata serviceMetadata;
  private OdataHibernateEdmProvider edmProvider;

  public void setServiceMetadata(ServiceMetadata serviceMetadata){
    this.serviceMetadata=serviceMetadata;
  }

  public void setEdmProvider(OdataHibernateEdmProvider edmProvider){
    this.edmProvider=edmProvider;
  }

  public List<String> getClassesNames(){
    return classesNames;
  }

  public String getNamespace(){
    return namespace;
  }

  public EdmEntitySet getEdmEntitySet(Entity entity){
    org.apache.olingo.server.api.edm.provider.EntitySet entitySet=null;
    for (String cls : getClassesNames()) {
      String esName=getEntitySetName(cls);
      if (cls.equals(entity.getType())) {
        entitySet= new org.apache.olingo.server.api.edm.provider.EntitySet()
            .setName(esName)
            .setType(new FullQualifiedName(cls));
        break;
      }
    }
    EdmEntitySetImpl edmEntitySet = new EdmEntitySetImpl(serviceMetadata.getEdm(),
        serviceMetadata.getEdm().getEntityContainer(edmProvider.getContainerName()), entitySet);
    return edmEntitySet;
  }

  public org.apache.olingo.server.api.edm.provider.EntitySet getEntitySet(String entitySetName){
    for (String cls : getClassesNames()) {
      String esName=getEntitySetName(cls);
      if (esName.equals(entitySetName)) {
        return new org.apache.olingo.server.api.edm.provider.EntitySet()
            .setName(esName)
            .setType(new FullQualifiedName(cls))/*
            .setNavigationPropertyBindings(
                Arrays.asList(
                    new NavigationPropertyBinding().setPath("Manufacturer").setTarget(
                        new Target().setTargetName(ES_MANUFACTURER_NAME).setEntityContainer(CONTAINER_FQN)))) */;
      }

    }



    return null;
  }

  public String getEntitySetName0(String cls){
    if(namespace.length()==0) {
      return "EntitySet_"+cls;
    }
    return "EntitySet_"+cls.substring(namespace.length()+1);
  }

  public String getEntitySetName(String cls){
    return "EntitySet_"+classesNames.indexOf(cls);
  }

  private void init(List<Class<?>> classes) throws ClassNotFoundException, IOException, NoSuchMethodException,
  SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    if(classesNames.size()>0){
      int p=classesNames.get(0).lastIndexOf(".");
      if(p!=-1) {
        namespace=classesNames.get(0).substring(0,p);
      } else {
        namespace="";
      }
    }
    for (Class<?> clazz : classes) {
      mapEntitySet.put(getEntitySetName(clazz.getCanonicalName()), clazz);
    }
  }
  public OdataHibernateDataProvider(List<String> classesNames) throws ClassNotFoundException, IOException,
  NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    List<Class<?>> classes = new ArrayList<Class<?>>();
    this.classesNames=classesNames;
    for (String className : classesNames) {
      classes.add(Class.forName(className));
    }
    init(classes);
  }


  public OdataHibernateDataProvider(String javaPackage) throws ClassNotFoundException, IOException,
  NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    String packageDir = javaPackage.replaceAll("[.]", "/");
    List<Class<?>> classes = new ArrayList<Class<?>>();
    URL upackage = getClass().getClassLoader().getResource(packageDir);
    InputStream in = (InputStream) upackage.getContent();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String line = null;
    classesNames=new ArrayList<String>();
    while ((line = br.readLine()) != null) {
      if(line.endsWith(".class")){
        Class<?> cls=Class.forName( packageDir+"."+line.substring(0,line.lastIndexOf('.')));
        classesNames.add(cls.getCanonicalName());
        classes.add(cls);
      }
    }
    init(classes);
  }

  public EntitySet readAll(EdmEntitySet edmEntitySet) throws ClassNotFoundException, NoSuchMethodException,
  SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    String entitySetName=edmEntitySet.getName();
    Session session = null;
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      Class<?> clazz=mapEntitySet.get(entitySetName);
      TypeParser parser=new TypeParser(clazz.getCanonicalName());
      EntitySet entitySet = new EntitySetImpl();
      List<Entity> entities=entitySet.getEntities();
      @SuppressWarnings("rawtypes")
      List objs = session.createCriteria(clazz).list();
      for (Object obj : objs) {
        entities.add(parser.createEntity(obj));
      }
      return entitySet;

    }finally {
      if (session != null && session.isOpen()) {
          session.close();
      }
    }
  }

  private UriResourceKind getKind(UriInfoResource uriInfo){
    final int lastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 1;
    final UriResource lastPathSegment = uriInfo.getUriResourceParts().get(lastPathSegmentIndex);
    return lastPathSegment.getKind();
  }

  public UriInfo parseUri(String link) throws UriParserException{
    return new Parser().parseUri(link, null, null,serviceMetadata.getEdm());
  }

  public void create(Entity entity) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
  InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
  UriParserException, DataProviderException{
    TypeParser parser=new TypeParser(entity.getType());
    Object obj=parser.createObject();
    for (org.apache.olingo.commons.api.data.Property prop : entity.getProperties()) {
      parser.invokeSetMethod(obj,prop.getName(),prop.getValue());
    }
    Session session = null;
    try {
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        for (Link link : entity.getNavigationBindings()) {
          UriInfo uriInfo = parseUri(link.getBindingLink());
          final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
          @SuppressWarnings("rawtypes")
          List objs = findObjects(session,resourceEntitySet.getEntitySet().getEntityType(),
              resourceEntitySet.getKeyPredicates());
          if(objs.size()>0) {
            parser.invokeSetMethod(obj,link.getTitle(),objs.get(0));
          }
        }
        session.save(obj);
        session.getTransaction().commit();
    } finally {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

  }

  @SuppressWarnings("rawtypes")
  private List findObjects(Session session,final EdmEntityType entityType,final List<UriParameter> keys)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, DataProviderException{
    final TypeParser parser=new TypeParser(entityType.getFullQualifiedName());
    final Object obj=parser.createObject();
    Criteria criteria=session.createCriteria(obj.getClass());
    for (final UriParameter key : keys) {
      final EdmProperty property = (EdmProperty) entityType.getProperty(key.getName());
      final EdmPrimitiveType type = (EdmPrimitiveType) property.getType();
      final Class<?> returnType=parser.getColumnType(key.getName());
      try {
      final Object keyValue = type.valueOfString(type.fromUriLiteral(key.getText()),
        property.isNullable(), property.getMaxLength(), property.getPrecision(), property.getScale(),
        property.isUnicode(),
        returnType);
        //Calendar.class.isAssignableFrom(returnType) ? Calendar.class : returnType);
        criteria.add(Restrictions.eq(key.getName(),keyValue));
        //parser.invokeSetMethod(obj,key.getName(),keyValue);
      } catch (final EdmPrimitiveTypeException e) {
        throw new DataProviderException("Wrong key!", e);
      }
    }
    //@SuppressWarnings("rawtypes")
    return criteria.list();
  }

  public void update(Entity entity,final EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws
  ClassNotFoundException, DataProviderException, InstantiationException, IllegalAccessException,
  IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, UriParserException{
    final EdmEntityType entityType = edmEntitySet.getEntityType();
    final TypeParser parser=new TypeParser(entityType.getFullQualifiedName());
    Session session = null;
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      @SuppressWarnings("rawtypes")
      List objs = findObjects(session,entityType,keys);
      if(objs.size()==0) {
        throw new NotFoundDataProviderException("Object not found!");
      }
      Object object=objs.get(0);
      for (Property prop : entity.getProperties()) {
        parser.invokeSetMethod(object, prop.getName(), prop.getValue());
      }
      session.beginTransaction();
      for (Link link : entity.getNavigationBindings()) {
        UriInfo uriInfo = parseUri(link.getBindingLink());
        final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        objs = findObjects(session,resourceEntitySet.getEntitySet().getEntityType(),
            resourceEntitySet.getKeyPredicates());
        if(objs.size()>0) {
          parser.invokeSetMethod(object,link.getTitle(),objs.get(0));
        }
      }
      session.update(object);
      session.getTransaction().commit();
    }finally {
      if (session != null && session.isOpen()) {
          session.close();
      }
    }
  }



  public void delete(final EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws
  ClassNotFoundException, DataProviderException, InstantiationException, IllegalAccessException,
  IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    final EdmEntityType entityType = edmEntitySet.getEntityType();
    Session session = null;
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      @SuppressWarnings("rawtypes")
      List objs = findObjects(session,entityType,keys);
      if(objs.size()==0) {
        throw new NotFoundDataProviderException("Object not found!");
      }
      session.beginTransaction();
      session.delete(objs.get(0));
      session.getTransaction().commit();
    }finally {
      if (session != null && session.isOpen()) {
          session.close();
      }
    }
  }


  @SuppressWarnings("rawtypes")
  public Entity read(UriInfoResource uriInfo, final EdmEntitySet edmEntitySet)
  //(final EdmEntitySet edmEntitySet, final List<UriParameter> keys)
      throws DataProviderException{
    try {
      UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
      EdmEntityType entityType = edmEntitySet.getEntityType();

      final List<UriParameter> keys=resourceEntitySet.getKeyPredicates();

      Session session = null;
      try{
        session = HibernateUtil.getSessionFactory().openSession();
        List objs = findObjects(session,entityType,keys);
        if(objs.size()==0) {
          throw new NotFoundDataProviderException("Object not found!");
        }
        TypeParser parser=new TypeParser(entityType.getFullQualifiedName());
        if(getKind(uriInfo)==UriResourceKind.navigationProperty) {
          UriResourceNavigation resourceNavigation = (UriResourceNavigation) uriInfo.getUriResourceParts().get(1);
          String column=resourceNavigation.getProperty().getName();
          Object obj=parser.invokeGetMethod(objs.get(0),column);
          parser=new TypeParser(parser.getJoinColumnType(column).getCanonicalName());
          Serializable identifier = session.getIdentifier(obj);
          String entityName = session.getEntityName(obj);
          session.evict(obj);
          obj=session.get(entityName, identifier);
          return parser.createEntity(obj);
        }
        return parser.createEntity(objs.get(0));

      }finally {
        if (session != null && session.isOpen()) {
            session.close();
        }
      }
    } catch (NotFoundDataProviderException e) {
      throw e;
    } catch (Exception e) {
      throw new DataProviderException(e.getMessage());
    }


  }

  public static class DataProviderException extends ODataException {
    private static final long serialVersionUID = 5098059649321796156L;

    public DataProviderException(String message, Throwable throwable) {
      super(message, throwable);
    }

    public DataProviderException(String message) {
      super(message);
    }
  }

  public static class NotFoundDataProviderException extends DataProviderException {
    private static final long serialVersionUID = 5098059649321796151L;

    public NotFoundDataProviderException(String message, Throwable throwable) {
      super(message, throwable);
    }

    public NotFoundDataProviderException(String message) {
      super(message);
    }
  }




}