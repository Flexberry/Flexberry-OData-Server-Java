package net.flexberry.services.server.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.flexberry.services.edm.PrimitiveTypeParser;
import net.flexberry.services.util.HibernateUtil;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntitySet;
//import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.data.EntityImpl;
import org.apache.olingo.commons.core.data.EntitySetImpl;
import org.apache.olingo.commons.core.data.PropertyImpl;
import org.apache.olingo.server.api.edm.provider.Property;
import org.apache.olingo.server.api.uri.UriParameter;
import org.hibernate.Session;



public class OdataHibernateDataProvider {

  private Map<String, EntitySet> data;
  private List<String> classesNames;
  private List<Class<?>> classes;
  private String namespace;
  
  public Set<String> getEntitySets(){
    return data.keySet();
  }

  public List<String> getClassesNames(){
    return classesNames;
  }
  
  public String getNamespace(){
    return namespace;
  }

  public String getEntitySetName0(String cls){
    if(namespace.length()==0)
      return "EntitySet_"+cls;
    return "EntitySet_"+cls.substring(namespace.length()+1);
  }
  
  public String getEntitySetName(String cls){
    return "EntitySet_"+classesNames.indexOf(cls);
  }
  
  private void init() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    Session session = null;
    data = new HashMap<String, EntitySet>();
    try{
      session = HibernateUtil.getSessionFactory().openSession();
      for (Class<?> clazz : classes) {
        String name=clazz.getCanonicalName();
        PrimitiveTypeParser parser=new PrimitiveTypeParser(name);
        HashMap<String, String> methods=parser.getMethods();
        EntitySet entitySet = new EntitySetImpl();
        List<Entity> entities=entitySet.getEntities();
        @SuppressWarnings("rawtypes")
        List objs = session.createCriteria(clazz).list();
        for (Object obj : objs) {
          EntityImpl entity=new EntityImpl();
          for (String method : methods.keySet()) {
            Method md=clazz.getMethod(method);
            PropertyImpl property=new PropertyImpl(null, methods.get(method), ValueType.PRIMITIVE, md.invoke(obj));
            entity.addProperty(property);
          }
          entities.add(entity);
        }
        data.put(getEntitySetName(clazz.getCanonicalName()), entitySet);
      }
    }finally {
      if (session != null && session.isOpen()) {
          session.close();
      }
    }
  }
  
  private void init(List<Class<?>> classes) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    if(classesNames.size()>0){
      int p=classesNames.get(0).lastIndexOf(".");
      if(p!=-1)
        namespace=classesNames.get(0).substring(0,p);
      else
        namespace="";
    }
    this.classes=classes;
    init();
  }
  public OdataHibernateDataProvider(List<String> classesNames) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    List<Class<?>> classes = new ArrayList<Class<?>>();
    this.classesNames=classesNames;
    for (String className : classesNames) {
      classes.add(Class.forName(className));
    }
    init(classes);
  }
  
  
  public OdataHibernateDataProvider(String javaPackage) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
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
  
  public EntitySet readAll(EdmEntitySet edmEntitySet) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    init();
    return data.get(edmEntitySet.getName());
  }

  public void create(Entity entity) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    PrimitiveTypeParser parser=new PrimitiveTypeParser(entity.getType());
    Object obj=parser.createObject();
    for (org.apache.olingo.commons.api.data.Property prop : entity.getProperties()) {
      String method=parser.getColumns().get(prop.getName());
      method="set"+method.substring(3);
      Method md=obj.getClass().getMethod(method,prop.getValue().getClass());
      md.invoke(obj,prop.getValue());
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
  
  public Entity read(final EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws DataProviderException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final EdmEntityType entityType = edmEntitySet.getEntityType();
    init();
    final EntitySet entitySet = data.get(edmEntitySet.getName());
    if (entitySet == null) {
      return null;
    } else {
      try {
        for (final Entity entity : entitySet.getEntities()) {
          boolean found = true;
          for (final UriParameter key : keys) {
            final EdmProperty property = (EdmProperty) entityType.getProperty(key.getName());
            final EdmPrimitiveType type = (EdmPrimitiveType) property.getType();
            final Object value = entity.getProperty(key.getName()).getValue();
            final Object keyValue = type.valueOfString(type.fromUriLiteral(key.getText()),
                property.isNullable(), property.getMaxLength(), property.getPrecision(), property.getScale(),
                property.isUnicode(),
                Calendar.class.isAssignableFrom(value.getClass()) ? Calendar.class : value.getClass());
            if (!value.equals(keyValue)) {
              found = false;
              break;
            }
          }
          if (found) {
            return entity;
          }
        }
        return null;
      } catch (final EdmPrimitiveTypeException e) {
        throw new DataProviderException("Wrong key!", e);
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
} 