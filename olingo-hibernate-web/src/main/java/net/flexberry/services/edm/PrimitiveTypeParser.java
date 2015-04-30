package net.flexberry.services.edm;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.edm.provider.EntityType;
import org.apache.olingo.server.api.edm.provider.Property;
import org.apache.olingo.server.api.edm.provider.PropertyRef;

/**
 * Parses the class-file as an olingo EntityType. Called from EdmProvider class.
 */
public class PrimitiveTypeParser {
  private Class<?> cl;
  private FullQualifiedName fqn;
  private ArrayList<Property> properties;
  private String key;
  private HashMap<String, String> methods;
  private HashMap<String, String> columns;  
  
  public PrimitiveTypeParser(String className) throws ClassNotFoundException{
    this(new FullQualifiedName(className));
  }
  
  public HashMap<String, String> getMethods(){
    return methods;
  }

  public HashMap<String, String> getColumns(){
    return columns;
  }

  public Object createObject() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    return cl.getConstructor(new Class[0]).newInstance();
  }
  
  
  public PrimitiveTypeParser(FullQualifiedName fqn) throws ClassNotFoundException{
    this.fqn=fqn;
    cl = Class.forName(fqn.getFullQualifiedNameAsString());
    if(!cl.isAnnotationPresent(Entity.class))
      return;
    properties=new ArrayList<Property>();
    methods=new HashMap<String, String>();
    columns=new HashMap<String, String>();
    for(Method md: cl.getMethods()){
      if(md.isAnnotationPresent(Column.class)){
        EdmPrimitiveTypeKind edmType=mapEdmPrimitiveTypeKind(md.getReturnType());
        if(edmType==null)
          continue;
        Column column=md.getAnnotation(Column.class);
        if(md.isAnnotationPresent(Id.class))
          key=column.name();
        Property property=new Property();
        property.setPrecision(100);
        String propertyName=column.name();
        property.setName(propertyName);
        property.setType(edmType.getFullQualifiedName());
        methods.put(md.getName(), column.name());
        columns.put(column.name(),md.getName());
        properties.add(property);
      }
    }
  }
  
  public static EdmPrimitiveTypeKind mapEdmPrimitiveTypeKind(Class<?> type){
    EdmPrimitiveTypeKind edmType=null;
    if(type==Boolean.class)
      edmType=EdmPrimitiveTypeKind.Boolean;
    else
    if(type==Byte.class)
      edmType=EdmPrimitiveTypeKind.Byte;
    else
    if(type==Short.class)
      edmType=EdmPrimitiveTypeKind.Int16;
    else
    if(type==Integer.class)
      edmType=EdmPrimitiveTypeKind.Int32;
    else
    if(type==Long.class)
      edmType=EdmPrimitiveTypeKind.Int64;
    else
    if(type==Float.class)
      edmType=EdmPrimitiveTypeKind.Single;
    else
    if(type==Double.class)
      edmType=EdmPrimitiveTypeKind.Double;
    else
    if(type==java.util.Date.class)
      edmType=EdmPrimitiveTypeKind.DateTime;
    else
    if(type==Character.class)
      edmType=EdmPrimitiveTypeKind.String;
    else
    if(type==String.class)
      edmType=EdmPrimitiveTypeKind.String;
    return edmType;
  }
  
  public static Class<?> mapJavaPrimitiveType(EdmPrimitiveTypeKind edmType){
    Class<?> type=null;
    if(edmType==EdmPrimitiveTypeKind.Boolean)
      type=Boolean.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Byte)
      type=Byte.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Int16)
      type=Short.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Int32)
      type=Integer.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Int64)
      type=Long.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Single)
      type=Float.class;
    else
    if(edmType==EdmPrimitiveTypeKind.Double)
      type=Double.class;
    else
    if(edmType==EdmPrimitiveTypeKind.DateTime)
      type=java.util.Date.class;
    else
    if(edmType==EdmPrimitiveTypeKind.String)
      type=Character.class;
    else
    if(edmType==EdmPrimitiveTypeKind.String)
      type=String.class;
    return type;
  }
  

  
  public EntityType createEntityType() throws ClassNotFoundException{
    if(!cl.isAnnotationPresent(Entity.class))
      return null;
    EntityType type=new EntityType();
    type.setName(fqn.getName());
    if(key!=null)
      type.setKey(Arrays.asList(new PropertyRef().setPropertyName(key)));
    type.setProperties(properties);
    return type;
  }
}
