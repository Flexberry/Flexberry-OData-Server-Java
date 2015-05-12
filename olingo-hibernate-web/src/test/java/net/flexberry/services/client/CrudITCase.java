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
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataEntityCreateResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.commons.api.domain.v4.ODataEntity;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.Test;

public class CrudITCase extends AbstractBaseTestITCase {

  @Test
  public void crud() {
    final ODataClient client = getClient();
    String entityType="servicebus.ApplicationLog";
    HashMap<String, String> map=getMapEntitySet();
    assertTrue(map.containsKey(entityType));
    String entitySetName=map.get(entityType);
    String guid=getGUID();
    
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
    ODataEntityRequest<ODataEntity> reqRead = client
        .getRetrieveRequestFactory().getEntityRequest(client.newURIBuilder(SERVICE_URI)
      .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build());
    assertNotNull(reqRead);
    ODataRetrieveResponse<ODataEntity> responseRead = reqRead.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), responseRead.getStatusCode());
    entity = responseRead.getBody();
    assertNotNull(entity);
    assertEquals(333, entity.getProperty("Priority").getValue().asPrimitive().toValue());
    assertEquals(guid, entity.getProperty("primaryKey").getValue().asPrimitive().toValue());
    
    // Test an interface of delete
    final URI uri = client.newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment(entitySetName).appendKeySegment(guid).build();
    final ODataDeleteRequest requestDelete = client.getCUDRequestFactory().getDeleteRequest(uri);
    assertNotNull(requestDelete);
    final ODataDeleteResponse responseDelete = requestDelete.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), responseDelete.getStatusCode());
    // Check that the entity is really gone.
    ODataEntityRequest<ODataEntity> checkRequest = client
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
  
}
