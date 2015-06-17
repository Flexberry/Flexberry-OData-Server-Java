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

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServlet;

import net.flexberry.services.client.TomcatTestServer.TestServerBuilder;
import net.flexberry.services.server.LoggerFilter;
import net.flexberry.services.server.OdataHibernateServlet;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBaseTestITCase {

  protected static final int SERVICE_PORT=9080;
  protected static final String SERVLET_PATH = "/olingo-hibernate-web/hibernate.svc";
  protected static final String SERVICE_URI = "http://localhost:"+SERVICE_PORT+SERVLET_PATH;
  protected static final String FILTER_NAME="LoggerFilter";
  protected static final String FILTER_CLASS=LoggerFilter.class.getName();
  protected static final String SERVLET_NAME="OdataHibernateServlet";
  protected static final String SERVLET_CLASS=OdataHibernateServlet.class.getName();

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

  @BeforeClass
  public static void init()
      throws LifecycleException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    HttpServlet httpServlet = (HttpServlet)Class.forName(SERVLET_CLASS).newInstance();
    TestServerBuilder builder=TomcatTestServer.init(SERVICE_PORT)
        .addServlet(httpServlet,SERVLET_NAME, SERVLET_PATH+"/*")
        .addWebApp(false);
    FilterDef filterDef=new FilterDef();
    filterDef.setFilterName(FILTER_NAME);
    filterDef.setFilterClass(FILTER_CLASS);
    filterDef.getParameterMap().put("active", "true");
    builder.getContext().addFilterDef(filterDef);
    FilterMap filterMap=new FilterMap();
    filterMap.setFilterName(FILTER_NAME);
    filterMap.addServletName(SERVLET_NAME);
    builder.getContext().addFilterMap(filterMap);
    builder.start();
  }

}