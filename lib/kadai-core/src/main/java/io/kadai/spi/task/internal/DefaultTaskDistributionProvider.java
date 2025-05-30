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

package io.kadai.spi.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.task.api.TaskDistributionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultTaskDistributionProvider implements TaskDistributionProvider {

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    // NOOP
  }

  @Override
  public Map<String, List<String>> distributeTasks(
      List<String> taskIds, List<String> workbasketIds, Map<String, Object> additionalInformation) {

    if (taskIds == null || taskIds.isEmpty()) {
      throw new IllegalArgumentException("Task Ids list cannot be null or empty.");
    }
    if (workbasketIds == null || workbasketIds.isEmpty()) {
      throw new IllegalArgumentException("Ids of destinationWorkbaskets cannot be null or empty.");
    }

    Map<String, List<String>> distributedTaskIds =
        workbasketIds.stream().collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));

    int workbasketCount = workbasketIds.size();
    IntStream.range(0, taskIds.size())
        .forEach(
            i ->
                distributedTaskIds.get(workbasketIds.get(i % workbasketCount)).add(taskIds.get(i)));

    return distributedTaskIds;
  }
}
