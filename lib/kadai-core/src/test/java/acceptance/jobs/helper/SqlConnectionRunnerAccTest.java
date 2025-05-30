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

package acceptance.jobs.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.AbstractAccTest;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.task.internal.jobs.helper.SqlConnectionRunner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Acceptance test for all "jobs tasks runner" scenarios. */
@ExtendWith(JaasExtension.class)
class SqlConnectionRunnerAccTest extends AbstractAccTest {

  @Test
  void should_executeSimpleQuery() {
    // given
    SqlConnectionRunner runner = new SqlConnectionRunner(kadaiEngine);
    String taskId = "TKI:000000000000000000000000000000000050";

    // when
    runner.runWithConnection(
        connection -> {
          PreparedStatement preparedStatement =
              connection.prepareStatement("select * from TASK where ID = ?");
          preparedStatement.setString(1, taskId);
          ResultSet resultSet = preparedStatement.executeQuery();
          // then
          assertThat(resultSet.next()).isTrue();
        });
  }

  @Test
  void should_catchSqlExceptionAndThrowSystemException() {
    // given
    SqlConnectionRunner runner = new SqlConnectionRunner(kadaiEngine);
    SQLException expectedException = new SQLException("test");

    // when
    ThrowingCallable code =
        () ->
            runner.runWithConnection(
                connection -> {
                  throw expectedException;
                });

    // then
    assertThatThrownBy(code).isInstanceOf(SystemException.class).hasCause(expectedException);
  }
}
