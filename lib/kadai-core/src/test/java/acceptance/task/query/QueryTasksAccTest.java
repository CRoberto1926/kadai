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

package acceptance.task.query;

import static io.kadai.common.api.BaseQuery.SortDirection.ASCENDING;
import static io.kadai.common.api.BaseQuery.SortDirection.DESCENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import acceptance.AbstractAccTest;
import acceptance.KadaiEngineProxy;
import acceptance.TaskTestMapper;
import io.kadai.KadaiConfiguration;
import io.kadai.KadaiConfiguration.Builder;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.internal.util.CollectionUtil;
import io.kadai.common.internal.util.Triplet;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskQueryColumnName;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.user.api.exceptions.UserNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

/** Acceptance test for all "query tasks with sorting" scenarios. */
@ExtendWith(JaasExtension.class)
class QueryTasksAccTest extends AbstractAccTest {

  @BeforeEach
  void before() throws Exception {
    // required if single tests modify database
    // TODO split test class into readOnly & modifying tests to improve performance
    resetDb(false);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_SetOwnerLongNameOfTask_When_PropertyEnabled() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(true).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<TaskSummary> tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .idIn("TKI:000000000000000000000000000000000000")
            .list();

    assertThat(tasks).hasSize(1);
    String longName = kadaiEngine.getUserService().getUser(tasks.get(0).getOwner()).getLongName();
    assertThat(tasks.get(0)).extracting(TaskSummary::getOwnerLongName).isEqualTo(longName);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_NotSetOwnerLongNameOfTask_When_PropertyDisabled() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<TaskSummary> tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .idIn("TKI:000000000000000000000000000000000000")
            .list();

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0)).extracting(TaskSummary::getOwnerLongName).isNull();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_SetOwnerLongNameOfTask_When_FilteringWithOwnerLongNameIn() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    String longName = "Eifrig, Elena - (user-1-2)";
    List<TaskSummary> tasks =
        kadaiEngine.getTaskService().createTaskQuery().ownerLongNameIn(longName).list();

    assertThat(tasks)
        .hasSize(25)
        .extracting(TaskSummary::getOwnerLongName)
        .doesNotContainNull()
        .containsOnly(longName);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_SetOwnerLongNameOfTask_When_FilteringWithOwnerLongNameNotIn() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    List<TaskSummary> tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .idIn(
                "TKI:000000000000000000000000000000000000",
                "TKI:000000000000000000000000000000000027")
            .ownerLongNameNotIn("Eifrig, Elena - (user-1-2)")
            .list();

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0))
        .extracting(TaskSummary::getOwnerLongName)
        .isEqualTo("Mustermann, Max - (user-1-1)");
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_SetOwnerLongNameOfTask_When_FilteringWithOwnerLongNameLike() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<TaskSummary> tasks =
        kadaiEngine.getTaskService().createTaskQuery().ownerLongNameLike("%1-2%").list();

    assertThat(tasks)
        .hasSize(25)
        .extracting(TaskSummary::getOwnerLongName)
        .doesNotContainNull()
        .containsOnly("Eifrig, Elena - (user-1-2)");
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_SetOwnerLongNameOfTask_When_FilteringWithOwnerLongNameNotLike() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<TaskSummary> tasks =
        kadaiEngine.getTaskService().createTaskQuery().ownerLongNameNotLike("%1-1%").list();

    assertThat(tasks)
        .hasSize(25)
        .extracting(TaskSummary::getOwnerLongName)
        .doesNotContainNull()
        .containsOnly("Eifrig, Elena - (user-1-2)");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_SetOwnerLongNameOfTaskToNull_When_OwnerNotExistingAsUserInDatabase()
      throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(true).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);

    List<TaskSummary> tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .idIn("TKI:000000000000000000000000000000000041")
            .list();

    assertThat(tasks).hasSize(1);
    ThrowingCallable call =
        () -> kadaiEngine.getUserService().getUser(tasks.get(0).getOwner()).getLongName();
    assertThatThrownBy(call).isInstanceOf(UserNotFoundException.class);
    assertThat(tasks.get(0)).extracting(TaskSummary::getOwnerLongName).isNull();
  }

  @WithAccessId(user = "admin")
  @Test
  void should_OrderByOwnerLongName_When_QueryingTask() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    List<TaskSummary> tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .stateIn(TaskState.CLAIMED)
            .ownerNotIn("user-b-1")
            .orderByOwnerLongName(ASCENDING)
            .list();
    assertThat(tasks).extracting(TaskSummary::getOwnerLongName).hasSize(18).isSorted();

    tasks =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .stateIn(TaskState.CLAIMED)
            .ownerNotIn("user-b-1")
            .orderByOwnerLongName(DESCENDING)
            .list();
    assertThat(tasks)
        .hasSize(18)
        .extracting(TaskSummary::getOwnerLongName)
        .isSortedAccordingTo(Comparator.reverseOrder());
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ListValues_For_OwnerLongName() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    List<String> longNames =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .listValues(TaskQueryColumnName.OWNER_LONG_NAME, ASCENDING)
            .stream()
            .filter(Objects::nonNull)
            .toList();
    assertThat(longNames)
        .hasSize(2)
        .isSorted()
        .containsExactly("Eifrig, Elena - (user-1-2)", "Mustermann, Max - (user-1-1)");

    longNames =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .listValues(TaskQueryColumnName.OWNER_LONG_NAME, DESCENDING)
            .stream()
            .filter(Objects::nonNull)
            .toList();
    assertThat(longNames)
        .hasSize(2)
        .contains("Mustermann, Max - (user-1-1)", "Eifrig, Elena - (user-1-2)")
        .isSortedAccordingTo(Comparator.reverseOrder());
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ListValuesCorrectly_When_FilteringWithOwnerLongName() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    String longName = "Eifrig, Elena - (user-1-2)";
    List<String> listedValues =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .ownerLongNameIn(longName)
            .orderByTaskId(null)
            .listValues(TaskQueryColumnName.ID, null);
    assertThat(listedValues).hasSize(25);

    List<TaskSummary> query =
        kadaiEngine
            .getTaskService()
            .createTaskQuery()
            .ownerLongNameIn(longName)
            .orderByTaskId(null)
            .list();
    assertThat(query).hasSize(25).extracting(TaskSummary::getId).isEqualTo(listedValues);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_CountCorrectly_When_FilteringWithOwnerLongName() throws Exception {
    KadaiConfiguration kadaiConfiguration =
        new Builder(AbstractAccTest.kadaiConfiguration).addAdditionalUserInfo(false).build();
    KadaiEngine kadaiEngine = KadaiEngine.buildKadaiEngine(kadaiConfiguration);
    String longName = "Eifrig, Elena - (user-1-2)";
    long count = kadaiEngine.getTaskService().createTaskQuery().ownerLongNameIn(longName).count();
    assertThat(count).isEqualTo(25);

    List<TaskSummary> query =
        kadaiEngine.getTaskService().createTaskQuery().ownerLongNameIn(longName).list();
    assertThat(query).hasSize((int) count);
  }

  @WithAccessId(user = "admin")
  @Test
  void should_SplitTaskListIntoChunksOf32000_When_AugmentingTasksAfterTaskQuery() {
    try (MockedStatic<CollectionUtil> listUtilMock =
        Mockito.mockStatic(CollectionUtil.class, new CallsRealMethods())) {
      taskService.createTaskQuery().list();

      listUtilMock.verify(() -> CollectionUtil.partitionBasedOnSize(any(), eq(32000)));
    }
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnTasksWithEmptyCustomFields_When_FilteringWithEmptyStringOnCustomField()
      throws InvalidArgumentException {
    List<TaskSummary> query =
        taskService.createTaskQuery().customAttributeIn(TaskCustomField.CUSTOM_1, "").list();
    assertThat(query).hasSize(96);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CustomAttributeTest {

    @WithAccessId(user = "admin")
    @TestFactory
    Stream<DynamicTest> should_ReturnCorrectResults_When_QueryingForCustomXStatements() {
      List<Triplet<TaskCustomField, String[], Integer>> list =
          List.of(
              Triplet.of(
                  TaskCustomField.CUSTOM_1, new String[] {"custom%", "p%", "%xyz%", "efg"}, 4),
              Triplet.of(TaskCustomField.CUSTOM_2, new String[] {"custom%", "a%"}, 2),
              Triplet.of(TaskCustomField.CUSTOM_3, new String[] {"ffg"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_4, new String[] {"%ust%", "%ty"}, 2),
              Triplet.of(TaskCustomField.CUSTOM_5, new String[] {"ew", "al"}, 6),
              Triplet.of(TaskCustomField.CUSTOM_6, new String[] {"%custom6%", "%vvg%", "11%"}, 5),
              Triplet.of(TaskCustomField.CUSTOM_7, new String[] {"ijk%"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_8, new String[] {"%lnp"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_9, new String[] {"%9%"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_10, new String[] {"ert%"}, 3),
              Triplet.of(TaskCustomField.CUSTOM_11, new String[] {"%ert"}, 3),
              Triplet.of(TaskCustomField.CUSTOM_12, new String[] {"dd%"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_13, new String[] {"%dd_"}, 1),
              Triplet.of(TaskCustomField.CUSTOM_14, new String[] {"%"}, 100),
              Triplet.of(TaskCustomField.CUSTOM_15, new String[] {"___"}, 4),
              Triplet.of(TaskCustomField.CUSTOM_16, new String[] {"___"}, 4));
      assertThat(list).hasSameSizeAs(TaskCustomField.values());

      return DynamicTest.stream(
          list.iterator(),
          t -> t.getLeft().name(),
          t -> testQueryForCustomXLikeAndIn(t.getLeft(), t.getMiddle(), t.getRight()));
    }

    void testQueryForCustomXLikeAndIn(
        TaskCustomField customField, String[] searchArguments, int expectedResult)
        throws Exception {
      List<TaskSummary> results =
          taskService.createTaskQuery().customAttributeLike(customField, searchArguments).list();
      assertThat(results).hasSize(expectedResult);

      String[] customAttributes =
          results.stream().map(t -> t.getCustomField(customField)).toArray(String[]::new);

      List<TaskSummary> result2 =
          taskService.createTaskQuery().customAttributeIn(customField, customAttributes).list();
      assertThat(result2).hasSize(expectedResult);
    }

    @WithAccessId(user = "admin")
    @TestFactory
    Stream<DynamicTest> should_ReturnCorrectResults_When_QueryingForCustomXNotIn() {
      // carefully constructed to always return exactly 2 results
      List<Triplet<TaskCustomField, String[], Integer>> list =
          List.of(
              Triplet.of(TaskCustomField.CUSTOM_1, new String[] {"custom1"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_2, new String[] {""}, 2),
              Triplet.of(TaskCustomField.CUSTOM_3, new String[] {"custom3"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_4, new String[] {""}, 2),
              Triplet.of(TaskCustomField.CUSTOM_5, new String[] {"ew", "al", "el"}, 93),
              Triplet.of(TaskCustomField.CUSTOM_6, new String[] {"11", "vvg"}, 96),
              Triplet.of(TaskCustomField.CUSTOM_7, new String[] {"custom7", "ijk"}, 98),
              Triplet.of(TaskCustomField.CUSTOM_8, new String[] {"not_existing"}, 100),
              Triplet.of(TaskCustomField.CUSTOM_9, new String[] {"custom9"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_10, new String[] {"custom10"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_11, new String[] {"custom11"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_12, new String[] {"custom12"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_13, new String[] {"custom13"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_14, new String[] {"abc"}, 0),
              Triplet.of(TaskCustomField.CUSTOM_15, new String[] {"custom15"}, 99),
              Triplet.of(TaskCustomField.CUSTOM_16, new String[] {"custom16"}, 99));
      assertThat(list).hasSameSizeAs(TaskCustomField.values());

      return DynamicTest.stream(
          list.iterator(),
          t -> t.getLeft().name(),
          t -> testQueryForCustomXNotIn(t.getLeft(), t.getMiddle(), t.getRight()));
    }

    void testQueryForCustomXNotIn(
        TaskCustomField customField, String[] searchArguments, int expectedCount) throws Exception {
      long results =
          taskService.createTaskQuery().customAttributeNotIn(customField, searchArguments).count();
      assertThat(results).isEqualTo(expectedCount);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ReturnTasksWithEmptyCustomField_When_QueriedByCustomFieldWhichIsNull()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService
              .createTaskQuery()
              .customAttributeIn(TaskCustomField.CUSTOM_9, new String[] {null})
              .list();
      assertThat(results).hasSize(1);

      results =
          taskService
              .createTaskQuery()
              .customAttributeIn(TaskCustomField.CUSTOM_9, null, "custom9")
              .list();
      assertThat(results).hasSize(2);

      results =
          taskService
              .createTaskQuery()
              .customAttributeIn(TaskCustomField.CUSTOM_9, new String[] {null})
              .customAttributeIn(TaskCustomField.CUSTOM_14, "abc")
              .list();
      assertThat(results).hasSize(1);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ReturnTasksWithNullCustomField_When_QueriedByCustomFieldWhichIsEmpty()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService.createTaskQuery().customAttributeIn(TaskCustomField.CUSTOM_9, "").list();
      assertThat(results).hasSize(98);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_AllowToQueryTasksByCustomFieldWithNullAndEmptyInParallel()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService
              .createTaskQuery()
              .customAttributeIn(TaskCustomField.CUSTOM_9, "", null)
              .list();
      assertThat(results).hasSize(99);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ReturnTasksWithEmptyCustomField_When_QueriedByCustomFieldWhichIsNotNull()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService
              .createTaskQuery()
              .customAttributeNotIn(TaskCustomField.CUSTOM_9, new String[] {null})
              .list();
      assertThat(results).hasSize(99);

      results =
          taskService
              .createTaskQuery()
              .customAttributeNotIn(TaskCustomField.CUSTOM_9, null, "custom9")
              .list();
      assertThat(results).hasSize(98);

      results =
          taskService
              .createTaskQuery()
              .customAttributeNotIn(TaskCustomField.CUSTOM_9, new String[] {null})
              .customAttributeNotIn(TaskCustomField.CUSTOM_10, "custom10")
              .list();
      assertThat(results).hasSize(98);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ReturnTasksWithNullCustomField_When_QueriedByCustomFieldWhichIsNotEmpty()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService.createTaskQuery().customAttributeNotIn(TaskCustomField.CUSTOM_9, "").list();
      assertThat(results).hasSize(2);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_AllowToQueryTasksByCustomFieldWithNeitherNullOrEmptyInParallel()
        throws InvalidArgumentException {
      List<TaskSummary> results =
          taskService
              .createTaskQuery()
              .customAttributeNotIn(TaskCustomField.CUSTOM_9, "", null)
              .list();
      assertThat(results).hasSize(1);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ThrowException_When_SearchArgumentInLikeQueryIsNotGiven() {
      TaskQuery taskQuery = taskService.createTaskQuery();
      assertThatThrownBy(() -> taskQuery.customAttributeLike(TaskCustomField.CUSTOM_7))
          .isInstanceOf(InvalidArgumentException.class);
    }

    @WithAccessId(user = "admin")
    @Test
    void should_ThrowException_When_SearchArgumentInInQueryIsNotGiven() {
      TaskQuery taskQuery = taskService.createTaskQuery();
      assertThatThrownBy(() -> taskQuery.customAttributeIn(TaskCustomField.CUSTOM_7))
          .isInstanceOf(InvalidArgumentException.class);
    }

    @WithAccessId(user = "admin")
    @Test
    void testQueryTaskByCustomAttributes() throws Exception {
      Task newTask = taskService.newTask("USER-1-1", "DOMAIN_A");
      newTask.setPrimaryObjRef(
          createObjectReference("COMPANY_A", "SYSTEM_A", "INSTANCE_A", "VNR", "1234567"));
      newTask.setClassificationKey("T2100");
      Map<String, String> customAttributesForCreate =
          createSimpleCustomPropertyMap(20000); // about 1 Meg
      newTask.setCustomAttributeMap(customAttributesForCreate);
      Task createdTask = taskService.createTask(newTask);

      assertThat(createdTask).isNotNull();
      // query the task by custom attributes
      KadaiEngineProxy engineProxy = new KadaiEngineProxy(kadaiEngine);
      try {
        SqlSession session = engineProxy.getSqlSession();
        Configuration config = session.getConfiguration();
        if (!config.hasMapper(TaskTestMapper.class)) {
          config.addMapper(TaskTestMapper.class);
        }

        TaskTestMapper mapper = session.getMapper(TaskTestMapper.class);
        engineProxy.openConnection();
        List<TaskImpl> queryResult =
            mapper.selectTasksByCustomAttributeLike("%Property Value of Property_1339%");

        assertThat(queryResult).hasSize(1);
        Task retrievedTask = queryResult.get(0);

        assertThat(retrievedTask.getId()).isEqualTo(createdTask.getId());

        // verify that the map is correctly retrieved from the database
        Map<String, String> customAttributesFromDb = retrievedTask.getCustomAttributeMap();
        assertThat(customAttributesFromDb).isEqualTo(customAttributesForCreate);

      } finally {
        engineProxy.returnConnection();
      }
    }
  }
}
