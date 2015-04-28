package org.apache.olingo.server.sample.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logic.Student;
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
import org.apache.olingo.server.sample.edmprovider.StudentsEdmProvider;
import org.hibernate.Session;



public class StudentsDataProvider {

  private Map<String, EntitySet> data;
  private List<String> classesNames;
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

  public String getEntitySetName(String cls){
    if(namespace.length()==0)
      return "EntitySet_"+cls;
    return "EntitySet_"+cls.substring(namespace.length()+1);
  }
  
  
  
  private void init(List<Class<?>> classes) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    if(classesNames.size()>0){
      int p=classesNames.get(0).lastIndexOf(".");
      if(p!=-1)
        namespace=classesNames.get(0).substring(0,p);
      else
        namespace="";
    }
    
    
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
  public StudentsDataProvider(List<String> classesNames) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    List<Class<?>> classes = new ArrayList<Class<?>>();
    this.classesNames=classesNames;
    for (String className : classesNames) {
      classes.add(Class.forName(className));
    }
    init(classes);
  }
  
  
  public StudentsDataProvider(String javaPackage) throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
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
  
  public EntitySet readAll(EdmEntitySet edmEntitySet) {
    return data.get(edmEntitySet.getName());
  }

  public Entity read(final EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws DataProviderException {
    final EdmEntityType entityType = edmEntitySet.getEntityType();
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
            if (!type.valueToString(entity.getProperty(key.getName()).getValue(),
                property.isNullable(), property.getMaxLength(), property.getPrecision(), property.getScale(),
                property.isUnicode())
                .equals(key.getText())) {
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