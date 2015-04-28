/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
//package org.apache.olingo.server.sample.edmprovider;
package org.apache.olingo.server.sample.edmprovider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.flexberry.services.edm.PrimitiveTypeParser;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.Target;
import org.apache.olingo.server.api.edm.provider.ComplexType;
import org.apache.olingo.server.api.edm.provider.EdmProvider;
import org.apache.olingo.server.api.edm.provider.EntityContainer;
import org.apache.olingo.server.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.server.api.edm.provider.EntitySet;
import org.apache.olingo.server.api.edm.provider.EntityType;
import org.apache.olingo.server.api.edm.provider.NavigationProperty;
import org.apache.olingo.server.api.edm.provider.NavigationPropertyBinding;
import org.apache.olingo.server.api.edm.provider.Property;
import org.apache.olingo.server.api.edm.provider.PropertyRef;
import org.apache.olingo.server.api.edm.provider.Schema;
import org.apache.olingo.server.sample.data.StudentsDataProvider;

public class StudentsEdmProvider extends EdmProvider {

  // EDM Container
  public static final String CONTAINER_NAME = "Container";
  private FullQualifiedName container_fqn;// = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
  private StudentsDataProvider dataProvider;
  public StudentsEdmProvider(StudentsDataProvider dataProvider){
    
    this.dataProvider=dataProvider;
    container_fqn = new FullQualifiedName(dataProvider.getNamespace(), CONTAINER_NAME);
  }
  
  @Override
  public EntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {
    try {
      PrimitiveTypeParser parser=new PrimitiveTypeParser(entityTypeName);
      EntityType entityType=parser.getEntityType();
      /*
      entityType.setNavigationProperties(Arrays.asList(
          new NavigationProperty().setName("Manufacturer").setType(ET_MANUFACTURER)
          )
      );
      */
      return entityType;
    } catch (ClassNotFoundException e) {
      throw new ODataException(e);
    }
  }

  /*
  public ComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
    if (CT_ADDRESS.equals(complexTypeName)) {
      return new ComplexType().setName(CT_ADDRESS.getName()).setProperties(Arrays.asList(
          new Property().setName("Street").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
          new Property().setName("City").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
          new Property().setName("ZipCode").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
          new Property().setName("Country").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
          ));
    }
    return null;
  }
*/
  
  @Override
  public EntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName)
      throws ODataException {
    if (container_fqn.equals(entityContainer)) {
      for (String cls : dataProvider.getClassesNames()) {
        String esName=dataProvider.getEntitySetName(cls);
        if (esName.equals(entitySetName)) {
          return new EntitySet()
              .setName(esName)
              .setType(new FullQualifiedName(cls))/*
              .setNavigationPropertyBindings(
                  Arrays.asList(
                      new NavigationPropertyBinding().setPath("Manufacturer").setTarget(
                          new Target().setTargetName(ES_MANUFACTURER_NAME).setEntityContainer(CONTAINER_FQN)))) */;
        }
        
      }
      
      
    }
    return null;
  }

  @Override
  public List<Schema> getSchemas() throws ODataException {
    List<Schema> schemas = new ArrayList<Schema>();
    Schema schema = new Schema();
    schema.setNamespace(dataProvider.getNamespace());
    // EntityTypes
    List<EntityType> entityTypes = new ArrayList<EntityType>();
    for (String cls : dataProvider.getClassesNames()) {
      entityTypes.add(getEntityType(new FullQualifiedName(cls)));
    }
    schema.setEntityTypes(entityTypes);

    // ComplexTypes
/*
    List<ComplexType> complexTypes = new ArrayList<ComplexType>();
    complexTypes.add(getComplexType(CT_ADDRESS));
    schema.setComplexTypes(complexTypes);
*/
    // EntityContainer
    schema.setEntityContainer(getEntityContainer());
    schemas.add(schema);

    return schemas;
  }

  @Override
  public EntityContainer getEntityContainer() throws ODataException {
    EntityContainer container = new EntityContainer();
    container.setName(container_fqn.getName());

    // EntitySets
    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    container.setEntitySets(entitySets);
    
    for (String cls : dataProvider.getClassesNames()) {
      entitySets.add(getEntitySet(container_fqn, dataProvider.getEntitySetName(cls)));
    }
    
    

    return container;
  }

  @Override
  public EntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName) throws ODataException {
    if (entityContainerName == null || container_fqn.equals(entityContainerName)) {
      return new EntityContainerInfo().setContainerName(container_fqn);
    }
    return null;
  }
} 