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

package io.kadai.workbasket.rest.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.kadai.common.rest.assembler.CollectionRepresentationModelAssembler;
import io.kadai.common.rest.assembler.PagedRepresentationModelAssembler;
import io.kadai.common.rest.models.PageMetadata;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;
import io.kadai.workbasket.rest.WorkbasketController;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemPagedRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemRepresentationModel;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Transforms {@link WorkbasketAccessItem} to its resource counterpart {@link
 * WorkbasketAccessItemRepresentationModel} and vice versa.
 */
@Component
public class WorkbasketAccessItemRepresentationModelAssembler
    implements CollectionRepresentationModelAssembler<
            WorkbasketAccessItem,
            WorkbasketAccessItemRepresentationModel,
            WorkbasketAccessItemCollectionRepresentationModel>,
        PagedRepresentationModelAssembler<
            WorkbasketAccessItem,
            WorkbasketAccessItemRepresentationModel,
            WorkbasketAccessItemPagedRepresentationModel> {

  private final WorkbasketService workbasketService;

  @Autowired
  public WorkbasketAccessItemRepresentationModelAssembler(WorkbasketService workbasketService) {
    this.workbasketService = workbasketService;
  }

  @NonNull
  @Override
  public WorkbasketAccessItemRepresentationModel toModel(@NonNull WorkbasketAccessItem wbAccItem) {
    WorkbasketAccessItemRepresentationModel repModel =
        new WorkbasketAccessItemRepresentationModel();
    repModel.setAccessId(wbAccItem.getAccessId());
    repModel.setWorkbasketId(wbAccItem.getWorkbasketId());
    repModel.setWorkbasketKey(wbAccItem.getWorkbasketKey());
    repModel.setAccessItemId(wbAccItem.getId());
    repModel.setAccessName(wbAccItem.getAccessName());
    repModel.setPermRead(wbAccItem.getPermission(WorkbasketPermission.READ));
    repModel.setPermReadTasks(wbAccItem.getPermission(WorkbasketPermission.READTASKS));
    repModel.setPermOpen(wbAccItem.getPermission(WorkbasketPermission.OPEN));
    repModel.setPermAppend(wbAccItem.getPermission(WorkbasketPermission.APPEND));
    repModel.setPermEditTasks(wbAccItem.getPermission(WorkbasketPermission.EDITTASKS));
    repModel.setPermTransfer(wbAccItem.getPermission(WorkbasketPermission.TRANSFER));
    repModel.setPermDistribute(wbAccItem.getPermission(WorkbasketPermission.DISTRIBUTE));
    repModel.setPermCustom1(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_1));
    repModel.setPermCustom2(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_2));
    repModel.setPermCustom3(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_3));
    repModel.setPermCustom4(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_4));
    repModel.setPermCustom5(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_5));
    repModel.setPermCustom6(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_6));
    repModel.setPermCustom7(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_7));
    repModel.setPermCustom8(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_8));
    repModel.setPermCustom9(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_9));
    repModel.setPermCustom10(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_10));
    repModel.setPermCustom11(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_11));
    repModel.setPermCustom12(wbAccItem.getPermission(WorkbasketPermission.CUSTOM_12));
    return repModel;
  }

  public WorkbasketAccessItem toEntityModel(WorkbasketAccessItemRepresentationModel repModel) {
    WorkbasketAccessItemImpl wbAccItemModel =
        (WorkbasketAccessItemImpl)
            workbasketService.newWorkbasketAccessItem(
                repModel.getWorkbasketId(), repModel.getAccessId());
    wbAccItemModel.setWorkbasketKey(repModel.getWorkbasketKey());
    wbAccItemModel.setAccessName(repModel.getAccessName());
    wbAccItemModel.setPermission(WorkbasketPermission.READ, repModel.isPermRead());
    wbAccItemModel.setPermission(WorkbasketPermission.READTASKS, repModel.isPermReadTasks());
    wbAccItemModel.setPermission(WorkbasketPermission.OPEN, repModel.isPermOpen());
    wbAccItemModel.setPermission(WorkbasketPermission.APPEND, repModel.isPermAppend());
    wbAccItemModel.setPermission(WorkbasketPermission.EDITTASKS, repModel.isPermEditTasks());
    wbAccItemModel.setPermission(WorkbasketPermission.TRANSFER, repModel.isPermTransfer());
    wbAccItemModel.setPermission(WorkbasketPermission.DISTRIBUTE, repModel.isPermDistribute());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_1, repModel.isPermCustom1());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_2, repModel.isPermCustom2());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_3, repModel.isPermCustom3());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_4, repModel.isPermCustom4());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_5, repModel.isPermCustom5());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_6, repModel.isPermCustom6());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_7, repModel.isPermCustom7());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_8, repModel.isPermCustom8());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_9, repModel.isPermCustom9());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_10, repModel.isPermCustom10());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_11, repModel.isPermCustom11());
    wbAccItemModel.setPermission(WorkbasketPermission.CUSTOM_12, repModel.isPermCustom12());
    wbAccItemModel.setId(repModel.getAccessItemId());
    return wbAccItemModel;
  }

  public WorkbasketAccessItemCollectionRepresentationModel
      toKadaiCollectionModelForSingleWorkbasket(
          String workbasketId, List<WorkbasketAccessItem> workbasketAccessItems)
          throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException {
    WorkbasketAccessItemCollectionRepresentationModel pageModel =
        toKadaiCollectionModel(workbasketAccessItems);
    pageModel.add(
        linkTo(methodOn(WorkbasketController.class).getWorkbasket(workbasketId))
            .withRel("workbasket"));
    return pageModel;
  }

  @Override
  public WorkbasketAccessItemCollectionRepresentationModel buildCollectionEntity(
      List<WorkbasketAccessItemRepresentationModel> content) {
    return new WorkbasketAccessItemCollectionRepresentationModel(content);
  }

  @Override
  public WorkbasketAccessItemPagedRepresentationModel buildPageableEntity(
      Collection<WorkbasketAccessItemRepresentationModel> content, PageMetadata pageMetadata) {
    return new WorkbasketAccessItemPagedRepresentationModel(content, pageMetadata);
  }
}
