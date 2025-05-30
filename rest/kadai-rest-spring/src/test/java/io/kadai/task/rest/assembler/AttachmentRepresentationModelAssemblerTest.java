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
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.internal.models.AttachmentImpl;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.rest.models.AttachmentRepresentationModel;
import io.kadai.task.rest.models.ObjectReferenceRepresentationModel;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Test for {@link AttachmentRepresentationModelAssembler}. */
@KadaiSpringBootTest
class AttachmentRepresentationModelAssemblerTest {

  private final AttachmentRepresentationModelAssembler assembler;
  private final ClassificationService classService;
  private final TaskService taskService;

  @Autowired
  AttachmentRepresentationModelAssemblerTest(
      AttachmentRepresentationModelAssembler assembler,
      ClassificationService classService,
      TaskService taskService) {
    this.assembler = assembler;
    this.classService = classService;
    this.taskService = taskService;
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelToEntity() {
    ObjectReferenceRepresentationModel reference = new ObjectReferenceRepresentationModel();
    reference.setId("abc");
    ClassificationSummaryRepresentationModel summary =
        new ClassificationSummaryRepresentationModel();
    summary.setKey("keyabc");
    summary.setDomain("DOMAIN_A");
    summary.setType("MANUAL");
    AttachmentRepresentationModel repModel = new AttachmentRepresentationModel();
    repModel.setCustomAttributes(Map.of("abc", "def"));
    repModel.setClassificationSummary(summary);
    repModel.setAttachmentId("id");
    repModel.setTaskId("taskId");
    repModel.setChannel("channel");
    repModel.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setObjectReference(reference);
    repModel.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));

    Attachment attachment = assembler.toEntityModel(repModel);

    testEquality(attachment, repModel);
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingEntityToRepresentationModel() {
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    ObjectReferenceImpl reference = new ObjectReferenceImpl();
    ClassificationSummary summary =
        classService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    reference.setId("abc");
    attachment.setCustomAttributeMap(Map.of("abc", "def"));
    attachment.setClassificationSummary(summary);
    attachment.setId("id");
    attachment.setTaskId("taskId");
    attachment.setChannel("channel");
    attachment.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    attachment.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    attachment.setObjectReference(reference);
    attachment.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));

    AttachmentRepresentationModel repModel = assembler.toModel(attachment);

    testEquality(attachment, repModel);
  }

  @Test
  void should_Equal_When_ComparingEntityWithConvertedEntity() {
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    ObjectReferenceImpl reference = new ObjectReferenceImpl();
    ClassificationSummary summary =
        classService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    reference.setId("abc");
    attachment.setCustomAttributeMap(Map.of("abc", "def"));
    attachment.setClassificationSummary(summary);
    attachment.setId("id");
    attachment.setTaskId("taskId");
    attachment.setChannel("channel");
    attachment.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    attachment.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    attachment.setObjectReference(reference);
    attachment.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));

    AttachmentRepresentationModel repModel = assembler.toModel(attachment);
    Attachment attachment2 = assembler.toEntityModel(repModel);

    assertThat(attachment)
        .hasNoNullFieldsOrProperties()
        .isNotSameAs(attachment2)
        .isEqualTo(attachment2);
  }

  void testEquality(Attachment attachment, AttachmentRepresentationModel repModel) {
    AttachmentSummaryRepresentationModelAssemblerTest.testEquality(attachment, repModel);

    assertThat(attachment.getCustomAttributeMap()).isEqualTo(repModel.getCustomAttributes());
  }
}
