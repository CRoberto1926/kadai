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

package acceptance.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.InvalidCallbackStateException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.models.TaskImpl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "create task" scenarios. */
@ExtendWith(JaasExtension.class)
class CallbackStateAccTest extends AbstractAccTest {

  @WithAccessId(user = "user-1-1")
  @Test
  void testCreateTaskWithDifferentCallbackStates() throws Exception {

    TaskService taskService = kadaiEngine.getTaskService();
    TaskImpl createdTask = createTask(taskService, CallbackState.NONE);
    createdTask = (TaskImpl) taskService.getTask(createdTask.getId());
    assertThat(createdTask.getCallbackState()).isEqualTo(CallbackState.NONE);

    createdTask = createTask(taskService, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    createdTask = (TaskImpl) taskService.getTask(createdTask.getId());
    assertThat(createdTask.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    createdTask = createTask(taskService, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    createdTask = (TaskImpl) taskService.getTask(createdTask.getId());
    assertThat(createdTask.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_COMPLETED);

    createdTask = createTask(taskService, CallbackState.CLAIMED);
    createdTask = (TaskImpl) taskService.getTask(createdTask.getId());
    assertThat(createdTask.getCallbackState()).isEqualTo(CallbackState.CLAIMED);
  }

  @WithAccessId(user = "admin")
  @Test
  void testDeletionOfTaskWithWrongCallbackStateIsBlocked() throws Exception {
    TaskService taskService = kadaiEngine.getTaskService();

    final TaskImpl createdTask =
        createTask(kadaiEngine.getTaskService(), CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    assertThat(createdTask.getState()).isEqualTo(TaskState.READY);

    ThrowingCallable call = () -> taskService.forceDeleteTask(createdTask.getId());
    CallbackState[] expectedCallbackStates = {
      CallbackState.NONE, CallbackState.CLAIMED, CallbackState.CALLBACK_PROCESSING_COMPLETED
    };
    assertThatThrownBy(call)
        .isInstanceOf(InvalidCallbackStateException.class)
        .hasMessage(
            "Expected callback state of Task with id '%s' to be: '%s', but found '%s'",
            createdTask.getId(),
            Arrays.toString(expectedCallbackStates),
            createdTask.getCallbackState());

    final TaskImpl createdTask2 = (TaskImpl) taskService.claim(createdTask.getId());

    assertThat(createdTask2.getState()).isEqualTo(TaskState.CLAIMED);

    call = () -> taskService.forceDeleteTask(createdTask2.getId());
    assertThatThrownBy(call)
        .isInstanceOf(InvalidCallbackStateException.class)
        .hasMessage(
            "Expected callback state of Task with id '%s' to be: '%s', but found '%s'",
            createdTask2.getId(),
            Arrays.toString(expectedCallbackStates),
            createdTask2.getCallbackState());

    final TaskImpl createdTask3 = (TaskImpl) taskService.completeTask(createdTask.getId());

    call = () -> taskService.forceDeleteTask(createdTask3.getId());
    assertThatThrownBy(call)
        .isInstanceOf(InvalidCallbackStateException.class)
        .hasMessage(
            "Expected callback state of Task with id '%s' to be: '%s', but found '%s'",
            createdTask3.getId(),
            Arrays.toString(expectedCallbackStates),
            createdTask3.getCallbackState());
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotThrowUnsupportedOperationException_When_ExternalIdListIsUnmodifiable() {
    TaskService taskService = kadaiEngine.getTaskService();

    assertThatCode(
            () ->
                taskService.setCallbackStateForTasks(
                    List.of(""), CallbackState.CALLBACK_PROCESSING_COMPLETED))
        .doesNotThrowAnyException();
  }

  @WithAccessId(user = "admin")
  @Test
  void testUpdateOfCallbackState() throws Exception {

    TaskImpl createdTask1 =
        createTask(kadaiEngine.getTaskService(), CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask1.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask2 =
        createTask(kadaiEngine.getTaskService(), CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask2.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask3 =
        createTask(kadaiEngine.getTaskService(), CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask3.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskService taskService = kadaiEngine.getTaskService();
    createdTask1 = (TaskImpl) taskService.forceCompleteTask(createdTask1.getId());
    createdTask2 = (TaskImpl) taskService.forceCompleteTask(createdTask2.getId());
    createdTask3 = (TaskImpl) taskService.forceCompleteTask(createdTask3.getId());

    assertThat(createdTask1.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(createdTask2.getState()).isEqualTo(TaskState.COMPLETED);
    assertThat(createdTask3.getState()).isEqualTo(TaskState.COMPLETED);

    List<String> taskIds =
        List.of(createdTask1.getId(), createdTask2.getId(), createdTask3.getId());
    // delete should fail because callback_state = CALLBACK_PROCESSING_REQUIRED
    BulkOperationResults<String, KadaiException> bulkResult1 = taskService.deleteTasks(taskIds);

    assertThat(bulkResult1.containsErrors()).isTrue();

    assertThat(bulkResult1.getErrorMap().values())
        .hasOnlyElementsOfType(InvalidCallbackStateException.class);
    List<String> externalIds =
        List.of(
            createdTask1.getExternalId(),
            createdTask2.getExternalId(),
            createdTask3.getExternalId());

    // now enable deletion by setting callback state to CALLBACK_PROCESSING_COMPLETED
    BulkOperationResults<String, KadaiException> bulkResult2 =
        taskService.setCallbackStateForTasks(
            externalIds, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(bulkResult2.containsErrors()).isFalse();

    taskIds = List.of(createdTask1.getId(), createdTask2.getId(), createdTask3.getId());
    // now it should be possible to delete the tasks
    BulkOperationResults<String, KadaiException> bulkResult3 = taskService.deleteTasks(taskIds);
    assertThat(bulkResult3.containsErrors()).isFalse();
  }

  @WithAccessId(user = "admin")
  @Test
  void testInvalidUpdateOfCallbackStateToNone() throws Exception {

    TaskService taskService = kadaiEngine.getTaskService();

    TaskImpl createdTask1 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask1.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask2 = createTask(taskService, CallbackState.CLAIMED);
    assertThat(createdTask2.getCallbackState()).isEqualTo(CallbackState.CLAIMED);

    TaskImpl createdTask3 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(createdTask3.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_COMPLETED);

    List<String> externalIds =
        List.of(
            createdTask1.getExternalId(),
            createdTask2.getExternalId(),
            createdTask3.getExternalId());

    // try to set CallbackState to NONE
    BulkOperationResults<String, KadaiException> bulkResult =
        taskService.setCallbackStateForTasks(externalIds, CallbackState.NONE);

    // It's never allowed to set CallbackState to NONE over public API
    assertThat(bulkResult.containsErrors()).isTrue();
    List<String> failedTaskIds = bulkResult.getFailedIds();
    assertThat(failedTaskIds).hasSize(3);
  }

  @WithAccessId(user = "admin")
  @Test
  void testInvalidUpdateOfCallbackStateToComplete() throws Exception {

    TaskService taskService = kadaiEngine.getTaskService();

    TaskImpl createdTask1 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask1.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask2 = createTask(taskService, CallbackState.CLAIMED);
    assertThat(createdTask2.getCallbackState()).isEqualTo(CallbackState.CLAIMED);

    TaskImpl createdTask3 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(createdTask3.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_COMPLETED);

    List<String> externalIds =
        List.of(
            createdTask1.getExternalId(),
            createdTask2.getExternalId(),
            createdTask3.getExternalId());

    // complete a task
    createdTask3 = (TaskImpl) taskService.forceCompleteTask(createdTask3.getId());

    // It's only allowed to set CallbackState to COMPLETE, if TaskState equals COMPLETE, therefore 2
    // tasks should not get updated
    BulkOperationResults<String, KadaiException> bulkResult =
        taskService.setCallbackStateForTasks(
            externalIds, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(bulkResult.containsErrors()).isTrue();
    List<String> failedTaskIds = bulkResult.getFailedIds();
    assertThat(failedTaskIds).hasSize(2).doesNotContain(createdTask3.getExternalId());
  }

  @WithAccessId(user = "admin")
  @Test
  void testInvalidUpdateOfCallbackStateToClaimed() throws Exception {

    TaskService taskService = kadaiEngine.getTaskService();

    TaskImpl createdTask1 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask1.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask2 = createTask(taskService, CallbackState.CLAIMED);
    assertThat(createdTask2.getCallbackState()).isEqualTo(CallbackState.CLAIMED);

    TaskImpl createdTask3 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(createdTask3.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_COMPLETED);

    List<String> externalIds =
        List.of(
            createdTask1.getExternalId(),
            createdTask2.getExternalId(),
            createdTask3.getExternalId());

    // claim two tasks
    createdTask1 = (TaskImpl) taskService.forceClaim(createdTask1.getId());
    taskService.forceClaim(createdTask2.getId());

    // It's only allowed to claim a task if the TaskState equals CLAIMED and the CallbackState
    // equals REQUIRED
    // Therefore 2 tasks should not get updated
    BulkOperationResults<String, KadaiException> bulkResult =
        taskService.setCallbackStateForTasks(externalIds, CallbackState.CLAIMED);
    assertThat(bulkResult.containsErrors()).isTrue();
    List<String> failedTaskIds = bulkResult.getFailedIds();
    assertThat(failedTaskIds)
        .hasSize(2)
        .doesNotContain(createdTask1.getExternalId())
        .containsExactlyInAnyOrder(createdTask2.getExternalId(), createdTask3.getExternalId());
  }

  @WithAccessId(user = "admin")
  @Test
  void testInvalidUpdateOfCallbackStateToRequired() throws Exception {

    TaskService taskService = kadaiEngine.getTaskService();

    TaskImpl createdTask1 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(createdTask1.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_REQUIRED);

    TaskImpl createdTask2 = createTask(taskService, CallbackState.CLAIMED);
    assertThat(createdTask2.getCallbackState()).isEqualTo(CallbackState.CLAIMED);

    TaskImpl createdTask3 = createTask(taskService, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(createdTask3.getCallbackState())
        .isEqualTo(CallbackState.CALLBACK_PROCESSING_COMPLETED);

    List<String> externalIds =
        List.of(
            createdTask1.getExternalId(),
            createdTask2.getExternalId(),
            createdTask3.getExternalId());

    // It's only allowed to set the CallbackState to REQUIRED if the TaskState doesn't equal
    // COMPLETE
    // Therefore 1 task should not get updated
    BulkOperationResults<String, KadaiException> bulkResult =
        taskService.setCallbackStateForTasks(
            externalIds, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(bulkResult.containsErrors()).isTrue();
    List<String> failedTaskIds = bulkResult.getFailedIds();
    assertThat(failedTaskIds).containsExactlyInAnyOrder(createdTask3.getExternalId());
  }

  @WithAccessId(user = "admin")
  @Test
  void testQueriesWithCallbackState() throws Exception {
    resetDb(false);
    TaskService taskService = kadaiEngine.getTaskService();

    List<TaskSummary> claimedTasks =
        taskService.createTaskQuery().stateIn(TaskState.CLAIMED).list();
    assertThat(claimedTasks).hasSizeGreaterThan(10);
    taskService.forceCompleteTask(claimedTasks.get(0).getId());
    taskService.forceCompleteTask(claimedTasks.get(1).getId());
    taskService.forceCompleteTask(claimedTasks.get(2).getId());

    // now we should have several completed tasks with callback state NONE.
    // let's set it to CALLBACK_PROCESSING_REQUIRED
    List<TaskSummary> completedTasks =
        taskService.createTaskQuery().stateIn(TaskState.COMPLETED).list();
    List<String> externalIds =
        completedTasks.stream().map(TaskSummary::getExternalId).toList();
    BulkOperationResults<String, KadaiException> bulkResultCompleted =
        taskService.setCallbackStateForTasks(
            externalIds, CallbackState.CALLBACK_PROCESSING_REQUIRED);
    assertThat(bulkResultCompleted.containsErrors()).isFalse();

    // now complete some additional tasks
    taskService.forceCompleteTask(claimedTasks.get(3).getId());
    taskService.forceCompleteTask(claimedTasks.get(4).getId());
    taskService.forceCompleteTask(claimedTasks.get(5).getId());

    int numberOfCompletedTasksAtStartOfTest = completedTasks.size();
    // now lets retrieve those completed tasks that have callback_processing_required
    List<TaskSummary> tasksToBeActedUpon =
        taskService
            .createTaskQuery()
            .stateIn(TaskState.COMPLETED)
            .callbackStateIn(CallbackState.CALLBACK_PROCESSING_REQUIRED)
            .list();
    assertThat(tasksToBeActedUpon).hasSize(numberOfCompletedTasksAtStartOfTest);
    // now we set callback state to callback_processing_completed
    externalIds =
        tasksToBeActedUpon.stream().map(TaskSummary::getExternalId).toList();
    BulkOperationResults<String, KadaiException> bulkResult =
        taskService.setCallbackStateForTasks(
            externalIds, CallbackState.CALLBACK_PROCESSING_COMPLETED);
    assertThat(bulkResult.containsErrors()).isFalse();

    long numOfTasksRemaining =
        taskService
            .createTaskQuery()
            .stateIn(TaskState.COMPLETED)
            .callbackStateIn(CallbackState.CALLBACK_PROCESSING_REQUIRED)
            .count();
    assertThat(numOfTasksRemaining).isZero();
  }

  private TaskImpl createTask(TaskService taskService, CallbackState callbackState)
      throws Exception {
    Task newTask = taskService.newTask("USER-1-1", "DOMAIN_A");
    newTask.setClassificationKey("L12010");
    newTask.setPrimaryObjRef(
        createObjectReference("COMPANY_A", "SYSTEM_A", "INSTANCE_A", "VNR", "1234567"));
    newTask.setClassificationKey("L12010");
    HashMap<String, String> callbackInfo = new HashMap<>();
    callbackInfo.put(Task.CALLBACK_STATE, callbackState.name());
    newTask.setCallbackInfo(callbackInfo);
    augmentCallbackInfo(newTask);

    return (TaskImpl) taskService.createTask(newTask);
  }

  private void augmentCallbackInfo(Task task) {
    Map<String, String> callbackInfo = task.getCallbackInfo();
    for (int i = 1; i <= 10; i++) {
      callbackInfo.put("info_" + i, "Value of info_" + i);
    }
    task.setCallbackInfo(callbackInfo);
  }
}
