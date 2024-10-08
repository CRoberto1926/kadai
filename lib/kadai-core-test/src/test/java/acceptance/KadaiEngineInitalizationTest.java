/*
 * Copyright [2024] [envite consulting GmbH]
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

package acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.spi.priority.api.PriorityServiceProvider;
import io.kadai.spi.priority.internal.PriorityServiceManager;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import java.lang.reflect.Field;
import java.util.List;
import java.util.OptionalInt;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@KadaiIntegrationTest
public class KadaiEngineInitalizationTest {

  static class MyPriorityServiceProvider implements PriorityServiceProvider {

    private KadaiEngine kadaiEngine;

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      this.kadaiEngine = kadaiEngine;
    }

    @Override
    public OptionalInt calculatePriority(TaskSummary taskSummary) {
      return OptionalInt.empty();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @WithServiceProvider(
      serviceProviderInterface = PriorityServiceProvider.class,
      serviceProviders = MyPriorityServiceProvider.class)
  class PriorityServiceManagerTest {
    @KadaiInject KadaiEngineImpl kadaiEngine;

    @Test
    @SuppressWarnings("unchecked")
    void should_InitializePriorityServiceProviders() throws Exception {
      PriorityServiceManager priorityServiceManager = kadaiEngine.getPriorityServiceManager();
      Field priorityServiceProvidersField =
          PriorityServiceManager.class.getDeclaredField("priorityServiceProviders");
      priorityServiceProvidersField.setAccessible(true);
      List<PriorityServiceProvider> serviceProviders =
          (List<PriorityServiceProvider>) priorityServiceProvidersField.get(priorityServiceManager);

      assertThat(priorityServiceManager.isEnabled()).isTrue();
      assertThat(serviceProviders)
          .asInstanceOf(InstanceOfAssertFactories.LIST)
          .hasOnlyElementsOfType(MyPriorityServiceProvider.class)
          .extracting(MyPriorityServiceProvider.class::cast)
          .extracting(sp -> sp.kadaiEngine)
          .containsOnly(kadaiEngine);
    }
  }
}
