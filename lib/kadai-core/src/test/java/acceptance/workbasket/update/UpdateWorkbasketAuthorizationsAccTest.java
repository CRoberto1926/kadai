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

package acceptance.workbasket.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.KeyDomain;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedToQueryWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.internal.models.WorkbasketAccessItemImpl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "update workbasket authorizations" scenarios. */
@ExtendWith(JaasExtension.class)
class UpdateWorkbasketAuthorizationsAccTest extends AbstractAccTest {

  @WithAccessId(user = "user-1-1")
  @WithAccessId(user = "taskadmin")
  @TestTemplate
  void should_ThrowException_When_UserIsNotAdminOrBusinessAdmin() {

    final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    WorkbasketAccessItem workbasketAccessItem =
        workbasketService.newWorkbasketAccessItem(
            "WBI:100000000000000000000000000000000008", "newAccessIdForUpdate");

    assertThatThrownBy(() -> workbasketService.updateWorkbasketAccessItem(workbasketAccessItem))
        .isInstanceOf(NotAuthorizedException.class);
  }

  @Test
  @WithAccessId(user = "admin")
  void
      should_ThrowWorkbasketNotFoundException_When_TryingToSetAccessItemsOfNonExistingWorkbasket() {

    final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    WorkbasketAccessItem workbasketAccessItem =
        workbasketService.newWorkbasketAccessItem("WBI:1337gibtEsNicht007", "newAccessIdForUpdate");

    assertThatThrownBy(
            () ->
                workbasketService.setWorkbasketAccessItems(
                    "WBI:1337gibtEsNicht007", List.of(workbasketAccessItem)))
        .isInstanceOf(WorkbasketNotFoundException.class);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdateWorkbasketAccessItemSucceeds() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    WorkbasketAccessItem accessItem =
        workbasketService.newWorkbasketAccessItem(
            "WBI:100000000000000000000000000000000002", "user1");
    accessItem.setPermission(WorkbasketPermission.READ, true);
    accessItem.setPermission(WorkbasketPermission.APPEND, true);
    accessItem.setPermission(WorkbasketPermission.CUSTOM_11, true);
    accessItem = workbasketService.createWorkbasketAccessItem(accessItem);

    final WorkbasketAccessItem newItem = accessItem;
    accessItem.setPermission(WorkbasketPermission.CUSTOM_1, true);
    accessItem.setPermission(WorkbasketPermission.APPEND, false);
    accessItem.setAccessName("Rojas, Miguel");
    workbasketService.updateWorkbasketAccessItem(accessItem);
    List<WorkbasketAccessItem> items =
        workbasketService.getWorkbasketAccessItems("WBI:100000000000000000000000000000000002");

    WorkbasketAccessItem updatedItem =
        items.stream().filter(t -> newItem.getId().equals(t.getId())).findFirst().orElse(null);

    assertThat(updatedItem).isNotNull();
    assertThat(updatedItem.getAccessName()).isEqualTo("Rojas, Miguel");
    assertThat(updatedItem.getPermission(WorkbasketPermission.APPEND)).isFalse();
    assertThat(updatedItem.getPermission(WorkbasketPermission.READ)).isTrue();
    assertThat(updatedItem.getPermission(WorkbasketPermission.CUSTOM_11)).isTrue();
    assertThat(updatedItem.getPermission(WorkbasketPermission.CUSTOM_1)).isTrue();
    assertThat(updatedItem.getPermission(WorkbasketPermission.CUSTOM_2)).isFalse();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdateWorkbasketAccessItemRejected() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    WorkbasketAccessItem accessItem =
        workbasketService.newWorkbasketAccessItem(
            "WBI:100000000000000000000000000000000001", "user1");
    accessItem.setPermission(WorkbasketPermission.APPEND, true);
    accessItem.setPermission(WorkbasketPermission.CUSTOM_11, true);
    accessItem.setPermission(WorkbasketPermission.READ, true);
    WorkbasketAccessItem accessItemCreated =
        workbasketService.createWorkbasketAccessItem(accessItem);

    accessItemCreated.setPermission(WorkbasketPermission.CUSTOM_1, true);
    accessItemCreated.setPermission(WorkbasketPermission.APPEND, false);
    ((WorkbasketAccessItemImpl) accessItemCreated).setAccessId("willi");

    assertThatThrownBy(() -> workbasketService.updateWorkbasketAccessItem(accessItemCreated))
        .describedAs("InvalidArgumentException was expected because access id was changed")
        .isInstanceOf(InvalidArgumentException.class);

    ((WorkbasketAccessItemImpl) accessItemCreated).setAccessId("user1");
    WorkbasketAccessItem accessItemUpdated =
        workbasketService.updateWorkbasketAccessItem(accessItemCreated);
    assertThat(accessItemUpdated.getPermission(WorkbasketPermission.APPEND)).isFalse();
    assertThat(accessItemUpdated.getPermission(WorkbasketPermission.READ)).isTrue();
    assertThat(accessItemUpdated.getPermission(WorkbasketPermission.CUSTOM_11)).isTrue();
    assertThat(accessItemUpdated.getPermission(WorkbasketPermission.CUSTOM_1)).isTrue();
    assertThat(accessItemUpdated.getPermission(WorkbasketPermission.CUSTOM_2)).isFalse();

    ((WorkbasketAccessItemImpl) accessItemUpdated).setWorkbasketId("2");

    assertThatThrownBy(() -> workbasketService.updateWorkbasketAccessItem(accessItemUpdated))
        .describedAs("InvalidArgumentException was expected because key was changed")
        .isInstanceOf(InvalidArgumentException.class);
  }

  @WithAccessId(user = "businessadmin", groups = GROUP_2_DN)
  @Test
  void testUpdatedAccessItemLeadsToNotAuthorizedException() throws Exception {
    TaskService taskService = kadaiEngine.getTaskService();
    final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();

    String wbKey = "USER-2-1";
    String wbDomain = "DOMAIN_A";

    Task newTask = taskService.newTask(wbKey, wbDomain);
    newTask.setClassificationKey("T2100");
    newTask.setPrimaryObjRef(
        createObjectReference("COMPANY_A", "SYSTEM_A", "INSTANCE_A", "VNR", "1234567"));
    Task createdTask = taskService.createTask(newTask);
    List<TaskSummary> tasks =
        taskService.createTaskQuery().workbasketKeyDomainIn(new KeyDomain(wbKey, wbDomain)).list();
    assertThat(tasks).hasSize(1);
    assertThat(createdTask).isNotNull();

    List<WorkbasketAccessItem> accessItems =
        workbasketService.getWorkbasketAccessItems("WBI:100000000000000000000000000000000008");
    WorkbasketAccessItem theAccessItem =
        accessItems.stream()
            .filter(x -> GROUP_2_DN.equalsIgnoreCase(x.getAccessId()))
            .findFirst()
            .orElse(null);

    assertThat(theAccessItem).isNotNull();
    theAccessItem.setPermission(WorkbasketPermission.OPEN, false);
    workbasketService.updateWorkbasketAccessItem(theAccessItem);

    ThrowingCallable call =
        () ->
            taskService
                .createTaskQuery()
                .workbasketKeyDomainIn(new KeyDomain(wbKey, wbDomain))
                .list();
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedToQueryWorkbasketException.class);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_setReadTasksPerm() throws Exception {
    final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    String wbId = "WBI:100000000000000000000000000000000006";

    List<WorkbasketAccessItem> accessItems = workbasketService.getWorkbasketAccessItems(wbId);
    WorkbasketAccessItem theAccessItem =
        accessItems.stream()
            .filter(x -> "user-1-1".equalsIgnoreCase(x.getAccessId()))
            .findFirst()
            .orElse(null);
    assertThat(theAccessItem).isNotNull();
    theAccessItem.setPermission(WorkbasketPermission.READTASKS, false);
    workbasketService.updateWorkbasketAccessItem(theAccessItem);

    List<WorkbasketAccessItem> accessItems2 = workbasketService.getWorkbasketAccessItems(wbId);
    WorkbasketAccessItem item =
        accessItems2.stream()
            .filter(t -> theAccessItem.getId().equals(t.getId()))
            .findFirst()
            .orElse(null);
    assertThat(item).isNotNull();
    assertThat(theAccessItem.getPermission(WorkbasketPermission.READTASKS)).isFalse();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_setEditTasksPerm() throws Exception {
    final WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    String wbId = "WBI:100000000000000000000000000000000006";

    List<WorkbasketAccessItem> accessItems = workbasketService.getWorkbasketAccessItems(wbId);
    WorkbasketAccessItem theAccessItem =
        accessItems.stream()
            .filter(x -> "user-1-1".equalsIgnoreCase(x.getAccessId()))
            .findFirst()
            .orElse(null);
    assertThat(theAccessItem).isNotNull();
    theAccessItem.setPermission(WorkbasketPermission.EDITTASKS, false);
    workbasketService.updateWorkbasketAccessItem(theAccessItem);

    List<WorkbasketAccessItem> accessItems2 = workbasketService.getWorkbasketAccessItems(wbId);
    WorkbasketAccessItem item =
        accessItems2.stream()
            .filter(t -> theAccessItem.getId().equals(t.getId()))
            .findFirst()
            .orElse(null);
    assertThat(item).isNotNull();
    assertThat(theAccessItem.getPermission(WorkbasketPermission.EDITTASKS)).isFalse();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testUpdatedAccessItemList() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    final String wbId = "WBI:100000000000000000000000000000000004";
    List<WorkbasketAccessItem> accessItems = workbasketService.getWorkbasketAccessItems(wbId);

    // update some values
    WorkbasketAccessItem item0 = accessItems.get(0);
    item0.setPermission(WorkbasketPermission.APPEND, false);
    item0.setPermission(WorkbasketPermission.OPEN, false);
    item0.setPermission(WorkbasketPermission.TRANSFER, false);

    WorkbasketAccessItem item1 = accessItems.get(1);
    item1.setPermission(WorkbasketPermission.APPEND, false);
    item1.setPermission(WorkbasketPermission.OPEN, false);
    item1.setPermission(WorkbasketPermission.TRANSFER, false);

    workbasketService.setWorkbasketAccessItems(wbId, accessItems);

    List<WorkbasketAccessItem> updatedAccessItems =
        workbasketService.getWorkbasketAccessItems(wbId);
    assertThat(updatedAccessItems)
        .hasSameSizeAs(accessItems)
        .usingElementComparator(Comparator.comparing(WorkbasketAccessItem::getId))
        .contains(item0, item1);

    WorkbasketAccessItem item =
        updatedAccessItems.stream().filter(i -> i.getId().equals(item0.getId())).findFirst().get();
    assertThat(item.getPermission(WorkbasketPermission.APPEND)).isFalse();
    assertThat(item.getPermission(WorkbasketPermission.OPEN)).isFalse();
    assertThat(item.getPermission(WorkbasketPermission.TRANSFER)).isFalse();

    item =
        updatedAccessItems.stream().filter(i -> i.getId().equals(item1.getId())).findFirst().get();
    assertThat(item.getPermission(WorkbasketPermission.APPEND)).isFalse();
    assertThat(item.getPermission(WorkbasketPermission.OPEN)).isFalse();
    assertThat(item.getPermission(WorkbasketPermission.TRANSFER)).isFalse();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testInsertAccessItemList() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    final String wbId = "WBI:100000000000000000000000000000000004";
    List<WorkbasketAccessItem> accessItems = workbasketService.getWorkbasketAccessItems(wbId);
    final int countBefore = accessItems.size();

    // update some values
    WorkbasketAccessItem item0 = accessItems.get(0);
    item0.setPermission(WorkbasketPermission.APPEND, false);
    item0.setPermission(WorkbasketPermission.OPEN, false);
    item0.setPermission(WorkbasketPermission.TRANSFER, false);
    final String updateId0 = item0.getId();
    // insert new entry
    WorkbasketAccessItem newItem = workbasketService.newWorkbasketAccessItem(wbId, "dummyUser1");
    newItem.setPermission(WorkbasketPermission.READ, true);
    newItem.setPermission(WorkbasketPermission.OPEN, true);
    newItem.setPermission(WorkbasketPermission.CUSTOM_12, true);
    accessItems.add(newItem);

    workbasketService.setWorkbasketAccessItems(wbId, accessItems);

    List<WorkbasketAccessItem> updatedAccessItems =
        workbasketService.getWorkbasketAccessItems(wbId);
    assertThat(updatedAccessItems).hasSize(countBefore + 1);

    item0 = updatedAccessItems.stream().filter(i -> i.getId().equals(updateId0)).findFirst().get();
    assertThat(item0.getPermission(WorkbasketPermission.APPEND)).isFalse();
    assertThat(item0.getPermission(WorkbasketPermission.OPEN)).isFalse();
    assertThat(item0.getPermission(WorkbasketPermission.TRANSFER)).isFalse();
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testDeleteAccessItemForAccessItemId() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    final String wbId = "WBI:100000000000000000000000000000000001";

    List<WorkbasketAccessItem> originalList = workbasketService.getWorkbasketAccessItems(wbId);
    List<String> originalIds = new ArrayList<>();
    for (WorkbasketAccessItem a : originalList) {
      originalIds.add(a.getId());
    }

    List<WorkbasketAccessItem> accessList = new ArrayList<>(originalList);
    WorkbasketAccessItem newItem = workbasketService.newWorkbasketAccessItem(wbId, GROUP_1_DN);
    accessList.add(newItem);
    assertThat(accessList).isNotEqualTo(originalList);
    workbasketService.setWorkbasketAccessItems(wbId, accessList);

    List<WorkbasketAccessItem> modifiedList =
        new ArrayList<>(workbasketService.getWorkbasketAccessItems(wbId));
    for (WorkbasketAccessItem a : modifiedList) {
      if (!originalIds.contains(a.getId())) {
        workbasketService.deleteWorkbasketAccessItem(a.getId());
      }
    }
    List<WorkbasketAccessItem> listEqualToOriginal =
        new ArrayList<>(workbasketService.getWorkbasketAccessItems(wbId));

    // with DB2 V 11, the lists are sorted differently...
    assertThat(new HashSet<>(listEqualToOriginal)).isEqualTo(new HashSet<>(originalList));
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testDeleteAccessItemsForAccessId() throws Exception {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    final long accessIdCountBefore =
        workbasketService.createWorkbasketAccessItemQuery().accessIdIn(GROUP_1_DN).count();

    workbasketService.deleteWorkbasketAccessItemsForAccessId(GROUP_1_DN);

    final long accessIdCountAfter =
        workbasketService.createWorkbasketAccessItemQuery().accessIdIn(GROUP_1_DN).count();
    assertThat(accessIdCountBefore).isGreaterThan(accessIdCountAfter);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void testDeleteAccessItemsForAccessIdWithUnusedValuesThrowingNoException() {
    WorkbasketService workbasketService = kadaiEngine.getWorkbasketService();
    assertThatCode(() -> workbasketService.deleteWorkbasketAccessItemsForAccessId(""))
        .doesNotThrowAnyException();
    assertThatCode(() -> workbasketService.deleteWorkbasketAccessItemsForAccessId(null))
        .doesNotThrowAnyException();
    assertThatCode(() -> workbasketService.deleteWorkbasketAccessItemsForAccessId("123UNUSED456"))
        .doesNotThrowAnyException();
  }
}
