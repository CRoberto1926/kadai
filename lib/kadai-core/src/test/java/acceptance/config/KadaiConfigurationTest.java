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

package acceptance.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.CustomHoliday;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.WrongCustomHolidayFormatException;
import io.kadai.common.test.config.DataSourceGenerator;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class KadaiConfigurationTest {

  @Test
  void should_ReturnKadaiEngine_When_BuildingWithConfiguration() throws Exception {
    DataSource ds = DataSourceGenerator.getDataSource();
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(ds, false, DataSourceGenerator.getSchemaName())
            .initKadaiProperties()
            .build();

    KadaiEngine te = KadaiEngine.buildKadaiEngine(configuration);

    assertThat(te).isNotNull();
  }

  @Test
  void should_SetCorpusChristiEnabled_When_PropertyIsSet() {
    DataSource ds = DataSourceGenerator.getDataSource();
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(ds, false, DataSourceGenerator.getSchemaName(), true)
            .initKadaiProperties("/corpusChristiEnabled.properties", "|")
            .build();

    assertThat(configuration.isGermanPublicHolidaysCorpusChristiEnabled()).isTrue();
  }

  @Test
  void should_ReturnTheTwoCustomHolidays_When_TwoCustomHolidaysAreConfiguredInThePropertiesFile() {
    DataSource ds = DataSourceGenerator.getDataSource();
    KadaiConfiguration configuration =
        new KadaiConfiguration.Builder(ds, false, DataSourceGenerator.getSchemaName(), true)
            .initKadaiProperties("/custom_holiday_kadai.properties", "|")
            .build();
    assertThat(configuration.getCustomHolidays())
        .contains(CustomHoliday.of(31, 7), CustomHoliday.of(16, 12));
  }

  @Test
  void should_ThrowError_When_AnyCustomHolidayIsInWrongFormatInPropertiesFile() {
    DataSource ds = DataSourceGenerator.getDataSource();
    KadaiConfiguration.Builder builder =
        new KadaiConfiguration.Builder(ds, false, DataSourceGenerator.getSchemaName(), true);

    ThrowingCallable call =
        () ->
            builder.initKadaiProperties("/custom_holiday_with_wrong_format_kadai.properties", "|");

    assertThatThrownBy(call)
        .isInstanceOf(WrongCustomHolidayFormatException.class)
        .extracting(WrongCustomHolidayFormatException.class::cast)
        .extracting(WrongCustomHolidayFormatException::getCustomHoliday)
        .isEqualTo("31,07");
  }
}
