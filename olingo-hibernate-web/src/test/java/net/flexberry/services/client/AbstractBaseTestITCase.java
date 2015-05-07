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

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import net.flexberry.services.server.OdataHibernateServlet;

import org.apache.catalina.LifecycleException;
import org.apache.olingo.client.api.communication.request.retrieve.XMLMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.edm.xml.EntityContainer;
import org.apache.olingo.client.api.edm.xml.EntitySet;
import org.apache.olingo.client.api.edm.xml.XMLMetadata;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBaseTestITCase {

  protected static final String SERVICE_URI = OdataHibernateSvcConst.BASE_URI;

  /**
   * Logger.
   */
  protected static final Logger LOG = LoggerFactory.getLogger(AbstractBaseTestITCase.class);

  protected String getGUID(){
    return UUID.randomUUID().toString().toUpperCase();
  }

  
  protected ODataClient getClient() {
    ODataClient odata = ODataClientFactory.getV4();
    odata.getConfiguration().setDefaultPubFormat(ODataFormat.JSON);
    return odata;
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
  
  
  @BeforeClass
  public static void init()
      throws LifecycleException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    TomcatTestServer.init(9080)
        .addServlet(OdataHibernateServlet.class, "/olingo-hibernate-web/hibernate.svc/*")
        .addWebApp(false)
        .start();
  }

}