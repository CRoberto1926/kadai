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

import io.kadai.monitor.api.reports.Report;
import io.kadai.monitor.api.reports.header.TimeIntervalColumnHeader;
import io.kadai.monitor.api.reports.item.MonitorQueryItem;
import io.kadai.monitor.api.reports.item.QueryItemPreprocessor;
import io.kadai.monitor.api.reports.row.FoldableRow;
import io.kadai.monitor.api.reports.row.Row;
import io.kadai.monitor.api.reports.row.SingleRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@linkplain Report}. */
class ReportTest {

  private static final List<TimeIntervalColumnHeader> HEADERS =
      IntStream.range(0, 4).mapToObj(TimeIntervalColumnHeader::new).toList();
  private Report<MonitorQueryItem, TimeIntervalColumnHeader> report;
  private MonitorQueryItem item;

  @BeforeEach
  void before() {
    this.report =
        new MonitorQueryItemTimeIntervalColumnHeaderReport(HEADERS, new String[] {"rowDesc"});

    item = new MonitorQueryItem();
    item.setKey("key");
    item.setAgeInDays(0);
    item.setNumberOfTasks(3);
  }

  @Test
  void should_HaveSumRowTotalKey_When_ReportExists() {
    assertThat(report.getSumRow().getKey()).isEqualTo("Total");
  }

  @Test
  void should_HaveEmptySumRow_When_ReportIsEmpty() {
    // then
    assertThat(report.getRows()).isEmpty();
    Row<MonitorQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {0, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isZero();
  }

  @Test
  void should_CreateRowAndSetKey_When_InsertingItemWithUnknownKey() {
    // when
    report.addItem(item);

    // then
    assertThat(report.getRows()).hasSize(1);
    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getKey()).isEqualTo("key");
  }

  @Test
  void should_CreateFoldableRowAndSetKey_When_InsertingItemWithUnknownKey() {
    // when
    ReportWithFoldableRow reportWithFoldableRow =
        new ReportWithFoldableRow(HEADERS, new String[] {"rowDesc", "foldableRowDesc"});
    reportWithFoldableRow.addItem(item);

    // then
    assertThat(reportWithFoldableRow.getRows()).hasSize(1);
    FoldableTestRow row = reportWithFoldableRow.getRow("key");
    assertThat(row.getKey()).isEqualTo("key");

    assertThat(row.getFoldableRowCount()).isOne();
    Row<MonitorQueryItem> foldableRow = row.getFoldableRow("KEY");
    assertThat(foldableRow.getKey()).isEqualTo("KEY");
  }

  @Test
  void should_AppendItemValueInFoldableRow_When_ItemIsInserted() {
    // when
    ReportWithFoldableRow reportWithFoldableRow =
        new ReportWithFoldableRow(HEADERS, new String[] {"rowDesc", "foldableRowDesc"});
    reportWithFoldableRow.addItem(item);

    // then
    assertThat(reportWithFoldableRow.getRows()).hasSize(1);
    FoldableTestRow row = reportWithFoldableRow.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {item.getValue(), 0, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(item.getValue());

    assertThat(row.getFoldableRowCount()).isOne();
    Row<MonitorQueryItem> foldableRow = row.getFoldableRow("KEY");
    assertThat(foldableRow.getCells()).isEqualTo(new int[] {item.getValue(), 0, 0, 0});
    assertThat(foldableRow.getTotalValue()).isEqualTo(item.getValue());
  }

  @Test
  void should_AppendItemValue_When_ItemIsInserted() {
    // when
    report.addItem(item);

    // then
    assertThat(report.getRows()).hasSize(1);
    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {item.getValue(), 0, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(item.getValue());
  }

  @Test
  void should_AppendItemValue_When_CellAlreadyContainsValue() {
    // when
    report.addItem(item);
    report.addItem(item);

    // then
    assertThat(report.getRows()).hasSize(1);
    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {2 * item.getValue(), 0, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(2 * item.getValue());
  }

  @Test
  void should_AppendItemValue_When_UsingBulkOperationToInsertItems() {
    // when
    report.addItems(List.of(item, item));

    // then
    assertThat(report.getRows()).hasSize(1);
    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {2 * item.getValue(), 0, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(2 * item.getValue());
  }

  @Test
  void should_PreProcessItem_When_PreprocessorIsDefined() {
    // given
    int overrideValue = 5;
    QueryItemPreprocessor<MonitorQueryItem> preprocessor =
        queryItem -> {
          queryItem.setNumberOfTasks(overrideValue);
          return queryItem;
        };
    item.setAgeInDays(1);

    // when
    report.addItem(item, preprocessor);

    // then
    assertThat(report.getRows()).hasSize(1);

    Row<MonitorQueryItem> row = report.getRow(item.getKey());
    assertThat(row.getCells()).isEqualTo(new int[] {0, overrideValue, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(overrideValue);

    Row<MonitorQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {0, overrideValue, 0, 0});
    assertThat(sumRow.getTotalValue()).isEqualTo(overrideValue);
  }

  @Test
  void should_PreProcessItem_When_PreprocessorIsDefinedForBulkInsert() {
    // given
    int overrideValue = 5;
    QueryItemPreprocessor<MonitorQueryItem> preprocessor =
        queryItem -> {
          queryItem.setNumberOfTasks(overrideValue);
          return queryItem;
        };
    // when
    report.addItems(List.of(item, item), preprocessor);

    // then
    assertThat(report.getRows()).hasSize(1);
    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {2 * overrideValue, 0, 0, 0});
    assertThat(row.getTotalValue()).isEqualTo(2 * overrideValue);
  }

  @Test
  void should_OnlyContainTotalRows_When_ReportContainsNoHeaders() {
    // given
    List<TimeIntervalColumnHeader> headerList = List.of();
    report =
        new MonitorQueryItemTimeIntervalColumnHeaderReport(headerList, new String[] {"rowDesc"});

    // when
    report.addItem(item);

    // then
    assertThat(report.getRows()).hasSize(1);
    assertThat(report.getRow("key").getCells()).isEqualTo(new int[0]);
    assertThat(report.getRow("key").getTotalValue()).isEqualTo(item.getValue());
  }

  @Test
  void should_NotInsertItem_When_ItemIsOutOfHeaderScope() {
    // given
    item.setAgeInDays(-2);
    // when
    report.addItem(item);

    // then
    assertThat(report.getRows()).isEmpty();
    Row<MonitorQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {0, 0, 0, 0});
    assertThat(sumRow.getTotalValue()).isZero();
  }

  @Test
  void should_InsertItemMultipleTimes_When_HeaderFitsMultipleTimes() {
    // given
    List<TimeIntervalColumnHeader> headers = new ArrayList<>(HEADERS);
    headers.add(new TimeIntervalColumnHeader(0, 3));
    report = new MonitorQueryItemTimeIntervalColumnHeaderReport(headers, new String[] {"rowDesc"});

    item.setAgeInDays(2);

    // when
    report.addItem(item);

    // then
    assertThat(report.getRows()).hasSize(1);

    Row<MonitorQueryItem> row = report.getRow("key");
    assertThat(row.getCells()).isEqualTo(new int[] {0, 0, item.getValue(), 0, item.getValue()});
    assertThat(row.getTotalValue()).isEqualTo(2 * item.getValue());

    Row<MonitorQueryItem> sumRow = report.getSumRow();
    assertThat(sumRow.getCells()).isEqualTo(new int[] {0, 0, item.getValue(), 0, item.getValue()});
    assertThat(sumRow.getTotalValue()).isEqualTo(2 * item.getValue());
  }

  @Test
  void should_FallBackToKey_When_DisplayMapDoesNotContainName() {
    report.augmentDisplayNames(new HashMap<>());

    assertThat(report.getSumRow().getDisplayName()).isEqualTo(report.getSumRow().getKey());
  }

  @Test
  void should_SetDisplayName_When_DisplayMapContainsName() {
    HashMap<String, String> displayMap = new HashMap<>();
    displayMap.put(report.getSumRow().getKey(), "BLA BLA");
    report.augmentDisplayNames(displayMap);

    assertThat(report.getSumRow().getDisplayName()).isEqualTo("BLA BLA");
  }

  @Test
  void should_SetDisplayNameForFoldableRows_When_DisplayMapContainsNames() {
    ReportWithFoldableRow reportWithFoldableRow =
        new ReportWithFoldableRow(HEADERS, new String[] {"totalDesc", "foldalbeRowDesc"});
    reportWithFoldableRow.addItem(item);

    HashMap<String, String> displayMap = new HashMap<>();
    displayMap.put("key", "displayname for key");
    displayMap.put("KEY", "displayname for KEY");
    reportWithFoldableRow.augmentDisplayNames(displayMap);

    FoldableTestRow row = reportWithFoldableRow.getRow("key");
    assertThat(row.getDisplayName()).isEqualTo("displayname for key");
    assertThat(row.getFoldableRow("KEY").getDisplayName()).isEqualTo("displayname for KEY");
  }

  private static class MonitorQueryItemTimeIntervalColumnHeaderReport
      extends Report<MonitorQueryItem, TimeIntervalColumnHeader> {

    public MonitorQueryItemTimeIntervalColumnHeaderReport(
        List<TimeIntervalColumnHeader> headerList, String[] rowDesc) {
      super(headerList, rowDesc);
    }
  }

  private static class ReportWithFoldableRow
      extends Report<MonitorQueryItem, TimeIntervalColumnHeader> {

    protected ReportWithFoldableRow(
        List<TimeIntervalColumnHeader> columnHeaders, String[] rowDesc) {
      super(columnHeaders, rowDesc);
    }

    @Override
    public FoldableTestRow getRow(String key) {
      return (FoldableTestRow) super.getRow(key);
    }

    @Override
    protected Row<MonitorQueryItem> createRow(String key, int columnSize) {
      return new FoldableTestRow(key, columnSize);
    }
  }

  private static class FoldableTestRow extends FoldableRow<MonitorQueryItem> {

    protected FoldableTestRow(String key, int columnSize) {
      super(key, columnSize, queryItem -> queryItem.getKey().toUpperCase());
    }

    @Override
    protected Row<MonitorQueryItem> buildRow(String key, int columnSize) {
      return new SingleRow<>(key, columnSize);
    }
  }
}
