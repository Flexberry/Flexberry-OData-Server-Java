package ru.ics.olingo.edm.hibernate;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.edm.provider.EntityType;
import org.apache.olingo.server.api.edm.provider.Property;
import org.apache.olingo.server.api.edm.provider.PropertyRef;

/**
 * Parses the class-file as an olingo EntityType. Called from EdmProvider class.
 */
public class PrimitiveTypeParser {

  private FullQualifiedName fqn;
  public PrimitiveTypeParser(String className){
    fqn=new FullQualifiedName(className);
  }
  public PrimitiveTypeParser(FullQualifiedName fqn){
    this.fqn=fqn;
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
  
  
  public EntityType getEntityType() throws ClassNotFoundException{
    Class<?> cl = Class.forName(fqn.getFullQualifiedNameAsString());
    if(!cl.isAnnotationPresent(Entity.class)){
      return null;
    }
    EntityType type=new EntityType();
    type.setName(fqn.getName());
    
    for(Method md: cl.getMethods()){
      if(md.isAnnotationPresent(Id.class) && md.isAnnotationPresent(Column.class)){
        EdmPrimitiveTypeKind edmType=mapEdmPrimitiveTypeKind(md.getReturnType());
        if(edmType==null)
          continue;
        Column column=md.getAnnotation(Column.class);
        type.setKey(Arrays.asList(new PropertyRef().setPropertyName(column.name())));
        break;
      }
    }     

    ArrayList<Property> properties=new ArrayList<Property>();
    for(Method md: cl.getMethods()){
      if(md.isAnnotationPresent(Column.class)){
        EdmPrimitiveTypeKind edmType=mapEdmPrimitiveTypeKind(md.getReturnType());
        if(edmType==null)
          continue;
        Column column=md.getAnnotation(Column.class);
        Property property=new Property();
        property.setName(column.name());
        property.setType(edmType.getFullQualifiedName());
        
        properties.add(property);
      }
    }
    type.setProperties(properties);
    return type;
  }
}
