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

package io.kadai.common.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.internal.util.spi.ServiceProviderInterface;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpiLoaderTest {

  @Test
  void should_loadServiceProviders() {
    List<ServiceProviderInterface> serviceProviders =
        SpiLoader.load(ServiceProviderInterface.class);

    assertThat(serviceProviders)
        .isNotEmpty()
        .extracting(ServiceProviderInterface::doStuff)
        .containsExactly("doing Stuff");
  }
}
