package net.flexberry.services.server.processor;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.flexberry.services.server.data.OdataHibernateDataProvider;
import net.flexberry.services.server.data.OdataHibernateDataProvider.DataProviderException;
import net.flexberry.services.server.data.OdataHibernateDataProvider.NotFoundDataProviderException;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
//import org.apache.olingo.server.core.deserializer.json.ODataJsonDeserializer;

/**
 * This processor will deliver entity collections, single entities as well as properties of an entity.
 * This is a very simple example which should give you a rough guideline on how to implement such an processor.
 * See the JavaDoc of the server.api interfaces for more information.
 */
public class OdataHibernateProcessor implements EntityCollectionProcessor, EntityProcessor,
    PrimitiveProcessor, PrimitiveValueProcessor, ComplexProcessor,ComplexCollectionProcessor,
    PrimitiveCollectionProcessor{

  private OData odata;
  private OdataHibernateDataProvider dataProvider;

  // This constructor is application specific and not mandatory for the Olingo library. We use it here to simulate the
  // database access
  public OdataHibernateProcessor(final OdataHibernateDataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  @Override
  public void init(OData odata, ServiceMetadata edm) {
    this.odata = odata;
  }

  @Override
  public void readEntityCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    // First we have to figure out which entity set to use
    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

    // Second we fetch the data for this specific entity set from the mock database and transform it into an EntitySet
    // object which is understood by our serialization
    EntitySet entitySet=null;
    try {
      entitySet = dataProvider.readAll(edmEntitySet);
    } catch (Exception e) {
      new ODataApplicationException("OdataHibernateDataProvider.readAll Exception", 0, null, e);
    }
    // Next we create a serializer based on the requested format. This could also be a custom format but we do not
    // support them in this example
    final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
    ODataSerializer serializer = odata.createSerializer(format);

    // Now the content is serialized using the serializer.
    final ExpandOption expand = uriInfo.getExpandOption();
    final SelectOption select = uriInfo.getSelectOption();

    EntityCollectionSerializerOptions options=EntityCollectionSerializerOptions.with()
    .contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
        getContextUrl(edmEntitySet, false, expand, select, null))
    .count(uriInfo.getCountOption())
    .expand(expand).select(select)
    .build();
    //options.

    //EdmElement el=edmEntitySet.getEntityType().getProperty("Timestamp");
    //edmEntitySet.getEntityType().getStructuralProperty("Timestamp").

    InputStream serializedContent = serializer.entityCollection(edmEntitySet.getEntityType(), entitySet,options);

    // Finally we set the response data, headers and status code
    response.setContent(serializedContent);
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
  }

  @Override
  public void readEntity(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    // First we have to figure out which entity set the requested entity is in
    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

    // Next we fetch the requested entity from the database
    Entity entity;
    try {
      entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
    } catch (NotFoundDataProviderException e) {
      // If no entity was found for the given key we throw an exception.
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    } catch (DataProviderException e) {
      throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
    }
    // If an entity was found we proceed by serializing it and sending it to the client.
    final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
    ODataSerializer serializer = odata.createSerializer(format);
    final ExpandOption expand = uriInfo.getExpandOption();
    final SelectOption select = uriInfo.getSelectOption();
    InputStream serializedContent = serializer.entity(edmEntitySet.getEntityType(), entity,
        EntitySerializerOptions.with()
            .contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                getContextUrl(edmEntitySet, true, expand, select, null))
            .expand(expand).select(select)
            .build());
    response.setContent(serializedContent);
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
  }

  @Override
  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                           ContentType requestFormat, ContentType responseFormat)
          throws ODataApplicationException, DeserializerException, SerializerException {

    try {
      ODataDeserializer deserializer=odata.createDeserializer(ODataFormat.fromContentType(requestFormat));
      EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
      Entity entity=deserializer.entity(request.getBody(), edmEntitySet.getEntityType());
      try {
        dataProvider.create(entity);
      } catch (Exception e) {
        throw new ODataApplicationException("dataProvider.create: "+e.getMessage(),
            HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH,e);
      }

      final ODataFormat format = ODataFormat.fromContentType(responseFormat);
      ODataSerializer serializer = odata.createSerializer(format);
      response.setContent(serializer.entity(edmEntitySet.getEntityType(), entity,
              EntitySerializerOptions.with()
                      .contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                              getContextUrl(edmEntitySet, true, null, null))
                      .build()));
      response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
      response.setHeader(HttpHeader.LOCATION,
              request.getRawBaseUri() + '/' + odata.createUriHelper().buildCanonicalURL(edmEntitySet, entity));
      
    } catch (Exception e) {
      throw new ODataApplicationException("createEntity: "+e.getMessage(),
          HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH);
    }
  }

  private ContextURL getContextUrl(final EdmEntitySet entitySet, final boolean isSingleEntity,
      final ExpandOption expand, final SelectOption select) throws SerializerException {
    return ContextURL.with().entitySet(entitySet)
        .selectList(odata.createUriHelper()
            .buildContextURLSelectList(entitySet.getEntityType(), expand, select))
        .suffix(isSingleEntity ? Suffix.ENTITY : null)
        .build();
  }

  @Override
  public void deleteEntity(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    blockNavigation(uriInfo);
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
    try {
      dataProvider.delete(resourceEntitySet.getEntitySet(), resourceEntitySet.getKeyPredicates());
    } catch (NotFoundDataProviderException e) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    } catch (Exception e) {
      throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
    }
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  @Override
  public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
          throws ODataApplicationException, SerializerException {
    readProperty(response, uriInfo, format, false);
  }

  @Override
  public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
          throws ODataApplicationException, SerializerException {
    readProperty(response, uriInfo, format, true);
  }

  @Override
  public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format)
          throws ODataApplicationException, SerializerException {
    // First we have to figure out which entity set the requested entity is in
    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    // Next we fetch the requested entity from the database
    final Entity entity;
    try {
      entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
    } catch (DataProviderException e) {
      throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
    }
    if (entity == null) {
      // If no entity was found for the given key we throw an exception.
      throw new ODataApplicationException("No entity found for this key", HttpStatusCode.NOT_FOUND
              .getStatusCode(), Locale.ENGLISH);
    } else {
      // Next we get the property value from the entity and pass the value to serialization
      UriResourceProperty uriProperty = (UriResourceProperty) uriInfo
              .getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
      EdmProperty edmProperty = uriProperty.getProperty();
      Property property = entity.getProperty(edmProperty.getName());
      if (property == null) {
        throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND
                .getStatusCode(), Locale.ENGLISH);
      } else {
        if (property.getValue() == null) {
          response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        } else {
          String value = String.valueOf(property.getValue());
          ByteArrayInputStream serializerContent = new ByteArrayInputStream(
                  value.getBytes(Charset.forName("UTF-8")));
          response.setContent(serializerContent);
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, HttpContentType.TEXT_PLAIN);
        }
      }
    }
  }

  private void readProperty(ODataResponse response, UriInfo uriInfo, ContentType contentType,
      boolean complex) throws ODataApplicationException, SerializerException {
    // To read a property we have to first get the entity out of the entity set
    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    Entity entity;
    try {
      entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
    } catch (DataProviderException e) {
      throw new ODataApplicationException(e.getMessage(),
              HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }

    if (entity == null) {
      // If no entity was found for the given key we throw an exception.
      throw new ODataApplicationException("No entity found for this key",
              HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    } else {
      // Next we get the property value from the entity and pass the value to serialization
      UriResourceProperty uriProperty = (UriResourceProperty) uriInfo
          .getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
      EdmProperty edmProperty = uriProperty.getProperty();
      Property property = entity.getProperty(edmProperty.getName());
      if (property == null) {
        throw new ODataApplicationException("No property found",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
      } else {
        if (property.getValue() == null) {
          response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        } else {
          final ODataFormat format = ODataFormat.fromContentType(contentType);
          ODataSerializer serializer = odata.createSerializer(format);
          final ContextURL contextURL = format == ODataFormat.JSON_NO_METADATA ? null :
              getContextUrl(edmEntitySet, true, null, null, edmProperty.getName());
          InputStream serializerContent = complex ?
              serializer.complex((EdmComplexType) edmProperty.getType(), property,
                  ComplexSerializerOptions.with().contextURL(contextURL).build()) :
              serializer.primitive((EdmPrimitiveType) edmProperty.getType(), property,
                                    PrimitiveSerializerOptions.with()
                                    .contextURL(contextURL)
                                    .scale(edmProperty.getScale())
                                    .nullable(edmProperty.isNullable())
                                    .precision(edmProperty.getPrecision())
                                    .maxLength(edmProperty.getMaxLength())
                                    .unicode(edmProperty.isUnicode()).build());
          response.setContent(serializerContent);
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
        }
      }
    }
  }

  private Entity readEntityInternal(final UriInfoResource uriInfo, final EdmEntitySet entitySet)
      throws OdataHibernateDataProvider.DataProviderException {
    // This method will extract the key values and pass them to the data provider
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
    Entity entity;
    try {
      entity = dataProvider.read(entitySet, resourceEntitySet.getKeyPredicates());
      return entity;
    } catch (NotFoundDataProviderException e) {
      // If no entity was found for the given key we throw an exception.
      throw e;
    } catch (Exception e) {
      throw new OdataHibernateDataProvider.DataProviderException("OdataHibernateProcessor.readEntityInternal",e);
    }
  }

  private EdmEntitySet getEdmEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    /*
     * To get the entity set we have to interpret all URI segments
     */
    if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
      throw new ODataApplicationException("Invalid resource type for first segment.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    /*
     * Here we should interpret the whole URI but in this example we do not support navigation so we throw an exception
     */

    final UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
    if (uriResource.getTypeFilterOnCollection() != null || uriResource.getTypeFilterOnEntry() != null) {
      throw new ODataApplicationException("Type filters are not supported.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
    return uriResource.getEntitySet();
  }

  private ContextURL getContextUrl(final EdmEntitySet entitySet, final boolean isSingleEntity,
      final ExpandOption expand, final SelectOption select, final String navOrPropertyPath)
      throws SerializerException {

    return ContextURL.with().entitySet(entitySet)
        .selectList(odata.createUriHelper().buildContextURLSelectList(entitySet.getEntityType(), expand, select))
        .suffix(isSingleEntity ? Suffix.ENTITY : null)
        .navOrPropertyPath(navOrPropertyPath)
        .build();
  }

  @Override
  public void updatePrimitive(final ODataRequest request, final ODataResponse response,
                              final UriInfo uriInfo, final ContentType requestFormat,
                              final ContentType responseFormat)
          throws ODataApplicationException, DeserializerException, SerializerException {
    throw new ODataApplicationException("Primitive property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deletePrimitive(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(response, uriInfo);
  }

  @Override
  public void deletePrimitiveCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(response, uriInfo);
  }

  @Override
  public void updateComplex(final ODataRequest request, final ODataResponse response,
                            final UriInfo uriInfo, final ContentType requestFormat,
                            final ContentType responseFormat)
          throws ODataApplicationException, DeserializerException, SerializerException {
    throw new ODataApplicationException("Complex property update is not supported yet.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(response, uriInfo);
  }

  @Override
  public void deleteComplexCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(response, uriInfo);
  }




  @Override
  public void updateEntity(final ODataRequest request, final ODataResponse response,
                           final UriInfo uriInfo, final ContentType requestFormat,
                           final ContentType responseFormat)
          throws ODataApplicationException, DeserializerException, SerializerException {



    blockNavigation(uriInfo);
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
    ODataDeserializer deserializer=odata.createDeserializer(ODataFormat.fromContentType(requestFormat));
    Entity requestEntity=deserializer.entity(request.getBody(), resourceEntitySet.getEntitySet().getEntityType());
    try {
      dataProvider.update(requestEntity,resourceEntitySet.getEntitySet(), resourceEntitySet.getKeyPredicates());
    } catch (NotFoundDataProviderException e) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    } catch (Exception e) {
      throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
    }
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }






  private void blockNavigation(final UriInfo uriInfo) throws ODataApplicationException {
    final List<UriResource> parts = uriInfo.asUriInfoResource().getUriResourceParts();
    if (parts.size() > 2
        || parts.size() == 2
            && parts.get(1).getKind() != UriResourceKind.count
            && parts.get(1).getKind() != UriResourceKind.value) {
      throw new ODataApplicationException("Invalid resource type.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
  }

  protected void validateOptions(final UriInfoResource uriInfo) throws ODataApplicationException {
    if (uriInfo.getCountOption() != null
        || !uriInfo.getCustomQueryOptions().isEmpty()
        || uriInfo.getFilterOption() != null
        || uriInfo.getIdOption() != null
        || uriInfo.getOrderByOption() != null
        || uriInfo.getSearchOption() != null
        || uriInfo.getSkipOption() != null
        || uriInfo.getSkipTokenOption() != null
        || uriInfo.getTopOption() != null) {
      throw new ODataApplicationException("Not all of the specified options are supported.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
  }

  private void validatePath(final UriInfoResource uriInfo) throws ODataApplicationException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    for (final UriResource segment : resourcePaths.subList(1, resourcePaths.size())) {
      final UriResourceKind kind = segment.getKind();
      if (kind != UriResourceKind.primitiveProperty
          && kind != UriResourceKind.complexProperty
          && kind != UriResourceKind.count
          && kind != UriResourceKind.value) {
        throw new ODataApplicationException("Invalid resource type.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
      }
    }
  }

  private void deleteProperty(final ODataResponse response, final UriInfo uriInfo) throws ODataApplicationException {
    final UriInfoResource resource = uriInfo.asUriInfoResource();
    validatePath(resource);

    final List<UriResource> resourceParts = resource.getUriResourceParts();
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
    final List<String> path = getPropertyPath(resourceParts);

    final EdmProperty edmProperty = ((UriResourceProperty) resourceParts.get(path.size())).getProperty();
    final Property property = getPropertyData(resourceEntitySet, path);

    if (edmProperty.isNullable() == null || edmProperty.isNullable()) {
      property.setValue(property.getValueType(), edmProperty.isCollection() ? Collections.emptyList() : null);
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    } else {
      throw new ODataApplicationException("Not nullable.", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
    }
  }

  private Property getPropertyData(final UriResourceEntitySet resourceEntitySet, final List<String> path)
      throws ODataApplicationException {
    Entity entity=null;
    try {
      entity = dataProvider.read(resourceEntitySet.getEntitySet(), resourceEntitySet.getKeyPredicates());
    } catch (Exception e) {
      throw new ODataApplicationException(" dataProvider.read ",HttpStatusCode.BAD_REQUEST.getStatusCode(),
          Locale.ROOT,e);
    }
    if (entity == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      Property property = entity.getProperty(path.get(0));
      for (final String name : path.subList(1, path.size())) {
        if (property != null && (property.isLinkedComplex() || property.isComplex())) {
          final List<Property> complex = property.isLinkedComplex() ?
              property.asLinkedComplex().getValue() : property.asComplex();
          property = null;
          for (final Property innerProperty : complex) {
            if (innerProperty.getName().equals(name)) {
              property = innerProperty;
              break;
            }
          }
        }
      }
      return property;
    }
  }
  private List<String> getPropertyPath(final List<UriResource> path) {
    List<String> result = new LinkedList<String>();
    int index = 1;
    while (index < path.size() && path.get(index) instanceof UriResourceProperty) {
      result.add(((UriResourceProperty) path.get(index)).getProperty().getName());
      index++;
    }
    return result;
  }

  @Override
  public void readComplexCollection(ODataRequest request,
      ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, SerializerException {
    throw new ODataApplicationException("readComplexCollection",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void updateComplexCollection(ODataRequest request,
      ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException,
      DeserializerException, SerializerException {
    throw new ODataApplicationException("updateComplexCollection",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void readPrimitiveCollection(ODataRequest request,
      ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, SerializerException {
    throw new ODataApplicationException("readPrimitiveCollection",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void updatePrimitiveCollection(ODataRequest request,
      ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException,
      DeserializerException, SerializerException {
    throw new ODataApplicationException("updatePrimitiveCollection",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);

  }




}