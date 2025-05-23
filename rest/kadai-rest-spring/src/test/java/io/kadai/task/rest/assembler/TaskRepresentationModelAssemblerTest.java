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

package io.kadai.task.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.classification.rest.models.ClassificationSummaryRepresentationModel;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskCustomIntField;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.AttachmentImpl;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.rest.models.AttachmentRepresentationModel;
import io.kadai.task.rest.models.ObjectReferenceRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.rest.models.WorkbasketSummaryRepresentationModel;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@KadaiSpringBootTest
class TaskRepresentationModelAssemblerTest {

  TaskService taskService;
  WorkbasketService workbasketService;
  ClassificationService classificationService;
  TaskRepresentationModelAssembler assembler;

  @Autowired
  TaskRepresentationModelAssemblerTest(
      TaskService taskService,
      WorkbasketService workbasketService,
      ClassificationService classificationService,
      TaskRepresentationModelAssembler assembler) {
    this.taskService = taskService;
    this.workbasketService = workbasketService;
    this.classificationService = classificationService;
    this.assembler = assembler;
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelToEntity() throws Exception {
    // given
    ObjectReferenceRepresentationModel primaryObjRef = new ObjectReferenceRepresentationModel();
    primaryObjRef.setId("abc");
    WorkbasketSummaryRepresentationModel workbasketResource =
        new WorkbasketSummaryRepresentationModel();
    workbasketResource.setWorkbasketId("workbasketId");
    ClassificationSummaryRepresentationModel classificationSummary =
        new ClassificationSummaryRepresentationModel();
    classificationSummary.setKey("keyabc");
    classificationSummary.setDomain("DOMAIN_A");
    classificationSummary.setType("MANUAL");
    AttachmentRepresentationModel attachment = new AttachmentRepresentationModel();
    attachment.setClassificationSummary(classificationSummary);
    attachment.setAttachmentId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskRepresentationModel repModel = new TaskRepresentationModel();
    repModel.setTaskId("taskId");
    repModel.setExternalId("externalId");
    repModel.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setName("name");
    repModel.setCreator("creator");
    repModel.setDescription("desc");
    repModel.setNote("note");
    repModel.setManualPriority(123);
    repModel.setPriority(123);
    repModel.setState(TaskState.READY);
    repModel.setNumberOfComments(2);
    repModel.setClassificationSummary(classificationSummary);
    repModel.setWorkbasketSummary(workbasketResource);
    repModel.setBusinessProcessId("businessProcessId");
    repModel.setParentBusinessProcessId("parentBusinessProcessId");
    repModel.setOwner("owner");
    repModel.setOwnerLongName("ownerLongName");
    repModel.setPrimaryObjRef(primaryObjRef);
    repModel.setRead(true);
    repModel.setTransferred(true);
    repModel.setReopened(true);
    repModel.setCustomAttributes(List.of(TaskRepresentationModel.CustomAttribute.of("abc", "def")));
    repModel.setCallbackInfo(List.of(TaskRepresentationModel.CustomAttribute.of("ghi", "jkl")));
    repModel.setAttachments(List.of(attachment));
    repModel.setGroupByCount(0);
    repModel.setCustom1("custom1");
    repModel.setCustom2("custom2");
    repModel.setCustom3("custom3");
    repModel.setCustom4("custom4");
    repModel.setCustom5("custom5");
    repModel.setCustom6("custom6");
    repModel.setCustom7("custom7");
    repModel.setCustom8("custom8");
    repModel.setCustom9("custom9");
    repModel.setCustom10("custom10");
    repModel.setCustom11("custom11");
    repModel.setCustom12("custom12");
    repModel.setCustom13("custom13");
    repModel.setCustom14("custom14");
    repModel.setCustom15("custom15");
    repModel.setCustom16("custom16");
    repModel.setCustomInt1(1);
    repModel.setCustomInt2(2);
    repModel.setCustomInt3(3);
    repModel.setCustomInt4(4);
    repModel.setCustomInt5(5);
    repModel.setCustomInt6(6);
    repModel.setCustomInt7(7);
    repModel.setCustomInt8(8);
    // when
    Task task = assembler.toEntityModel(repModel);
    // then
    testEquality(task, repModel);
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelWithoutWorkbasketSummaryToEntity()
      throws Exception {
    // given
    ObjectReferenceRepresentationModel primaryObjRef = new ObjectReferenceRepresentationModel();
    primaryObjRef.setId("abc");
    ClassificationSummaryRepresentationModel classificationSummary =
        new ClassificationSummaryRepresentationModel();
    classificationSummary.setKey("keyabc");
    classificationSummary.setDomain("DOMAIN_A");
    classificationSummary.setType("MANUAL");
    AttachmentRepresentationModel attachment = new AttachmentRepresentationModel();
    attachment.setClassificationSummary(classificationSummary);
    attachment.setAttachmentId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskRepresentationModel repModel = new TaskRepresentationModel();
    repModel.setTaskId("taskId");
    repModel.setExternalId("externalId");
    repModel.setClassificationSummary(classificationSummary);
    repModel.setPrimaryObjRef(primaryObjRef);
    // when
    Task task = assembler.toEntityModel(repModel);
    // then
    assertThat(repModel.getWorkbasketSummary()).isNull();
    assertThat(task.getWorkbasketSummary())
        .isNotNull()
        .hasAllNullFieldsOrPropertiesExcept(
            "markedForDeletion", "custom1", "custom2", "custom3", "custom4");
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingEntityToRepresentationModel()
      throws Exception {
    // given
    ObjectReferenceImpl primaryObjRef = new ObjectReferenceImpl();
    primaryObjRef.setId("abc");
    final Workbasket workbasket = workbasketService.newWorkbasket("key", "domain");
    ClassificationSummary classification =
        classificationService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    attachment.setClassificationSummary(classification);
    attachment.setId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskImpl task = (TaskImpl) taskService.newTask();
    task.setId("taskId");
    task.setExternalId("externalId");
    task.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setName("name");
    task.setCreator("creator");
    task.setDescription("desc");
    task.setNote("note");
    task.setPriority(123);
    task.setManualPriority(-5);
    task.setState(TaskState.READY);
    task.setNumberOfComments(2);
    task.setClassificationSummary(classification);
    task.setWorkbasketSummary(workbasket.asSummary());
    task.setBusinessProcessId("businessProcessId");
    task.setParentBusinessProcessId("parentBusinessProcessId");
    task.setOwner("owner");
    task.setOwnerLongName("ownerLongName");
    task.setPrimaryObjRef(primaryObjRef);
    task.setRead(true);
    task.setTransferred(true);
    task.setReopened(true);
    task.setGroupByCount(0);
    task.setCustomAttributeMap(Map.of("abc", "def"));
    task.setCallbackInfo(Map.of("ghi", "jkl"));
    task.setAttachments(List.of(attachment));
    task.setCustomField(TaskCustomField.CUSTOM_1, "custom1");
    task.setCustomField(TaskCustomField.CUSTOM_2, "custom2");
    task.setCustomField(TaskCustomField.CUSTOM_3, "custom3");
    task.setCustomField(TaskCustomField.CUSTOM_4, "custom4");
    task.setCustomField(TaskCustomField.CUSTOM_5, "custom5");
    task.setCustomField(TaskCustomField.CUSTOM_6, "custom6");
    task.setCustomField(TaskCustomField.CUSTOM_7, "custom7");
    task.setCustomField(TaskCustomField.CUSTOM_8, "custom8");
    task.setCustomField(TaskCustomField.CUSTOM_9, "custom9");
    task.setCustomField(TaskCustomField.CUSTOM_10, "custom10");
    task.setCustomField(TaskCustomField.CUSTOM_11, "custom11");
    task.setCustomField(TaskCustomField.CUSTOM_12, "custom12");
    task.setCustomField(TaskCustomField.CUSTOM_13, "custom13");
    task.setCustomField(TaskCustomField.CUSTOM_14, "custom14");
    task.setCustomField(TaskCustomField.CUSTOM_15, "custom15");
    task.setCustomField(TaskCustomField.CUSTOM_16, "custom16");
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_1, 1);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_2, 2);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_3, 3);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_4, 4);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_5, 5);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_6, 6);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_7, 7);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_8, 8);
    // when
    TaskRepresentationModel repModel = assembler.toModel(task);
    // then
    testEquality(task, repModel);
    testLinks(repModel);
  }

  @Test
  void should_Equal_When_ComparingEntityWithConvertedEntity() throws InvalidArgumentException {
    // given
    ObjectReferenceImpl primaryObjRef = new ObjectReferenceImpl();
    primaryObjRef.setId("abc");
    final WorkbasketSummary workbasket =
        workbasketService.newWorkbasket("key", "domain").asSummary();
    ClassificationSummary classification =
        classificationService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    attachment.setClassificationSummary(classification);
    attachment.setId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskImpl task = (TaskImpl) taskService.newTask();
    task.setId("taskId");
    task.setExternalId("externalId");
    task.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setName("name");
    task.setCreator("creator");
    task.setDescription("desc");
    task.setNote("note");
    task.setPriority(123);
    task.setManualPriority(123);
    task.setState(TaskState.READY);
    task.setNumberOfComments(2);
    task.setClassificationSummary(classification);
    task.setWorkbasketSummary(workbasket);
    task.setBusinessProcessId("businessProcessId");
    task.setParentBusinessProcessId("parentBusinessProcessId");
    task.setOwner("owner");
    task.setOwnerLongName("ownerLongName");
    task.setPrimaryObjRef(primaryObjRef);
    task.setRead(true);
    task.setTransferred(true);
    task.setReopened(true);
    task.setGroupByCount(0);
    task.setCustomAttributeMap(Map.of("abc", "def"));
    task.setCallbackInfo(Map.of("ghi", "jkl"));
    task.setAttachments(List.of(attachment));
    task.setCustom1("custom1");
    task.setCustom2("custom2");
    task.setCustom3("custom3");
    task.setCustom4("custom4");
    task.setCustom5("custom5");
    task.setCustom6("custom6");
    task.setCustom7("custom7");
    task.setCustom8("custom8");
    task.setCustom9("custom9");
    task.setCustom10("custom10");
    task.setCustom11("custom11");
    task.setCustom12("custom12");
    task.setCustom13("custom13");
    task.setCustom14("custom14");
    task.setCustom15("custom15");
    task.setCustom16("custom16");
    task.setCustomInt1(1);
    task.setCustomInt2(2);
    task.setCustomInt3(3);
    task.setCustomInt4(4);
    task.setCustomInt5(5);
    task.setCustomInt6(6);
    task.setCustomInt7(7);
    task.setCustomInt8(8);
    // when
    TaskRepresentationModel repModel = assembler.toModel(task);
    Task task2 = assembler.toEntityModel(repModel);
    // then
    assertThat(task).hasNoNullFieldsOrProperties().isNotSameAs(task2).isEqualTo(task2);
  }

  private void testEquality(Task task, TaskRepresentationModel repModel) throws Exception {
    TaskSummaryRepresentationModelAssemblerTest.testEquality(task, repModel);

    testEqualityCustomAttributes(task.getCustomAttributeMap(), repModel.getCustomAttributes());
    testEqualityCustomAttributes(task.getCallbackInfo(), repModel.getCallbackInfo());
    testEqualityAttachments(task.getAttachments(), repModel.getAttachments());
  }

  private void testEqualityCustomAttributes(
      Map<String, String> customAttributes,
      List<TaskRepresentationModel.CustomAttribute> repModelAttributes) {
    assertThat(repModelAttributes).hasSize(customAttributes.size());
    repModelAttributes.forEach(
        attribute ->
            assertThat(attribute.getValue()).isEqualTo(customAttributes.get(attribute.getKey())));
  }

  private void testEqualityAttachments(
      List<Attachment> attachments, List<AttachmentRepresentationModel> repModels) {
    String[] objects = attachments.stream().map(Attachment::getId).toArray(String[]::new);

    // Anything else should be be tested in AttachmentResourceAssemblerTest
    assertThat(repModels)
        .hasSize(attachments.size())
        .extracting(AttachmentRepresentationModel::getAttachmentId)
        .containsExactlyInAnyOrder(objects);
  }

  private void testLinks(TaskRepresentationModel repModel) {
    assertThat(repModel.getLinks()).hasSize(1);
    assertThat(repModel.getRequiredLink("self").getHref())
        .isEqualTo(RestEndpoints.URL_TASKS_ID.replaceAll("\\{.*}", repModel.getTaskId()));
  }
}
