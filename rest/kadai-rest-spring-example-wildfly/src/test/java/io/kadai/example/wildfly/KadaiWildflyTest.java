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

package io.kadai.example.wildfly;

import static io.kadai.common.test.rest.RestHelper.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.models.AccessIdRepresentationModel;
import io.kadai.common.rest.models.KadaiUserInfoRepresentationModel;
import io.kadai.common.test.rest.RestHelper;
import io.kadai.task.rest.models.TaskRepresentationModel;
import java.io.File;
import java.util.List;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This test class is configured to run with postgres DB. In order to run them on db2, change the
 * datasource.jndi to java:jboss/datasources/ExampleDS and set kadai.schemaName to KADAI
 */
@RunWith(Arquillian.class)
public class KadaiWildflyTest extends AbstractAccTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KadaiWildflyTest.class);

  @Deployment(testable = false)
  public static Archive<?> createTestArchive() {

    String applicationPropertyFile = "application.properties";
    String dbType = System.getProperty("db.type");
    if (dbType != null && !dbType.isEmpty()) {
      applicationPropertyFile = "application-" + dbType + ".properties";
    }

    LOGGER.info(
        "Running with db.type '{}' and using property file '{}'", dbType, applicationPropertyFile);

    File[] files =
        Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importCompileAndRuntimeDependencies()
            .resolve()
            .withTransitivity()
            .asFile();

    return ShrinkWrap.create(WebArchive.class, "kadai.war")
        .addPackages(true, "io.kadai")
        .addAsResource("kadai.properties")
        .addAsResource(applicationPropertyFile, "application.properties")
        .addAsResource("kadai-test.ldif")
        .addAsWebInfResource("int-test-web.xml", "web.xml")
        .addAsWebInfResource("int-test-jboss-web.xml", "jboss-web.xml")
        .addAsLibraries(files);
  }

  @Test
  @RunAsClient
  public void should_ReturnUserInformationForAuthenticatedUser_IfRequested() {
    ResponseEntity<KadaiUserInfoRepresentationModel> response =
        TEMPLATE.exchange(
            restHelper.toUrl("/kadai" + RestEndpoints.URL_CURRENT_USER),
            HttpMethod.GET,
            new HttpEntity<>(RestHelper.generateHeadersForUser("teamlead-1")),
            ParameterizedTypeReference.forType(KadaiUserInfoRepresentationModel.class));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    KadaiUserInfoRepresentationModel currentUser = response.getBody();
    assertThat(currentUser).isNotNull();
    assertThat(currentUser.getUserId()).isEqualTo("teamlead-1");
    assertThat(currentUser.getGroupIds()).hasSize(4);
    assertThat(currentUser.getRoles()).hasSize(3);
  }

  @Test
  @RunAsClient
  public void should_ReturnUserFromLdap_When_WildcardSearchIsConducted() {
    ResponseEntity<List<AccessIdRepresentationModel>> response =
        TEMPLATE.exchange(
            restHelper.toUrl("/kadai" + RestEndpoints.URL_ACCESS_ID + "?search-for=rig"),
            HttpMethod.GET,
            new HttpEntity<>(RestHelper.generateHeadersForUser("teamlead-1")),
            new ParameterizedTypeReference<List<AccessIdRepresentationModel>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
  }

  @Test
  @RunAsClient
  public void should_ReturnTask_When_Requested() {
    ResponseEntity<TaskRepresentationModel> response =
        TEMPLATE.exchange(
            restHelper.toUrl(
                "/kadai" + RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000001"),
            HttpMethod.GET,
            new HttpEntity<>(RestHelper.generateHeadersForUser("teamlead-1")),
            ParameterizedTypeReference.forType(TaskRepresentationModel.class));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }
}
