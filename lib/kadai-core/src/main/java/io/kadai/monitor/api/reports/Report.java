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

package io.kadai.monitor.api.reports;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.monitor.api.reports.header.ColumnHeader;
import io.kadai.monitor.api.reports.item.QueryItem;
import io.kadai.monitor.api.reports.item.QueryItemPreprocessor;
import io.kadai.monitor.api.reports.row.Row;
import io.kadai.monitor.api.reports.row.SingleRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Report represents an abstract table that consists of {@linkplain Row}s and a list of
 * &lt;ColumnHeader&gt;s. Since a report does not specify &lt;Item&gt; and &lt;ColumnHeader&gt; it
 * does not contain functional logic. Due to readability implicit definition of functional logic is
 * prevented and thus prevent initialization of an abstract Report. In order to create a specific
 * Report a subclass has to be created.
 *
 * @param <I> {@linkplain QueryItem} whose value is relevant for this report.
 * @param <H> {@linkplain ColumnHeader} which can determine if an &lt;Item&gt; belongs into that
 *     column or not.
 */
public abstract class Report<I extends QueryItem, H extends ColumnHeader<? super I>> {

  private final Map<String, Row<I>> reportRows = new LinkedHashMap<>();
  private final Row<I> sumRow;
  private final String[] rowDesc;
  protected List<H> columnHeaders;

  protected Report(List<H> columnHeaders, String[] rowDesc) {
    this.rowDesc = rowDesc;
    this.columnHeaders = new ArrayList<>(columnHeaders);
    sumRow = createRow("Total");
  }

  public final Map<String, Row<I>> getRows() {
    return reportRows;
  }

  public final Row<I> getSumRow() {
    return sumRow;
  }

  public final List<H> getColumnHeaders() {
    return columnHeaders;
  }

  public final String[] getRowDesc() {
    return rowDesc;
  }

  public Row<I> getRow(String key) {
    return reportRows.get(key);
  }

  public final Set<String> rowTitles() {
    return reportRows.keySet();
  }

  public final int rowSize() {
    return reportRows.size();
  }

  public final void addItem(I item) {
    Row<I> row = null;
    if (columnHeaders.isEmpty()) {
      row = reportRows.computeIfAbsent(item.getKey(), this::createRow);
      row.updateTotalValue(item);
      sumRow.updateTotalValue(item);
    } else {
      for (int i = 0; i < columnHeaders.size(); i++) {
        if (columnHeaders.get(i).fits(item)) {
          if (row == null) {
            row = reportRows.computeIfAbsent(item.getKey(), this::createRow);
          }
          row.addItem(item, i);
          sumRow.addItem(item, i);
        }
      }
    }
  }

  public final void addItem(I item, QueryItemPreprocessor<I> preprocessor) {
    addItem(preprocessor.apply(item));
  }

  public final void addItems(List<? extends I> items, QueryItemPreprocessor<I> preprocessor) {
    items.stream().map(preprocessor::apply).forEach(this::addItem);
  }

  public final void addItems(List<I> items) {
    items.forEach(this::addItem);
  }

  public final void augmentDisplayNames(Map<String, String> displayMap) {
    reportRows.values().forEach(row -> row.setDisplayName(displayMap));
    sumRow.setDisplayName(displayMap);
  }

  public final Row<I> createRow(String key) {
    return createRow(key, columnHeaders.size());
  }

  protected Row<I> createRow(String key, int columnSize) {
    return new SingleRow<>(key, columnSize);
  }

  @Override
  public String toString() {
    return "Report [reportRows="
        + reportRows
        + ", sumRow="
        + sumRow
        + ", rowDesc="
        + Arrays.toString(rowDesc)
        + ", columnHeaders="
        + columnHeaders
        + "]";
  }

  /**
   * Builder for {@linkplain Report}.
   *
   * @param <I> {@linkplain QueryItem} whose value is relevant for this report.
   * @param <H> {@linkplain ColumnHeader} which can determine if an &lt;Item&gt; belongs into that
   *     column or not.
   */
  public interface Builder<I extends QueryItem, H extends ColumnHeader<? super I>> {

    Report<I, H> buildReport() throws InvalidArgumentException, NotAuthorizedException;
  }
}
