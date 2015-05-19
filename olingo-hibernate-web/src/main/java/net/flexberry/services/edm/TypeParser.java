package net.flexberry.services.edm;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.data.EntityImpl;
import org.apache.olingo.commons.core.data.PropertyImpl;
import org.apache.olingo.server.api.edm.provider.EntityType;
import org.apache.olingo.server.api.edm.provider.NavigationProperty;
import org.apache.olingo.server.api.edm.provider.Property;
import org.apache.olingo.server.api.edm.provider.PropertyRef;

/**
 * Parses the class-file as an olingo EntityType. Called from EdmProvider class.
 */
public class TypeParser {
  private Class<?> cl;
  private FullQualifiedName fqn;
  private ArrayList<Property> properties;
  private ArrayList<NavigationProperty> navigationProperties;
  private List<String> key;
  private HashMap<String, String> columns;
  private HashMap<String, String> joinColumns;

  public TypeParser(String className) throws ClassNotFoundException, NoSuchMethodException, SecurityException{
    this(new FullQualifiedName(className));
  }

  public HashMap<String, String> getColumns(){
    return columns;
  }

  public HashMap<String, String> getJoinColumns(){
    return joinColumns;
  }

  public Object createObject() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
  InvocationTargetException, NoSuchMethodException, SecurityException{
    return cl.getConstructor(new Class[0]).newInstance();
  }

  public EntityImpl createEntity(Object obj) throws NoSuchMethodException, SecurityException, IllegalAccessException,
  IllegalArgumentException, InvocationTargetException{
    EntityImpl entity=new EntityImpl();
    for (String column : getColumns().keySet()) {
      PropertyImpl property=new PropertyImpl(null, column, ValueType.PRIMITIVE, invokeGetMethod(obj,column));
      entity.addProperty(property);
    }
    return entity;
  }

  public void invokeSetMethod(Object obj,String columnName,Object value) throws NoSuchMethodException,
  SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    String method=getColumns().get(columnName);
    if(method==null) {
      method=getJoinColumns().get(columnName);
    }
    method="set"+method.substring(3);
    Method md=cl.getMethod(method,value.getClass());
    md.invoke(obj,value);
  }

  public Object invokeGetMethod(Object obj,String columnName) throws NoSuchMethodException, SecurityException,
  IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    String method=getColumns().get(columnName);
    if(method==null) {
      method=getJoinColumns().get(columnName);
    }
    Method md=cl.getMethod(method);
    return md.invoke(obj);
  }

  public Class<?> getColumnType(String columnName) throws NoSuchMethodException, SecurityException{
    String method=getColumns().get(columnName);
    return cl.getMethod(method).getReturnType();
  }

  public Class<?> getJoinColumnType(String columnName) throws NoSuchMethodException, SecurityException{
    String method=getJoinColumns().get(columnName);
    return cl.getMethod(method).getReturnType();
  }

  public TypeParser(FullQualifiedName fqn) throws ClassNotFoundException, NoSuchMethodException, SecurityException{
    this.fqn=fqn;
    cl = Class.forName(fqn.getFullQualifiedNameAsString());
    if(!cl.isAnnotationPresent(Entity.class)) {
      return;
    }
    properties=new ArrayList<Property>();
    navigationProperties=new ArrayList<NavigationProperty>();
    columns=new HashMap<String, String>();
    joinColumns=new HashMap<String, String>();
    key=new ArrayList<String>();
    for(Method md: cl.getMethods()){
      if(md.isAnnotationPresent(Column.class)){
        EdmPrimitiveTypeKind edmType=mapEdmPrimitiveTypeKind(md.getReturnType());
        if(edmType==null) {
          continue;
        }
        Column column=md.getAnnotation(Column.class);
        String propertyName=column.name();
        if(md.isAnnotationPresent(Id.class)) {
          key.add(propertyName);
        }
        Property property=new Property();
        property.setPrecision(100);
        property.setName(propertyName);
        property.setType(edmType.getFullQualifiedName());
        columns.put(propertyName,md.getName());
        properties.add(property);
      }
      if(md.isAnnotationPresent(JoinColumn.class)&& md.isAnnotationPresent(ManyToOne.class)){
        JoinColumn joinColumn=md.getAnnotation(JoinColumn.class);
        String propertyName=joinColumn.name();
        joinColumns.put(propertyName,md.getName());
        if(md.isAnnotationPresent(Id.class)) {
          key.add(propertyName);
        }
        NavigationProperty navigationProperty=new NavigationProperty();
        navigationProperty.setName(propertyName);
        FullQualifiedName type=new FullQualifiedName(getJoinColumnType(propertyName).getCanonicalName());
        navigationProperty.setType(type);
        navigationProperties.add(navigationProperty);
      }
    }
  }

  public static EdmPrimitiveTypeKind mapEdmPrimitiveTypeKind(Class<?> type){
    EdmPrimitiveTypeKind edmType=null;
    if(type==Boolean.class) {
      edmType=EdmPrimitiveTypeKind.Boolean;
    } else
    if(type==Byte.class) {
      edmType=EdmPrimitiveTypeKind.Byte;
    } else
    if(type==Short.class) {
      edmType=EdmPrimitiveTypeKind.Int16;
    } else
    if(type==Integer.class) {
      edmType=EdmPrimitiveTypeKind.Int32;
    } else
    if(type==Long.class) {
      edmType=EdmPrimitiveTypeKind.Int64;
    } else
    if(type==Float.class) {
      edmType=EdmPrimitiveTypeKind.Single;
    } else
    if(type==Double.class) {
      edmType=EdmPrimitiveTypeKind.Double;
    } else
    if(type==java.math.BigDecimal.class) {
      edmType=EdmPrimitiveTypeKind.Decimal;
    } else
    if(type==java.util.Date.class) {
      edmType=EdmPrimitiveTypeKind.DateTime;
    } else
    if(type==Character.class) {
      edmType=EdmPrimitiveTypeKind.String;
    } else
    if(type==String.class) {
      edmType=EdmPrimitiveTypeKind.String;
    }
    return edmType;
  }

  public EntityType createEntityType() throws ClassNotFoundException{
    if(!cl.isAnnotationPresent(Entity.class)) {
      return null;
    }
    EntityType type=new EntityType();
    type.setName(fqn.getName());
    if(key!=null && key.size()>0) {
      List<PropertyRef> propertyRefs=new ArrayList<PropertyRef>();
      for (String propertyRefName : key) {
        propertyRefs.add(new PropertyRef().setPropertyName(propertyRefName));
      }
      type.setKey(propertyRefs);
    }
    if(properties.size()>0){
      type.setProperties(properties);
    }
    if(navigationProperties.size()>0){
      type.setNavigationProperties(navigationProperties);
    }
    return type;
  }
}
