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

package io.kadai.classification.rest;

import static io.kadai.common.api.SharedConstants.MASTER_DOMAIN;
import static io.kadai.rest.test.RestHelper.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.classification.rest.models.ClassificationRepresentationModel;
import io.kadai.classification.rest.models.ClassificationSummaryPagedRepresentationModel;
import io.kadai.classification.rest.models.ClassificationSummaryRepresentationModel;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpStatusCodeException;

/** Test {@link ClassificationController}. */
@KadaiSpringBootTest
class ClassificationControllerIntTest {

  private final RestHelper restHelper;

  @Autowired
  ClassificationControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @Test
  void should_ReturnAllMasterClassifications_When_DomainIsSetWithEmptyString() {
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS) + "?domain=";

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(ClassificationSummaryRepresentationModel::getDomain)
        .containsOnly(MASTER_DOMAIN);
  }

  @Test
  void testGetClassification() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_CLASSIFICATIONS_ID, "CLI:100000000000000000000000000000000002");

    ResponseEntity<ClassificationRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaTypes.HAL_JSON);
  }

  @Test
  void testGetAllClassifications() {
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
  }

  @Test
  void testGetAllClassificationsFilterByCustomAttribute() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS) + "?domain=DOMAIN_A&custom-1-like=RVNR";

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(33);
  }

  @Test
  void testGetAllClassificationsKeepingFilters() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS)
            + "?domain=DOMAIN_A&sort-by=KEY&order=ASCENDING";

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
        .endsWith("/api/v1/classifications?domain=DOMAIN_A&sort-by=KEY&order=ASCENDING");
    assertThat(response.getBody().getContent()).hasSize(37);
    assertThat(response.getBody().getContent().iterator().next().getKey()).isEqualTo("A12");
  }

  @Test
  void testGetSecondPageSortedByKey() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS)
            + "?domain=DOMAIN_A&sort-by=KEY&order=ASCENDING&page-size=5&page=2";

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(5);
    assertThat(response.getBody().getContent().iterator().next().getKey()).isEqualTo("L1050");
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
        .endsWith(
            "/api/v1/classifications?"
                + "domain=DOMAIN_A&sort-by=KEY&order=ASCENDING&page-size=5&page=2");
    assertThat(response.getBody().getLink(IanaLinkRelations.FIRST)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.LAST)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.NEXT)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.PREV)).isNotNull();
  }

  @Test
  @DirtiesContext
  void testCreateClassification() {
    ClassificationRepresentationModel newClassification = new ClassificationRepresentationModel();
    newClassification.setType("TASK");
    newClassification.setCategory("MANUAL");
    newClassification.setDomain("DOMAIN_A");
    newClassification.setKey("NEW_CLASS");
    newClassification.setServiceLevel("P1D");
    newClassification.setName("new classification");
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ResponseEntity<ClassificationRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .body(newClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    newClassification.setKey("NEW_CLASS_2");

    responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .body(newClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  @DirtiesContext
  void should_ThrowNotAuthorized_When_UserIsNotInRoleAdminOrBusinessAdmin_whileCreating() {
    ClassificationRepresentationModel newClassification = new ClassificationRepresentationModel();
    newClassification.setType("TASK");
    newClassification.setCategory("MANUAL");
    newClassification.setDomain("DOMAIN_A");
    newClassification.setKey("NEW_CLASS");
    newClassification.setName("new classification");

    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .post()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .body(newClassification)
                .retrieve()
                .toEntity(ClassificationRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DirtiesContext
  void testCreateClassificationWithParentId() {
    ClassificationRepresentationModel newClassification = new ClassificationRepresentationModel();
    newClassification.setType("TASK");
    newClassification.setCategory("MANUAL");
    newClassification.setDomain("DOMAIN_B");
    newClassification.setKey("NEW_CLASS_P1");
    newClassification.setName("new classification");
    newClassification.setServiceLevel("P1D");
    newClassification.setParentId("CLI:200000000000000000000000000000000015");
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ResponseEntity<ClassificationRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .body(newClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  @DirtiesContext
  void testCreateClassificationWithParentKey() {
    ClassificationRepresentationModel newClassification = new ClassificationRepresentationModel();
    newClassification.setType("TASK");
    newClassification.setCategory("MANUAL");
    newClassification.setDomain("DOMAIN_B");
    newClassification.setKey("NEW_CLASS_P1");
    newClassification.setName("new classification");
    newClassification.setParentKey("T2100");
    newClassification.setServiceLevel("P1D");
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ResponseEntity<ClassificationRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .body(newClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  @DirtiesContext
  void testCreateClassificationWithParentKeyInDomain_aShouldCreateAClassificationInRootDomain() {
    ClassificationRepresentationModel newClassification = new ClassificationRepresentationModel();
    newClassification.setType("TASK");
    newClassification.setCategory("MANUAL");
    newClassification.setDomain("DOMAIN_A");
    newClassification.setKey("NEW_CLASS_P2");
    newClassification.setName("new classification");
    newClassification.setParentKey("T2100");
    newClassification.setServiceLevel("P1D");
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ResponseEntity<ClassificationRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .body(newClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);
    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<ClassificationSummaryPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    boolean foundClassificationCreated = false;
    for (ClassificationSummaryRepresentationModel classification :
        response.getBody().getContent()) {
      if ("NEW_CLASS_P2".equals(classification.getKey())
          && MASTER_DOMAIN.equals(classification.getDomain())
          && "T2100".equals(classification.getParentKey())) {
        foundClassificationCreated = true;
        break;
      }
    }

    assertThat(foundClassificationCreated).isTrue();
  }

  @Test
  @DirtiesContext
  void testReturn400IfCreateClassificationWithIncompatibleParentIdAndKey() {
    String newClassification =
        "{\"classificationId\":\"\",\"category\":\"MANUAL\",\"domain\":\"DOMAIN_B\","
            + "\"key\":\"NEW_CLASS_P3\",\"name\":\"new classification\","
            + "\"type\":\"TASK\",\"parentId\":\"CLI:200000000000000000000000000000000015\","
            + "\"parentKey\":\"T2000\",\"serviceLevel\":\"P1D\"}";
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .post()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .body(newClassification)
                .retrieve()
                .toEntity(ClassificationRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DirtiesContext
  void testCreateClassificationWithClassificationIdReturnsError400() {
    String newClassification =
        "{\"classificationId\":\"someId\",\"category\":\"MANUAL\","
            + "\"domain\":\"DOMAIN_A\",\"key\":\"NEW_CLASS\","
            + "\"name\":\"new classification\",\"type\":\"TASK\",\"serviceLevel\":\"P1D\"}";
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS);

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .post()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .body(newClassification)
                .retrieve()
                .toEntity(ClassificationRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testGetClassificationWithSpecialCharacter() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_CLASSIFICATIONS_ID, "CLI:100000000000000000000000000000000009");

    ResponseEntity<ClassificationSummaryRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ClassificationSummaryRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("Zustimmungserklärung");
  }

  @Test
  @DirtiesContext
  void testDeleteClassification() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_CLASSIFICATIONS_ID, "CLI:200000000000000000000000000000000004");

    ResponseEntity<ClassificationSummaryRepresentationModel> response =
        CLIENT
            .delete()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
            .retrieve()
            .toEntity(ClassificationSummaryRepresentationModel.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .retrieve()
                .toEntity(ClassificationSummaryRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DirtiesContext
  void should_ReturnStatusCodeLocked_When_DeletingClassificationInUse() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_CLASSIFICATIONS_ID, "CLI:000000000000000000000000000000000003");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .delete()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .retrieve()
                .toEntity(ClassificationSummaryRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.LOCKED);
  }

  @Test
  void should_ThrowException_When_ProvidingInvalidFilterParams() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS)
            + "?domain=DOMAIN_A"
            + "&illegalParam=illegal"
            + "&anotherIllegalParam=stillIllegal"
            + "&sort-by=NAME&order=DESCENDING&page-size=5&page=2";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(ClassificationSummaryRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining(
            "Unknown request parameters found: [anotherIllegalParam, illegalParam]")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_UpdateClassification_When_NameIsChanged() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_CLASSIFICATIONS_ID, "CLI:000000000000000000000000000000000004");
    ResponseEntity<ClassificationRepresentationModel> responseGet =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);

    final ClassificationRepresentationModel originalClassification = responseGet.getBody();
    originalClassification.setName("new name");

    ResponseEntity<ClassificationRepresentationModel> responseUpdate =
        CLIENT
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .body(originalClassification)
            .retrieve()
            .toEntity(ClassificationRepresentationModel.class);

    ClassificationRepresentationModel updatedClassification = responseUpdate.getBody();
    assertThat(updatedClassification).isNotNull();
    assertThat(updatedClassification.getName()).isEqualTo("new name");
  }
}
