/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.workbasket.rest;

import static io.kadai.common.internal.util.CheckedFunction.wrapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.workbasket.api.WorkbasketQuery;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.models.WorkbasketImpl;
import io.kadai.workbasket.rest.assembler.WorkbasketAccessItemRepresentationModelAssembler;
import io.kadai.workbasket.rest.assembler.WorkbasketDefinitionRepresentationModelAssembler;
import io.kadai.workbasket.rest.assembler.WorkbasketRepresentationModelAssembler;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketDefinitionCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketDefinitionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketRepresentationModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Controller for all {@link WorkbasketDefinitionRepresentationModel} related endpoints. */
@RestController
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WorkbasketDefinitionController implements WorkbasketDefinitionApi {

  private final WorkbasketService workbasketService;
  private final WorkbasketDefinitionRepresentationModelAssembler workbasketDefinitionAssembler;
  private final WorkbasketRepresentationModelAssembler workbasketAssembler;
  private final WorkbasketAccessItemRepresentationModelAssembler accessItemAssembler;
  private final ObjectMapper mapper;

  @Autowired
  WorkbasketDefinitionController(
      WorkbasketService workbasketService,
      WorkbasketDefinitionRepresentationModelAssembler workbasketDefinitionAssembler,
      WorkbasketRepresentationModelAssembler workbasketAssembler,
      WorkbasketAccessItemRepresentationModelAssembler accessItemAssembler,
      ObjectMapper mapper) {
    this.workbasketService = workbasketService;
    this.workbasketDefinitionAssembler = workbasketDefinitionAssembler;
    this.workbasketAssembler = workbasketAssembler;
    this.accessItemAssembler = accessItemAssembler;
    this.mapper = mapper;
  }

  @GetMapping(path = RestEndpoints.URL_WORKBASKET_DEFINITIONS)
  public ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel> exportWorkbaskets(
      @RequestParam(value = "domain", required = false) String[] domain) {
    WorkbasketQuery query = workbasketService.createWorkbasketQuery();
    Optional.ofNullable(domain).ifPresent(query::domainIn);

    List<WorkbasketSummary> workbasketSummaryList = query.list();

    WorkbasketDefinitionCollectionRepresentationModel pageModel =
        workbasketSummaryList.stream()
            .map(WorkbasketSummary::getId)
            .map(wrapping(workbasketService::getWorkbasket))
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(), workbasketDefinitionAssembler::toKadaiCollectionModel));

    return ResponseEntity.ok(pageModel);
  }

  @PostMapping(
      path = RestEndpoints.URL_WORKBASKET_DEFINITIONS,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> importWorkbaskets(@RequestParam("file") MultipartFile file)
      throws IOException,
          DomainNotFoundException,
          InvalidArgumentException,
          WorkbasketAlreadyExistException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          ConcurrencyException,
          NotAuthorizedOnWorkbasketException,
          NotAuthorizedException {
    WorkbasketDefinitionCollectionRepresentationModel definitions =
        mapper.readValue(
            file.getInputStream(),
            new TypeReference<WorkbasketDefinitionCollectionRepresentationModel>() {});

    // key: logical ID
    // value: system ID (in database)
    Map<String, String> systemIds =
        workbasketService.createWorkbasketQuery().list().stream()
            .collect(Collectors.toMap(this::logicalId, WorkbasketSummary::getId));
    checkForDuplicates(definitions.getContent());

    // key: old system ID
    // value: system ID
    Map<String, String> idConversion = new HashMap<>();

    // STEP 1: update or create workbaskets from the import
    for (WorkbasketDefinitionRepresentationModel definition : definitions.getContent()) {
      Workbasket importedWb = workbasketAssembler.toEntityModel(definition.getWorkbasket());
      String newId;
      WorkbasketImpl wbWithoutId = (WorkbasketImpl) removeId(importedWb);
      if (systemIds.containsKey(logicalId(importedWb))) {
        Workbasket modifiedWb =
            workbasketService.getWorkbasket(importedWb.getKey(), importedWb.getDomain());
        wbWithoutId.setModified(modifiedWb.getModified());
        workbasketService.updateWorkbasket(wbWithoutId);

        newId = systemIds.get(logicalId(importedWb));
      } else {
        newId = workbasketService.createWorkbasket(wbWithoutId).getId();
      }

      // Since we would have a n² runtime when doing a lookup and updating the access items we
      // decided to
      // simply delete all existing accessItems and create new ones.
      boolean authenticated =
          definition.getAuthorizations().stream()
              .anyMatch(
                  access ->
                      (access.getWorkbasketId().equals(importedWb.getId()))
                          && (access.getWorkbasketKey().equals(importedWb.getKey())));
      if (!authenticated && !definition.getAuthorizations().isEmpty()) {
        throw new InvalidArgumentException(
            "The given Authentications for Workbasket "
                + importedWb.getId()
                + " don't match in WorkbasketId and WorkbasketKey. "
                + "Please provide consistent WorkbasketDefinitions");
      }
      for (WorkbasketAccessItem accessItem : workbasketService.getWorkbasketAccessItems(newId)) {
        workbasketService.deleteWorkbasketAccessItem(accessItem.getId());
      }
      for (WorkbasketAccessItemRepresentationModel authorization : definition.getAuthorizations()) {
        authorization.setWorkbasketId(newId);
        workbasketService.createWorkbasketAccessItem(
            accessItemAssembler.toEntityModel(authorization));
      }
      idConversion.put(importedWb.getId(), newId);
    }

    // STEP 2: update distribution targets
    // This can not be done in step 1 because the system IDs are only known after step 1
    for (WorkbasketDefinitionRepresentationModel definition : definitions.getContent()) {
      List<String> distributionTargets = new ArrayList<>();
      for (String oldId : definition.getDistributionTargets()) {
        if (idConversion.containsKey(oldId)) {
          distributionTargets.add(idConversion.get(oldId));
        } else if (systemIds.containsValue(oldId)) {
          distributionTargets.add(oldId);
        } else {
          throw new InvalidArgumentException(
              String.format(
                  "invalid import state: Workbasket '%s' does not exist in the given import list",
                  oldId));
        }
      }

      workbasketService.setDistributionTargets(
          // no verification necessary since the workbasket was already imported in step 1.
          idConversion.get(definition.getWorkbasket().getWorkbasketId()), distributionTargets);
    }
    return ResponseEntity.noContent().build();
  }

  private Workbasket removeId(Workbasket importedWb) {
    WorkbasketRepresentationModel wbRes = workbasketAssembler.toModel(importedWb);
    wbRes.setWorkbasketId(null);
    return workbasketAssembler.toEntityModel(wbRes);
  }

  private void checkForDuplicates(Collection<WorkbasketDefinitionRepresentationModel> definitions)
      throws WorkbasketAlreadyExistException {
    Set<String> identifiers = new HashSet<>();
    for (WorkbasketDefinitionRepresentationModel definition : definitions) {
      String identifier = logicalId(workbasketAssembler.toEntityModel(definition.getWorkbasket()));
      if (identifiers.contains(identifier)) {
        throw new WorkbasketAlreadyExistException(
            definition.getWorkbasket().getKey(), definition.getWorkbasket().getDomain());
      }
      identifiers.add(identifier);
    }
  }

  private String logicalId(WorkbasketSummary workbasket) {
    return logicalId(workbasket.getKey(), workbasket.getDomain());
  }

  private String logicalId(String key, String domain) {
    return key + "|" + domain;
  }
}
