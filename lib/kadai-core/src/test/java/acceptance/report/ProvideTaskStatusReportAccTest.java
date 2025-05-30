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

package acceptance.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.monitor.api.MonitorService;
import io.kadai.monitor.api.reports.TaskStatusReport;
import io.kadai.monitor.api.reports.item.TaskQueryItem;
import io.kadai.monitor.api.reports.row.Row;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JaasExtension.class)
class ProvideTaskStatusReportAccTest extends AbstractReportAccTest {

  private static final MonitorService MONITOR_SERVICE = kadaiEngine.getMonitorService();

  @BeforeEach
  void reset() throws Exception {
    resetDb();
  }

  @Test
  void should_ThrowException_When_UserIsNotAuthorized() {
    assertThatThrownBy(() -> MONITOR_SERVICE.createTaskStatusReportBuilder().buildReport())
        .isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "unknown")
  @WithAccessId(user = "user-1-1")
  @WithAccessId(user = "businessadmin")
  @WithAccessId(user = "taskadmin")
  @TestTemplate
  void should_ThrowException_When_UserIsNotAdminOrMonitor() {
    assertThatThrownBy(() -> MONITOR_SERVICE.createTaskStatusReportBuilder().buildReport())
        .isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "admin")
  @WithAccessId(user = "monitor")
  @TestTemplate
  void should_BuildReport_When_UserIsAdminOrMonitor() {
    assertThatCode(() -> MONITOR_SERVICE.createTaskStatusReportBuilder().buildReport())
        .doesNotThrowAnyException();
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_AugmentDisplayNames_When_ReportIsBuild() throws Exception {
    TaskStatusReport report = MONITOR_SERVICE.createTaskStatusReportBuilder().buildReport();

    assertThat(report.getRows()).hasSize(5);
    assertThat(report.getRow("USER-1-1").getDisplayName()).isEqualTo("PPK User 1 KSC 1");
    assertThat(report.getRow("USER-1-2").getDisplayName()).isEqualTo("PPK User 1 KSC 2");
    assertThat(report.getRow("USER-1-3").getDisplayName()).isEqualTo("PPK User 1 KSC 3");
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_NotThrowSqlExceptionDuringAugmentation_When_ReportContainsNoRows() {
    TaskStatusReport.Builder builder =
        MONITOR_SERVICE.createTaskStatusReportBuilder().domainIn(List.of("DOES NOT EXIST"));
    ThrowingCallable test =
        () -> {
          TaskStatusReport report = builder.buildReport();
          assertThat(report).isNotNull();
          assertThat(report.rowSize()).isZero();
        };
    assertThatCode(test).doesNotThrowAnyException();
  }

  @WithAccessId(user = "monitor")
  @Test
  void testCompleteTaskStatusReport() throws Exception {
    TaskStatusReport report = MONITOR_SERVICE.createTaskStatusReportBuilder().buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    Row<TaskQueryItem> row1 = report.getRow("USER-1-1");
    assertThat(row1.getCells()).isEqualTo(new int[] {18, 2, 0, 0, 0, 0, 0});
    assertThat(row1.getTotalValue()).isEqualTo(20);

    Row<TaskQueryItem> row2 = report.getRow("USER-1-2");
    assertThat(row2.getCells()).isEqualTo(new int[] {19, 1, 1, 1, 0, 0, 0});
    assertThat(row2.getTotalValue()).isEqualTo(22);

    Row<TaskQueryItem> row3 = report.getRow("USER-1-3");
    assertThat(row3.getCells()).isEqualTo(new int[] {4, 6, 0, 0, 0, 0, 0});
    assertThat(row3.getTotalValue()).isEqualTo(10);

    Row<TaskQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {42, 12, 1, 1, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isEqualTo(56);
  }

  @WithAccessId(user = "monitor")
  @Test
  void testCompleteTaskStatusReportWithDomainFilter() throws Exception {
    TaskStatusReport report =
        MONITOR_SERVICE
            .createTaskStatusReportBuilder()
            .domainIn(List.of("DOMAIN_C", "DOMAIN_A"))
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    Row<TaskQueryItem> row1 = report.getRow("USER-1-1");
    assertThat(row1.getCells()).isEqualTo(new int[] {15, 2, 0, 0, 0, 0, 0});
    assertThat(row1.getTotalValue()).isEqualTo(17);

    Row<TaskQueryItem> row2 = report.getRow("USER-1-2");
    assertThat(row2.getCells()).isEqualTo(new int[] {14, 1, 1, 1, 0, 0, 0});
    assertThat(row2.getTotalValue()).isEqualTo(17);

    Row<TaskQueryItem> row3 = report.getRow("USER-1-3");
    assertThat(row3.getCells()).isEqualTo(new int[] {3, 3, 0, 0, 0, 0, 0});
    assertThat(row3.getTotalValue()).isEqualTo(6);

    Row<TaskQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {33, 8, 1, 1, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isEqualTo(43);
  }

  @WithAccessId(user = "monitor")
  @Test
  void testCompleteTaskStatusReportWithStateFilter() throws Exception {
    TaskStatusReport report =
        MONITOR_SERVICE
            .createTaskStatusReportBuilder()
            .stateIn(List.of(TaskState.READY))
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(4);

    Row<TaskQueryItem> row1 = report.getRow("USER-1-1");
    assertThat(row1.getCells()).isEqualTo(new int[] {18});
    assertThat(row1.getTotalValue()).isEqualTo(18);

    Row<TaskQueryItem> row2 = report.getRow("USER-1-2");
    assertThat(row2.getCells()).isEqualTo(new int[] {19});
    assertThat(row2.getTotalValue()).isEqualTo(19);

    Row<TaskQueryItem> row3 = report.getRow("USER-1-3");
    assertThat(row3.getCells()).isEqualTo(new int[] {4});
    assertThat(row3.getTotalValue()).isEqualTo(4);

    Row<TaskQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {42});
    assertThat(sumRow.getTotalValue()).isEqualTo(42);
  }

  @WithAccessId(user = "monitor", groups = "admin")
  @Test
  void testCompleteTaskStatusReportWithStates() throws Exception {
    TaskService taskService = kadaiEngine.getTaskService();
    taskService.terminateTask("TKI:000000000000000000000000000000000010");
    taskService.terminateTask("TKI:000000000000000000000000000000000011");
    taskService.terminateTask("TKI:000000000000000000000000000000000012");
    taskService.cancelTask("TKI:000000000000000000000000000000000013");
    taskService.cancelTask("TKI:000000000000000000000000000000000014");
    TaskStatusReport report =
        MONITOR_SERVICE
            .createTaskStatusReportBuilder()
            .stateIn(
                List.of(
                    TaskState.READY,
                    TaskState.CLAIMED,
                    TaskState.COMPLETED,
                    TaskState.CANCELLED,
                    TaskState.TERMINATED))
            .buildReport();
    int[] summaryNumbers = report.getSumRow().getCells();
    assertThat(summaryNumbers).hasSize(5);
    assertThat(summaryNumbers[3]).isEqualTo(2); // number of cancelled tasks
    assertThat(summaryNumbers[4]).isEqualTo(3); // number of terminated tasks
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_FilterTasksByMinimumPriority_When_BuilderIsFilteredWithMinimumPriority()
      throws Exception {

    TaskStatusReport report =
        MONITOR_SERVICE.createTaskStatusReportBuilder().priorityMinimum(5).buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(4);

    Row<TaskQueryItem> row1 = report.getRow("USER-1-1");
    assertThat(row1.getCells()).isEqualTo(new int[] {3, 1, 0, 0, 0, 0, 0});
    assertThat(row1.getTotalValue()).isEqualTo(4);

    Row<TaskQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {4, 5, 0, 0, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isEqualTo(9);
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_FilterTasksByWorkbasket_When_BuilderIsFilteredWithWorkbasketIds() throws Exception {
    TaskStatusReport report =
        MONITOR_SERVICE
            .createTaskStatusReportBuilder()
            .workbasketIdsIn(List.of("WBI:000000000000000000000000000000000003"))
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(1);

    Row<TaskQueryItem> row1 = report.getRow("USER-1-3");
    assertThat(row1.getCells()).isEqualTo(new int[] {4, 6, 0, 0, 0, 0, 0});
    assertThat(row1.getTotalValue()).isEqualTo(10);

    Row<TaskQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {4, 6, 0, 0, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isEqualTo(10);
  }
}
