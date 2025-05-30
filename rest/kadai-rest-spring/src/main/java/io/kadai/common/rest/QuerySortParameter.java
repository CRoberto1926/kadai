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

package io.kadai.common.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.BaseQuery;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

public class QuerySortParameter<Q extends BaseQuery<?, ?>, S extends QuerySortBy<Q>>
    implements QueryParameter<Q, Void> {


  @Parameter(
      name = "sort-by",
      description =
          "Sort the result by a given field. Multiple sort values can be declared. When the "
              + "primary sort value is the same, the second one will be used.")
  @JsonProperty("sort-by")
  private final List<S> sortBy;

  @Parameter(
      name = "order",
      description =
          "The order direction for each sort value. This value requires the use of 'sort-by'. The"
              + " amount of sort-by and order declarations have to match. Alternatively the value"
              + " can be omitted. If done so the default sort order (ASCENDING) will be applied to"
              + " every sort-by value.")
  @JsonProperty("order")
  private final List<SortDirection> order;

  public QuerySortParameter(List<S> sortBy, List<SortDirection> order)
      throws InvalidArgumentException {
    this.sortBy = sortBy;
    this.order = order;
    verifyNotOnlyOrderByExists(sortBy, order);
    verifyAmountOfSortByAndOrderByMatches(sortBy, order);
  }

  // this method is only static because there exists no query for the task comment entity
  public static <T> void verifyAmountOfSortByAndOrderByMatches(
      List<T> sortBy, List<SortDirection> order) throws InvalidArgumentException {
    if (sortBy != null && order != null && sortBy.size() != order.size() && !order.isEmpty()) {
      throw new InvalidArgumentException(
          "The amount of 'sort-by' and 'order' does not match. "
              + "Please specify an 'order' for each 'sort-by' or no 'order' parameters at all.");
    }
  }

  // this method is only static because there exists no query for the task comment entity
  public static <T> void verifyNotOnlyOrderByExists(List<T> sortBy, List<SortDirection> order)
      throws InvalidArgumentException {
    if (sortBy == null && order != null) {
      throw new InvalidArgumentException(
          "Only 'order' parameters were provided. Please also provide 'sort-by' parameter(s)");
    }
  }

  @Override
  public Void apply(Q query) {
    if (sortBy != null) {
      for (int i = 0; i < sortBy.size(); i++) {
        SortDirection sortDirection =
            order == null || order.isEmpty() ? SortDirection.ASCENDING : order.get(i);
        sortBy.get(i).applySortByForQuery(query, sortDirection);
      }
    }
    return null;
  }

  @JsonProperty("sort-by")
  public List<S> getSortBy() {
    return sortBy;
  }

  public List<SortDirection> getOrder() {
    return order;
  }
}
