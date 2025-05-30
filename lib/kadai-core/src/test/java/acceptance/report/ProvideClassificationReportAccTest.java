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
import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.api.reports.ClassificationReport;
import io.kadai.monitor.api.reports.ClassificationReport.Builder;
import io.kadai.monitor.api.reports.header.TimeIntervalColumnHeader;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;

/** Acceptance test for all "classification report" scenarios. */
@ExtendWith(JaasExtension.class)
class ProvideClassificationReportAccTest extends AbstractReportAccTest {

  private static final MonitorService MONITOR_SERVICE = kadaiEngine.getMonitorService();

  @Test
  void testRoleCheck() {
    assertThatThrownBy(() -> MONITOR_SERVICE.createClassificationReportBuilder().buildReport())
        .isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_AugmentDisplayNames_When_ReportIsBuild() throws Exception {
    ClassificationReport report = MONITOR_SERVICE.createClassificationReportBuilder().buildReport();

    assertThat(report.getRows()).hasSize(5);
    assertThat(report.getRow("L10000").getDisplayName()).isEqualTo("OLD-Leistungsfall");
    assertThat(report.getRow("L20000").getDisplayName()).isEqualTo("Beratungsprotokoll");
    assertThat(report.getRow("L30000").getDisplayName()).isEqualTo("Widerruf");
    assertThat(report.getRow("L40000").getDisplayName()).isEqualTo("Dynamikaenderung");
    assertThat(report.getRow("L50000").getDisplayName()).isEqualTo("Dynamik-Ablehnung");
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_NotThrowSqlExceptionDuringAugmentation_When_ReportContainsNoRows() {
    Builder builder =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .classificationIdIn(List.of("DOES NOT EXIST"));
    ThrowingCallable test =
        () -> {
          ClassificationReport report = builder.buildReport();
          assertThat(report).isNotNull();
          assertThat(report.rowSize()).isZero();
        };
    assertThatCode(test).doesNotThrowAnyException();
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_FilterTasksAccordingToClassificationId_When_ClassificationIdFilterIsApplied()
      throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnsHeaders();
    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .classificationIdIn(List.of("CLI:000000000000000000000000000000000001"))
            .buildReport();
    assertThat(report).isNotNull();

    assertThat(report.rowSize()).isOne();
    assertThat(report.getRow("L10000").getCells()).isEqualTo(new int[] {7, 2, 0, 0, 1, 0, 0, 0, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTotalNumbersOfTasksOfClassificationReport() throws Exception {
    ClassificationReport report = MONITOR_SERVICE.createClassificationReportBuilder().buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    assertThat(report.getRow("L10000").getTotalValue()).isEqualTo(10);
    assertThat(report.getRow("L20000").getTotalValue()).isEqualTo(10);
    assertThat(report.getRow("L30000").getTotalValue()).isEqualTo(8);
    assertThat(report.getRow("L40000").getTotalValue()).isEqualTo(12);
    assertThat(report.getRow("L50000").getTotalValue()).isEqualTo(16);
    assertThat(report.getRow("L10000").getCells()).isEmpty();
    assertThat(report.getRow("L20000").getCells()).isEmpty();
    assertThat(report.getRow("L30000").getCells()).isEmpty();
    assertThat(report.getRow("L40000").getCells()).isEmpty();
    assertThat(report.getRow("L50000").getCells()).isEmpty();
    assertThat(report.getSumRow().getTotalValue()).isEqualTo(56);
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetClassificationReportWithReportLineItemDefinitions() throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnsHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    assertThat(report.getRow("L10000").getTotalValue()).isEqualTo(10);
    assertThat(report.getRow("L20000").getTotalValue()).isEqualTo(10);
    assertThat(report.getRow("L30000").getTotalValue()).isEqualTo(8);
    assertThat(report.getRow("L40000").getTotalValue()).isEqualTo(12);
    assertThat(report.getRow("L50000").getTotalValue()).isEqualTo(16);

    assertThat(report.getSumRow().getCells()).isEqualTo(new int[] {10, 9, 11, 0, 4, 0, 8, 7, 7});
    assertThat(report.getSumRow().getTotalValue()).isEqualTo(56);
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReport() throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {7, 2, 1, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {5, 3, 1, 1, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {2, 1, 0, 1, 4});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {2, 2, 2, 0, 6});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {3, 3, 0, 6, 4});
  }

  @WithAccessId(user = "monitor")
  @TestFactory
  Stream<DynamicTest> should_NotThrowError_When_BuildReportForTaskState() {
    Iterator<TaskTimestamp> iterator = Arrays.stream(TaskTimestamp.values()).iterator();
    ThrowingConsumer<TaskTimestamp> test =
        timestamp -> {
          ThrowingCallable callable =
              () -> MONITOR_SERVICE.createClassificationReportBuilder().buildReport(timestamp);
          assertThatCode(callable).doesNotThrowAnyException();
        };
    return DynamicTest.stream(iterator, t -> "for TaskState " + t, test);
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ComputeNumbersAccordingToPlannedDate_When_BuildReportForPlanned() throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport(TaskTimestamp.PLANNED);

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {0, 2, 8, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {0, 1, 9, 0, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {0, 0, 8, 0, 0});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {0, 0, 12, 0, 0});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {0, 0, 16, 0, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReportNotInWorkingDays() throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {9, 0, 1, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {8, 0, 1, 0, 1});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {3, 0, 0, 0, 5});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {4, 0, 2, 0, 6});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {6, 0, 0, 0, 10});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReportWithWorkbasketFilter() throws Exception {
    List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .workbasketIdIn(workbasketIds)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {6, 0, 0, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {2, 0, 0, 0, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {2, 1, 0, 1, 1});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {1, 0, 1, 0, 1});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {2, 2, 0, 0, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReportWithStateFilter() throws Exception {
    List<TaskState> states = List.of(TaskState.READY);
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .stateIn(states)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {7, 2, 1, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {5, 3, 1, 1, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {2, 1, 0, 1, 0});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {2, 2, 2, 0, 0});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {3, 3, 0, 6, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReportWithCategoryFilter() throws Exception {
    List<String> categories = List.of("AUTOMATIC", "MANUAL");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .classificationCategoryIn(categories)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(2);

    int[] row1 = report.getRow("L30000").getCells();
    assertThat(row1).isEqualTo(new int[] {2, 1, 0, 1, 4});

    int[] row2 = report.getRow("L40000").getCells();
    assertThat(row2).isEqualTo(new int[] {2, 2, 2, 0, 6});
  }

  @WithAccessId(user = "monitor")
  @Test
  void testEachItemOfClassificationReportWithDomainFilter() throws Exception {
    List<String> domains = List.of("DOMAIN_A");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .domainIn(domains)
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {5, 2, 1, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {3, 1, 1, 1, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {1, 0, 0, 1, 2});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {2, 0, 0, 0, 3});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {0, 1, 0, 3, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnItemsOfClassificationReport_When_FilteringWithCustomAttributeIn()
      throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .customAttributeIn(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A")
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {4, 0, 0, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {4, 1, 1, 1, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {1, 0, 0, 1, 1});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {1, 1, 2, 0, 4});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {1, 2, 0, 2, 0});
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnItemsOfClassificationReport_When_FilteringWithCustomAttributeNotIn()
      throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .customAttributeNotIn(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A")
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);

    int[] row1 = report.getRow("L10000").getCells();
    assertThat(row1).isEqualTo(new int[] {3, 2, 1, 0, 0});

    int[] row2 = report.getRow("L20000").getCells();
    assertThat(row2).isEqualTo(new int[] {1, 2, 0, 0, 0});

    int[] row3 = report.getRow("L30000").getCells();
    assertThat(row3).isEqualTo(new int[] {1, 1, 0, 0, 3});

    int[] row4 = report.getRow("L40000").getCells();
    assertThat(row4).isEqualTo(new int[] {1, 1, 0, 0, 2});

    int[] row5 = report.getRow("L50000").getCells();
    assertThat(row5).isEqualTo(new int[] {2, 1, 0, 4, 4});
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnItemsOfClassificationReport_When_FilteringWithCustomAttributeLike()
      throws Exception {
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    ClassificationReport report =
        MONITOR_SERVICE
            .createClassificationReportBuilder()
            .withColumnHeaders(columnHeaders)
            .customAttributeLike(TaskCustomField.CUSTOM_1, "_eschaeftsstelle A")
            .inWorkingDays()
            .buildReport();

    assertThat(report).isNotNull();
    assertThat(report.rowSize()).isEqualTo(5);
  }

  private List<TimeIntervalColumnHeader> getListOfColumnsHeaders() {
    List<TimeIntervalColumnHeader> columnHeaders = new ArrayList<>();
    columnHeaders.add(new TimeIntervalColumnHeader(Integer.MIN_VALUE, -11));
    columnHeaders.add(new TimeIntervalColumnHeader(-10, -6));
    columnHeaders.add(new TimeIntervalColumnHeader(-5, -2));
    columnHeaders.add(new TimeIntervalColumnHeader(-1));
    columnHeaders.add(new TimeIntervalColumnHeader(0));
    columnHeaders.add(new TimeIntervalColumnHeader(1));
    columnHeaders.add(new TimeIntervalColumnHeader(2, 5));
    columnHeaders.add(new TimeIntervalColumnHeader(6, 10));
    columnHeaders.add(new TimeIntervalColumnHeader(11, Integer.MAX_VALUE));
    return columnHeaders;
  }

  private List<TimeIntervalColumnHeader> getShortListOfColumnHeaders() {
    List<TimeIntervalColumnHeader> reportLineItemDefinitions = new ArrayList<>();
    reportLineItemDefinitions.add(new TimeIntervalColumnHeader(Integer.MIN_VALUE, -6));
    reportLineItemDefinitions.add(new TimeIntervalColumnHeader(-5, -1));
    reportLineItemDefinitions.add(new TimeIntervalColumnHeader(0));
    reportLineItemDefinitions.add(new TimeIntervalColumnHeader(1, 5));
    reportLineItemDefinitions.add(new TimeIntervalColumnHeader(6, Integer.MAX_VALUE));
    return reportLineItemDefinitions;
  }
}
