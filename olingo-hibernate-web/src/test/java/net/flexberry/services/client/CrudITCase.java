/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.flexberry.services.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashMap;

import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.cud.ODataDeleteRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataEntityCreateRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataEntityUpdateRequest;
import org.apache.olingo.client.api.communication.request.cud.v4.UpdateType;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.XMLMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataEntityCreateResponse;
import org.apache.olingo.client.api.communication.response.ODataEntityUpdateResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.edm.xml.EntityContainer;
import org.apache.olingo.client.api.edm.xml.EntitySet;
import org.apache.olingo.client.api.edm.xml.XMLMetadata;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.commons.api.domain.v4.ODataEntity;
import org.apache.olingo.commons.api.domain.v4.ODataLink;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.Test;

public class CrudITCase extends AbstractBaseTestITCase {


  @Test
  public void navigationProperty() {
    try {
      final ODataClient client = getClient();
      HashMap<String, String> map=getMapEntitySet();
      String entityType1="servicebus.Stormfiltersetting";
      assertTrue(map.containsKey(entityType1));
      String entitySetName1=map.get(entityType1);
      String guid1=getGUID();
      ODataEntity entity1 = client.getObjectFactory().
          newEntity(new FullQualifiedName(entityType1));
      entity1.getProperties().add(client.getObjectFactory().newPrimitiveProperty("Name",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString("Name 01")));
      entity1.getProperties().add(client.getObjectFactory().newPrimitiveProperty("DataObjectView",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString("DataObjectView 01")));
      entity1.getProperties().add(client.getObjectFactory().newPrimitiveProperty("primaryKey",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString(guid1)));
      final ODataEntityCreateRequest<ODataEntity> reqCreate1 = client.getCUDRequestFactory().getEntityCreateRequest(
          client.newURIBuilder(SERVICE_URI).appendEntitySetSegment(entitySetName1).build(), entity1);
      reqCreate1.setFormat(ODataFormat.JSON);
      final ODataEntityCreateResponse<ODataEntity> responseCreate1 = reqCreate1.execute();
      assertEquals(201, responseCreate1.getStatusCode());
      URI entity1Ref = client.newURIBuilder("")
          .appendEntitySetSegment(entitySetName1)
          .appendKeySegment(guid1)
          .build();
      String entityType2="servicebus.Stormfilterlookup";
      assertTrue(map.containsKey(entityType2));
      String entitySetName2=map.get(entityType2);
      String guid2=getGUID();
      ODataEntity entity2 = client.getObjectFactory().
          newEntity(new FullQualifiedName(entityType2));
      entity2.getProperties().add(client.getObjectFactory().newPrimitiveProperty("DataObjectType",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString("DataObjectType 01")));
      entity2.getProperties().add(client.getObjectFactory().newPrimitiveProperty("primaryKey",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString(guid2)));

      ODataLink filterSetting_m0 = client.getObjectFactory()
          .newEntityNavigationLink("FilterSetting_m0", entity1Ref);
      entity2.getNavigationLinks().add(filterSetting_m0);
      final ODataEntityCreateRequest<ODataEntity> reqCreate2 = client.getCUDRequestFactory().getEntityCreateRequest(
          client.newURIBuilder(SERVICE_URI).appendEntitySetSegment(entitySetName2).build(), entity2);
      reqCreate2.setFormat(ODataFormat.JSON);
      final ODataEntityCreateResponse<ODataEntity> responseCreate2 = reqCreate2.execute();
      assertEquals(201, responseCreate2.getStatusCode());

      ODataEntityRequest<ODataEntity> reqRead = client
          .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment(entitySetName2)
        .appendKeySegment(guid2)
        .appendPropertySegment("FilterSetting_m0")
        .build());
      assertNotNull(reqRead);
      ODataRetrieveResponse<ODataEntity> responseRead = reqRead.execute();
      assertEquals(HttpStatusCode.OK.getStatusCode(), responseRead.getStatusCode());
      ODataEntity entity = responseRead.getBody();
      assertNotNull(entity);
      Object prevValue=entity.getProperty("primaryKey").getValue().asPrimitive().toValue();
      //assertEquals(333, entity.getProperty("Priority").getValue().asPrimitive().toValue());
      //assertEquals(guid, entity.getProperty("primaryKey").getValue().asPrimitive().toValue());

      String guid3=getGUID();
      ODataEntity entity3 = client.getObjectFactory().
          newEntity(new FullQualifiedName(entityType1));
      entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("Name",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString("Name 03")));
      entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("DataObjectView",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString("DataObjectView 03")));
      entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("primaryKey",
          client.getObjectFactory().newPrimitiveValueBuilder().buildString(guid3)));
      final ODataEntityCreateRequest<ODataEntity> reqCreate3 = client.getCUDRequestFactory().getEntityCreateRequest(
          client.newURIBuilder(SERVICE_URI).appendEntitySetSegment(entitySetName1).build(), entity3);
      reqCreate3.setFormat(ODataFormat.JSON);
      final ODataEntityCreateResponse<ODataEntity> responseCreate3 = reqCreate3.execute();
      assertEquals(201, responseCreate3.getStatusCode());
      URI entity3Ref = client.newURIBuilder("")
          .appendEntitySetSegment(entitySetName1)
          .appendKeySegment(guid3)
          .build();
      filterSetting_m0 = client.getObjectFactory()
          .newEntityNavigationLink("FilterSetting_m0", entity3Ref);

      // Test an interface of update
      URI uri = client.newURIBuilder(SERVICE_URI)
          .appendEntitySetSegment(entitySetName2).appendKeySegment(guid2).build();
      entity2 = client.getObjectFactory().
          newEntity(new FullQualifiedName(entityType2));
      entity2.getNavigationLinks().add(filterSetting_m0);
      final ODataEntityUpdateRequest<ODataEntity> reqUpdate =
          client.getCUDRequestFactory().getEntityUpdateRequest(uri, UpdateType.PATCH, entity2);
      reqUpdate.setFormat(ODataFormat.JSON);
      final ODataEntityUpdateResponse<ODataEntity> responseUpdate = reqUpdate.execute();
      assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), responseUpdate.getStatusCode());

      reqRead = client
          .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment(entitySetName2)
        .appendKeySegment(guid2)
        .appendPropertySegment("FilterSetting_m0")
        .build());
      assertNotNull(reqRead);
      responseRead = reqRead.execute();
      assertEquals(HttpStatusCode.OK.getStatusCode(), responseRead.getStatusCode());
      entity = responseRead.getBody();
      assertNotNull(entity);
      assertTrue(!entity.getProperty("primaryKey").getValue().asPrimitive().toValue().equals(prevValue));
    } catch (ODataClientErrorException e) {
      fail(e.getMessage());
    }catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void navigationProperty2() {
    final ODataClient client = getClient();
    HashMap<String, String> map=getMapEntitySet();
    String entityType1="servicebus.Stormfiltersetting";
    assertTrue(map.containsKey(entityType1));
    String entitySetName1=map.get(entityType1);

    String guid3=getGUID();
    ODataEntity entity3 = client.getObjectFactory().
        newEntity(new FullQualifiedName(entityType1));
    entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("Name",
        client.getObjectFactory().newPrimitiveValueBuilder().buildString("Name 01")));
    entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("DataObjectView",
        client.getObjectFactory().newPrimitiveValueBuilder().buildString("DataObjectView 01")));
    entity3.getProperties().add(client.getObjectFactory().newPrimitiveProperty("primaryKey",
        client.getObjectFactory().newPrimitiveValueBuilder().buildString(guid3)));
    final ODataEntityCreateRequest<ODataEntity> reqCreate3 = client.getCUDRequestFactory().getEntityCreateRequest(
        client.newURIBuilder(SERVICE_URI).appendEntitySetSegment(entitySetName1).build(), entity3);
    reqCreate3.setFormat(ODataFormat.JSON);
    final ODataEntityCreateResponse<ODataEntity> responseCreate3 = reqCreate3.execute();
    assertEquals(201, responseCreate3.getStatusCode());
    URI entity3Ref = client.newURIBuilder("")
        .appendEntitySetSegment(entitySetName1)
        .appendKeySegment(guid3)
        .build();

  }

  @Test
  public void crud() {
    final ODataClient client = getClient();
    String entityType="servicebus.ApplicationLog";
    HashMap<String, String> map=getMapEntitySet();
    assertTrue(map.containsKey(entityType));
    String entitySetName=map.get(entityType);
    String guid=getGUID();
    URI uri=null;

    // Test an interface of create
    ODataEntity entity = client.getObjectFactory().
        newEntity(new FullQualifiedName(entityType));
    entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty("Priority",
        client.getObjectFactory().newPrimitiveValueBuilder().buildInt32(333)));
    entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty("primaryKey",
        client.getObjectFactory().newPrimitiveValueBuilder().buildString(guid)));
    final ODataEntityCreateRequest<ODataEntity> reqCreate = client.getCUDRequestFactory().getEntityCreateRequest(
        client.newURIBuilder(SERVICE_URI).appendEntitySetSegment(entitySetName).build(), entity);
    reqCreate.setFormat(ODataFormat.JSON_NO_METADATA);
    final ODataEntityCreateResponse<ODataEntity> responseCreate = reqCreate.execute();
    assertEquals(201, responseCreate.getStatusCode());

    // Test an interface of read
    final ODataEntityRequest<ODataEntity> reqRead = client
        .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
      .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build());
    assertNotNull(reqRead);
    final ODataRetrieveResponse<ODataEntity> responseRead = reqRead.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), responseRead.getStatusCode());
    entity = responseRead.getBody();
    assertNotNull(entity);
    assertEquals(333, entity.getProperty("Priority").getValue().asPrimitive().toValue());
    assertEquals(guid, entity.getProperty("primaryKey").getValue().asPrimitive().toValue());

    // Test an interface of update
    uri = client.newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build();
    entity = client.getObjectFactory().
        newEntity(new FullQualifiedName(entityType));
    entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty("Priority",
        client.getObjectFactory().newPrimitiveValueBuilder().buildInt32(999)));
    final ODataEntityUpdateRequest<ODataEntity> reqUpdate =
        client.getCUDRequestFactory().getEntityUpdateRequest(uri, UpdateType.PATCH, entity);
    reqUpdate.setFormat(ODataFormat.JSON_NO_METADATA);
    final ODataEntityUpdateResponse<ODataEntity> responseUpdate = reqUpdate.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), responseUpdate.getStatusCode());
    final ODataEntityRequest<ODataEntity> reqTestUpdate = client
        .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
      .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build());
    assertNotNull(reqTestUpdate);
    final ODataRetrieveResponse<ODataEntity> responseTestUpdate = reqTestUpdate.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), responseTestUpdate.getStatusCode());
    entity = responseTestUpdate.getBody();
    assertNotNull(entity);
    assertEquals(999, entity.getProperty("Priority").getValue().asPrimitive().toValue());

    // Test an interface of delete
    uri = client.newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build();
    final ODataDeleteRequest requestDelete = client.getCUDRequestFactory().getDeleteRequest(uri);
    assertNotNull(requestDelete);
    final ODataDeleteResponse responseDelete = requestDelete.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), responseDelete.getStatusCode());
    // Check that the entity is really gone.
    final ODataEntityRequest<ODataEntity> checkRequest = client
        .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
      .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build());
    try {
      checkRequest.execute();
      fail("Expected exception not thrown!");
    } catch (final ODataClientErrorException e) {
      assertEquals(HttpStatusCode.NOT_FOUND.getStatusCode(), e.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void readViaXmlMetadata() {
    HashMap<String, String> map=getMapEntitySet();
    assertTrue(map.containsKey("servicebus.ApplicationLog"));
    assertTrue(map.containsKey("servicebus.LogMsg"));
    assertTrue(map.containsKey("servicebus.UserSetting"));
    assertTrue(map.containsKey("servicebus.Клиент"));
    assertTrue(map.containsKey("servicebus.Подписка"));
    assertTrue(map.containsKey("servicebus.Сообщение"));
    assertTrue(map.containsKey("servicebus.ТипСообщения"));
    assertTrue(map.containsKey("servicebus.Тэг"));
    assertTrue(map.containsKey("servicebus.Шина"));
    assertTrue(map.containsKey("servicebus.Stormag"));
    assertTrue(map.containsKey("servicebus.StormadvLimit"));
    assertTrue(map.containsKey("servicebus.StormauEntity"));
    assertTrue(map.containsKey("servicebus.StormauField"));
    assertTrue(map.containsKey("servicebus.StormauObjType"));
    assertTrue(map.containsKey("servicebus.Stormfilterdetail"));
    assertTrue(map.containsKey("servicebus.Stormfilterlookup"));
    assertTrue(map.containsKey("servicebus.Stormfiltersetting"));
    assertTrue(map.containsKey("servicebus.Stormlg"));
    assertTrue(map.containsKey("servicebus.Stormnetlockdata"));
    assertTrue(map.containsKey("servicebus.Stormsettings"));
    assertTrue(map.containsKey("servicebus.Stormwebsearch"));
  }

  protected HashMap<String, String> getMapEntitySet(){
    XMLMetadataRequest request = getClient().getRetrieveRequestFactory().getXMLMetadataRequest(SERVICE_URI);
    assertNotNull(request);
    ODataRetrieveResponse<XMLMetadata> response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());
    XMLMetadata xmlMetadata = response.getBody();
    assertNotNull(xmlMetadata);
    assertTrue(xmlMetadata instanceof org.apache.olingo.client.api.edm.xml.v4.XMLMetadata);
    assertEquals(1, xmlMetadata.getSchemas().size());
    assertEquals("servicebus", xmlMetadata.getSchema("servicebus").getNamespace());
    HashMap<String, String> map=new HashMap<String, String>();
    EntityContainer container=xmlMetadata.getSchema("servicebus").getEntityContainer("Container");
    assertNotNull(container);
    for (EntitySet entitySet : container.getEntitySets()) {
      map.put(entitySet.getEntityType(), entitySet.getName());
    }
    return map;
  }

}
