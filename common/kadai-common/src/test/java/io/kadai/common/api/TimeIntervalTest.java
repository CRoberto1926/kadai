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

package io.kadai.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeIntervalTest {

  private static final ZoneId UTC = ZoneId.of("UTC");
  private final Instant date1 = LocalDate.of(2023, 2, 10).atStartOfDay(UTC).toInstant();
  private final Instant date2 = LocalDate.of(2023, 2, 13).atStartOfDay(UTC).toInstant();

  @Test
  void should_BeAValidIntervall_when_BeginIsBeforEnd() {
    TimeInterval timeInterval = new TimeInterval(date1, date2);

    assertThat(timeInterval.isValid()).isTrue();
  }

  @Test
  void should_BeAValidIntervall_when_BeginAndEndAreEqual() {
    TimeInterval timeInterval = new TimeInterval(date1, date1);

    assertThat(timeInterval.isValid()).isTrue();
  }

  @Test
  void should_NotBeAValidIntervall_when_BeginIsAfterEnd() {
    TimeInterval timeInterval = new TimeInterval(date2, date1);

    assertThat(timeInterval.isValid()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {10, 11, 12, 13})
  void should_ContainDateInIntervall(int day) {
    TimeInterval timeInterval = new TimeInterval(date1, date2);

    Instant actualInstant = LocalDate.of(2023, 2, day).atStartOfDay(UTC).toInstant();

    assertThat(timeInterval.contains(actualInstant)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {8, 9, 14, 15})
  void should_NotContainDateInIntervall_when_InstantIsBeforeOrAfter(int day) {
    TimeInterval timeInterval = new TimeInterval(date1, date2);

    Instant actualInstant = LocalDate.of(2023, 2, day).atStartOfDay(UTC).toInstant();

    assertThat(timeInterval.contains(actualInstant)).isFalse();
  }

  @Test
  void should_TwoIntervallsAreEqual_when_BothHaveSameBeginAndEnd() {
    TimeInterval timeInterval1 = new TimeInterval(date1, date2);
    TimeInterval timeInterval2 = new TimeInterval(date1, date2);

    assertThat(timeInterval1).isEqualTo(timeInterval2);
    assertThat(timeInterval2).isEqualTo(timeInterval1);
  }

  @Test
  void should_TwoIntervallsAreNotEqual_when_BeginAndEndAreDifferent() {
    TimeInterval timeInterval1 = new TimeInterval(date1, date2);
    TimeInterval timeInterval2 =
        new TimeInterval(date1, LocalDate.of(2023, 2, 14).atStartOfDay(UTC).toInstant());

    assertThat(timeInterval1).isNotEqualTo(timeInterval2);
    assertThat(timeInterval2).isNotEqualTo(timeInterval1);
  }
}
