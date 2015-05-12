package net.flexberry.services.server.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.flexberry.services.edm.PrimitiveTypeParser;
import net.flexberry.services.util.HibernateUtil;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.core.data.EntitySetImpl;
import org.apache.olingo.server.api.uri.UriParameter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
//import org.apache.olingo.commons.api.data.Property;



public class OdataHibernateDataProvider {
  private Map<String, Class<?>> mapEntitySet=new HashMap<String, Class<?>>();
  private List<String> classesNames;
  private String namespace;
  
  public List<String> getClassesNames(){
    return classesNames;
  }
  
  public String getNamespace(){
    return namespace;
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
      PrimitiveTypeParser parser=new PrimitiveTypeParser(clazz.getCanonicalName());
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

  public void create(Entity entity) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
  InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    PrimitiveTypeParser parser=new PrimitiveTypeParser(entity.getType());
    Object obj=parser.createObject();
    for (org.apache.olingo.commons.api.data.Property prop : entity.getProperties()) {
      parser.invokeSetMethod(obj,prop.getName(),prop.getValue());
    }
    Session session = null;
    try {
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(obj);
        session.getTransaction().commit();
    } finally {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
    
  }
  
  public void delete(final EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws 
  ClassNotFoundException, DataProviderException, InstantiationException, IllegalAccessException, 
  IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    final EdmEntityType entityType = edmEntitySet.getEntityType();
    final PrimitiveTypeParser parser=new PrimitiveTypeParser(entityType.getFullQualifiedName());
    final Object obj=parser.createObject();
    Session session = null;
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      Criteria criteria=session.createCriteria(obj.getClass());
      for (final UriParameter key : keys) {
        final EdmProperty property = (EdmProperty) entityType.getProperty(key.getName());
        final EdmPrimitiveType type = (EdmPrimitiveType) property.getType();
        final Class<?> returnType=parser.getType(key.getName());
        try {
        final Object keyValue = type.valueOfString(type.fromUriLiteral(key.getText()),
          property.isNullable(), property.getMaxLength(), property.getPrecision(), property.getScale(),
          property.isUnicode(),
          returnType);
          criteria.add(Restrictions.eq(key.getName(),keyValue));
        } catch (final EdmPrimitiveTypeException e) {
          throw new DataProviderException("Wrong key!", e);
        }
      }
      @SuppressWarnings("rawtypes")
      List objs = criteria.list();
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
  
  
  public Entity read(final EdmEntitySet edmEntitySet, final List<UriParameter> keys) 
      throws DataProviderException, ClassNotFoundException, NoSuchMethodException, SecurityException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException,
      EdmPrimitiveTypeException {
    final EdmEntityType entityType = edmEntitySet.getEntityType();
    final PrimitiveTypeParser parser=new PrimitiveTypeParser(entityType.getFullQualifiedName());
    final Object obj=parser.createObject();
    Session session = null;
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      Criteria criteria=session.createCriteria(obj.getClass());
      for (final UriParameter key : keys) {
        final EdmProperty property = (EdmProperty) entityType.getProperty(key.getName());
        final EdmPrimitiveType type = (EdmPrimitiveType) property.getType();
        final Class<?> returnType=parser.getType(key.getName());
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
      @SuppressWarnings("rawtypes")
      List objs = criteria.list();
        //.add(Example.create(obj).ignoreCase())
      if(objs.size()==0) {
        throw new NotFoundDataProviderException("Object not found!");
      }
      return parser.createEntity(objs.get(0));

    }finally {
      if (session != null && session.isOpen()) {
          session.close();
      }
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