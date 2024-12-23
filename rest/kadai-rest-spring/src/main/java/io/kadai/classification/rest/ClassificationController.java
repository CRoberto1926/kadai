/*
 * Copyright [2024] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.classification.rest;

import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.classification.api.ClassificationQuery;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.exceptions.ClassificationAlreadyExistException;
import io.kadai.classification.api.exceptions.ClassificationInUseException;
import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.classification.rest.assembler.ClassificationRepresentationModelAssembler;
import io.kadai.classification.rest.assembler.ClassificationSummaryRepresentationModelAssembler;
import io.kadai.classification.rest.models.ClassificationRepresentationModel;
import io.kadai.classification.rest.models.ClassificationSummaryPagedRepresentationModel;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.function.BiConsumer;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all {@link Classification} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ClassificationController {

  private final ClassificationService classificationService;
  private final ClassificationRepresentationModelAssembler modelAssembler;
  private final ClassificationSummaryRepresentationModelAssembler summaryModelAssembler;

  @Autowired
  ClassificationController(
      ClassificationService classificationService,
      ClassificationRepresentationModelAssembler modelAssembler,
      ClassificationSummaryRepresentationModelAssembler summaryModelAssembler) {
    this.classificationService = classificationService;
    this.modelAssembler = modelAssembler;
    this.summaryModelAssembler = summaryModelAssembler;
  }

  /**
   * This endpoint retrieves a list of existing Classifications. Filters can be applied.
   *
   * @title Get a list of all Classifications
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return the classifications with the given filter, sort and paging options.
   */
  @Operation(
      summary = "Get a list of all Classifications",
      description =
          "This endpoint retrieves a list of existing Classifications. Filters can be applied.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the classifications with the given filter, sort and paging options.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(implementation = ClassificationSummaryPagedRepresentationModel.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_CLASSIFICATIONS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ClassificationSummaryPagedRepresentationModel> getClassifications(
      HttpServletRequest request,
      @ParameterObject final ClassificationQueryFilterParameter filterParameter,
      @ParameterObject final ClassificationQuerySortParameter sortParameter,
      @ParameterObject
          final QueryPagingParameter<ClassificationSummary, ClassificationQuery> pagingParameter) {

    QueryParamsValidator.validateParams(
        request,
        ClassificationQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    final ClassificationQuery query = classificationService.createClassificationQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);
    List<ClassificationSummary> classificationSummaries = pagingParameter.apply(query);

    return ResponseEntity.ok(
        summaryModelAssembler.toPagedModel(
            classificationSummaries, pagingParameter.getPageMetadata()));
  }

  /**
   * This endpoint retrieves a single Classification.
   *
   * @param classificationId the Id of the requested Classification.
   * @return the requested classification
   * @throws ClassificationNotFoundException if the requested classification is not found.
   * @title Get a single Classification
   */
  @Operation(
      summary = "Get a single Classification",
      description = "This endpoint retrieves a single Classification.",
      parameters = {
        @Parameter(
            name = "classificationId",
            description = "the Id of the requested Classification.",
            example = "CLI:100000000000000000000000000000000009",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the requested classification",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = ClassificationRepresentationModel.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID, produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> getClassification(
      @PathVariable("classificationId") String classificationId)
      throws ClassificationNotFoundException {
    Classification classification = classificationService.getClassification(classificationId);
    return ResponseEntity.ok(modelAssembler.toModel(classification));
  }

  /**
   * This endpoint creates a new Classification.
   *
   * @title Create a new Classification
   * @param repModel the Classification which should be created.
   * @return The inserted Classification
   * @throws NotAuthorizedException if the current user is not allowed to create a Classification.
   * @throws ClassificationAlreadyExistException if the new Classification already exists. This
   *     means that a Classification with the requested key and domain already exist.
   * @throws DomainNotFoundException if the domain within the new Classification does not exist.
   * @throws InvalidArgumentException if the new Classification does not contain all relevant
   *     information.
   * @throws MalformedServiceLevelException if the {@code serviceLevel} property does not comply *
   *     with the ISO 8601 specification
   */
  @Operation(
      summary = "Create a new Classification",
      description = "This endpoint creates a new Classification.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the Classification which should be created.",
              content =
                  @Content(
                      schema = @Schema(implementation = ClassificationRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                      {
                                        "key" : "Key0815casdgdgh",
                                        "domain" : "DOMAIN_B",
                                        "priority" : 0,
                                        "serviceLevel" : "P1D",
                                        "type" : "TASK"
                                      }"""))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "The inserted Classification",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = ClassificationRepresentationModel.class))
            })
      })
  @PostMapping(path = RestEndpoints.URL_CLASSIFICATIONS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> createClassification(
      @RequestBody ClassificationRepresentationModel repModel)
      throws ClassificationAlreadyExistException,
          DomainNotFoundException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException {
    Classification classification = modelAssembler.toEntityModel(repModel);
    classification = classificationService.createClassification(classification);

    return ResponseEntity.status(HttpStatus.CREATED).body(modelAssembler.toModel(classification));
  }

  /**
   * This endpoint updates a Classification.
   *
   * @title Update a Classification
   * @param classificationId the Id of the Classification which should be updated.
   * @param resource the new Classification for the requested id.
   * @return the updated Classification
   * @throws NotAuthorizedException if the current user is not authorized to update a Classification
   * @throws ClassificationNotFoundException if the requested Classification is not found
   * @throws ConcurrencyException if the requested Classification Id has been modified in the
   *     meantime by a different process.
   * @throws InvalidArgumentException if the Id in the path and in the request body does not match
   * @throws MalformedServiceLevelException if the {@code serviceLevel} property does not comply *
   *     with the ISO 8601 specification
   */
  @Operation(
      summary = "Update a Classification",
      description = "This endpoint updates a Classification.",
      parameters = {
        @Parameter(
            name = "classificationId",
            description = "the Id of the Classification which should be updated.",
            example = "CLI:100000000000000000000000000000000009",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the new Classification for the requested id.",
              content =
                  @Content(
                      schema = @Schema(implementation = ClassificationRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                      {
                                        "classificationId" : \
                                      "CLI:100000000000000000000000000000000009",
                                        "key" : "L140101",
                                        "applicationEntryPoint" : "",
                                        "category" : "EXTERNAL",
                                        "domain" : "DOMAIN_A",
                                        "name" : "new name",
                                        "parentId" : "",
                                        "parentKey" : "",
                                        "priority" : 2,
                                        "serviceLevel" : "P2D",
                                        "type" : "TASK",
                                        "custom1" : "VNR",
                                        "custom2" : "",
                                        "custom3" : "",
                                        "custom4" : "",
                                        "custom5" : "",
                                        "custom6" : "",
                                        "custom7" : "",
                                        "custom8" : "",
                                        "isValidInDomain" : true,
                                        "created" : "2018-02-01T12:00:00.000Z",
                                        "modified" : "2018-02-01T12:00:00.000Z",
                                        "description" : "Zustimmungserklärung"
                                      }"""))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the updated Classification",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = ClassificationRepresentationModel.class))
            })
      })
  @PutMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> updateClassification(
      @PathVariable("classificationId") String classificationId,
      @RequestBody ClassificationRepresentationModel resource)
      throws ClassificationNotFoundException,
          ConcurrencyException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException {
    if (!classificationId.equals(resource.getClassificationId())) {
      throw new InvalidArgumentException(
          String.format(
              "ClassificationId ('%s') of the URI is not identical"
                  + " with the classificationId ('%s') of the object in the payload.",
              classificationId, resource.getClassificationId()));
    }
    Classification classification = modelAssembler.toEntityModel(resource);
    classification = classificationService.updateClassification(classification);

    return ResponseEntity.ok(modelAssembler.toModel(classification));
  }

  /**
   * This endpoint deletes a requested Classification if possible.
   *
   * @title Delete a Classification
   * @param classificationId the requested Classification Id which should be deleted
   * @return no content
   * @throws ClassificationNotFoundException if the requested Classification could not be found
   * @throws ClassificationInUseException if there are tasks existing referring to the requested
   *     Classification
   * @throws NotAuthorizedException if the user is not authorized to delete a Classification
   */
  @Operation(
      summary = "Delete a Classification",
      description = "This endpoint deletes a requested Classification if possible.",
      parameters = {
        @Parameter(
            name = "classificationId",
            description = "the requested Classification Id which should be deleted",
            example = "CLI:100000000000000000000000000000000010",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "204",
            content = {@Content(schema = @Schema())})
      })
  @DeleteMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> deleteClassification(
      @PathVariable("classificationId") String classificationId)
      throws ClassificationNotFoundException, ClassificationInUseException, NotAuthorizedException {
    classificationService.deleteClassification(classificationId);
    return ResponseEntity.noContent().build();
  }

  enum ClassificationQuerySortBy implements QuerySortBy<ClassificationQuery> {
    APPLICATION_ENTRY_POINT(ClassificationQuery::orderByApplicationEntryPoint),
    DOMAIN(ClassificationQuery::orderByDomain),
    KEY(ClassificationQuery::orderByKey),
    CATEGORY(ClassificationQuery::orderByCategory),
    CUSTOM_1((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_1, sort)),
    CUSTOM_2((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_2, sort)),
    CUSTOM_3((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_3, sort)),
    CUSTOM_4((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_4, sort)),
    CUSTOM_5((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_5, sort)),
    CUSTOM_6((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_6, sort)),
    CUSTOM_7((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_7, sort)),
    CUSTOM_8((q, sort) -> q.orderByCustomAttribute(ClassificationCustomField.CUSTOM_8, sort)),
    NAME(ClassificationQuery::orderByName),
    PARENT_ID(ClassificationQuery::orderByParentId),
    PARENT_KEY(ClassificationQuery::orderByParentKey),
    PRIORITY(ClassificationQuery::orderByPriority),
    SERVICE_LEVEL(ClassificationQuery::orderByServiceLevel);

    private final BiConsumer<ClassificationQuery, SortDirection> consumer;

    ClassificationQuerySortBy(BiConsumer<ClassificationQuery, SortDirection> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void applySortByForQuery(ClassificationQuery query, SortDirection sortDirection) {
      consumer.accept(query, sortDirection);
    }
  }

  // Unfortunately this class is necessary, since spring can not inject the generic 'sort-by'
  // parameter from the super class.
  public static class ClassificationQuerySortParameter
      extends QuerySortParameter<ClassificationQuery, ClassificationQuerySortBy> {

    @ConstructorProperties({"sort-by", "order"})
    public ClassificationQuerySortParameter(
        List<ClassificationQuerySortBy> sortBy, List<SortDirection> order)
        throws InvalidArgumentException {
      super(sortBy, order);
    }

    // this getter is necessary for the documentation!
    @Override
    public List<ClassificationQuerySortBy> getSortBy() {
      return super.getSortBy();
    }
  }
}
