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

package acceptance.taskcomment.get;

import static io.kadai.common.internal.util.CheckedConsumer.rethrowing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskComment;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiConfigurationModifier;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.TaskCommentBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.user.api.UserService;
import io.kadai.user.api.models.User;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.models.Workbasket;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@KadaiIntegrationTest
class GetTaskCommentAccTest {

  @KadaiInject TaskService taskService;
  @KadaiInject ClassificationService classificationService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject KadaiEngine kadaiEngine;

  Classification defaultClassification;
  Workbasket defaultWorkbasket;
  Task task1;
  Task task2;
  Task task3;

  @WithAccessId(user = "admin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassification =
        DefaultTestEntities.defaultTestClassification().buildAndStore(classificationService);
    defaultWorkbasket =
        DefaultTestEntities.defaultTestWorkbasket().buildAndStore(workbasketService);
    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasket.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    task1 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);
    task2 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);
    task3 =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);

    User userWithName = kadaiEngine.getUserService().newUser();
    userWithName.setId("user-1-1");
    userWithName.setFirstName("Max");
    userWithName.setLastName("Mustermann");
    userWithName.setFullName("Max Mustermann");
    kadaiEngine.getUserService().createUser(userWithName);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnTaskComments_For_TaskId() throws Exception {
    TaskComment comment1 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task3.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    TaskComment comment2 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task3.getId())
            .textField("Text2")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    TaskComment comment3 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task3.getId())
            .textField("Text3")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);

    List<TaskComment> taskComments = taskService.getTaskComments(task3.getId());

    assertThat(taskComments).containsExactlyInAnyOrder(comment1, comment2, comment3);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnEmptyList_When_TaskCommentsDontExist() throws Exception {
    assertThat(taskService.getTaskComments(task2.getId())).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_FailToReturnTaskComments_When_TaskIsNotVisible() {
    ThrowingCallable call = () -> taskService.getTaskComments(task1.getId());
    NotAuthorizedOnWorkbasketException e =
        catchThrowableOfType(NotAuthorizedOnWorkbasketException.class, call);

    assertThat(e.getCurrentUserId()).isEqualTo("user-1-2");
    assertThat(e.getRequiredPermissions())
        .containsExactly(WorkbasketPermission.READ, WorkbasketPermission.READTASKS);
    assertThat(e.getWorkbasketId()).isEqualTo(defaultWorkbasket.getId());
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_FailToReturnTaskComment_When_TaskIsNotVisible() throws Exception {
    TaskComment comment =
        TaskCommentBuilder.newTaskComment()
            .taskId(task1.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService, "user-1-1");

    ThrowingCallable call = () -> taskService.getTaskComment(comment.getId());
    NotAuthorizedOnWorkbasketException e =
        catchThrowableOfType(NotAuthorizedOnWorkbasketException.class, call);

    assertThat(e.getCurrentUserId()).isEqualTo("user-1-2");
    assertThat(e.getRequiredPermissions())
        .containsExactly(WorkbasketPermission.READ, WorkbasketPermission.READTASKS);
    assertThat(e.getWorkbasketId()).isEqualTo(defaultWorkbasket.getId());
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnTaskComment_For_TaskCommentId() throws Exception {
    TaskComment comment =
        TaskCommentBuilder.newTaskComment()
            .taskId(task1.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);

    TaskComment taskComment = taskService.getTaskComment(comment.getId());

    assertThat(taskComment).isEqualTo(comment);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_FailToReturnTaskComment_When_TaskCommentIsNotExisting() {
    String nonExistingId = "Definately Non Existing Task Comment Id";

    ThrowingCallable call = () -> taskService.getTaskComment(nonExistingId);
    TaskCommentNotFoundException e = catchThrowableOfType(TaskCommentNotFoundException.class, call);

    assertThat(e.getTaskCommentId()).isEqualTo(nonExistingId);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WithAdditionalUserInfoEnabled implements KadaiConfigurationModifier {

    @KadaiInject TaskService taskService;

    @KadaiInject UserService userService;

    @Override
    public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
      return builder.addAdditionalUserInfo(true);
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_SetCreatorFullNameOfTaskComment_When_PropertyEnabled() throws Exception {
      TaskComment comment =
          TaskCommentBuilder.newTaskComment()
              .taskId(task1.getId())
              .textField("Text1")
              .created(Instant.now())
              .modified(Instant.now())
              .buildAndStore(taskService);

      TaskComment taskComment = taskService.getTaskComment(comment.getId());
      String creatorFullName = userService.getUser(taskComment.getCreator()).getFullName();
      assertThat(taskComment)
          .extracting(TaskComment::getCreatorFullName)
          .isEqualTo(creatorFullName);
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_SetCreatorFullNameOfTaskComments_When_PropertyEnabled() throws Exception {
      TaskCommentBuilder.newTaskComment()
          .taskId(task1.getId())
          .textField("Text1")
          .created(Instant.now())
          .modified(Instant.now())
          .buildAndStore(taskService);

      List<TaskComment> taskComments = taskService.getTaskComments(task1.getId());

      taskComments.forEach(
          rethrowing(
              taskComment -> {
                String creatorFullName =
                    userService.getUser(taskComment.getCreator()).getFullName();
                assertThat(taskComment)
                    .extracting(TaskComment::getCreatorFullName)
                    .isEqualTo(creatorFullName);
              }));
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WithAdditionalUserInfoDisabled implements KadaiConfigurationModifier {

    @KadaiInject TaskService taskService;

    @Override
    public KadaiConfiguration.Builder modify(KadaiConfiguration.Builder builder) {
      return builder.addAdditionalUserInfo(false);
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_NotSetCreatorFullNameOfTaskComment_When_PropertyDisabled() throws Exception {
      TaskComment comment =
          TaskCommentBuilder.newTaskComment()
              .taskId(task1.getId())
              .textField("Text1")
              .created(Instant.now())
              .modified(Instant.now())
              .buildAndStore(taskService);

      TaskComment taskComment = taskService.getTaskComment(comment.getId());

      assertThat(taskComment).extracting(TaskComment::getCreatorFullName).isNull();
    }

    @WithAccessId(user = "user-1-1")
    @Test
    void should_NotSetCreatorFullNameOfTaskComments_When_PropertyDisabled() throws Exception {
      TaskCommentBuilder.newTaskComment()
          .taskId(task1.getId())
          .textField("Text1")
          .created(Instant.now())
          .modified(Instant.now())
          .buildAndStore(taskService);

      List<TaskComment> taskComments = taskService.getTaskComments(task1.getId());

      taskComments.forEach(
          taskComment ->
              assertThat(taskComment).extracting(TaskComment::getCreatorFullName).isNull());
    }
  }
}
