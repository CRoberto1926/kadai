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

package acceptance.classification.query;

import static io.kadai.classification.api.ClassificationCustomField.CUSTOM_1;
import static io.kadai.common.api.BaseQuery.SortDirection.ASCENDING;
import static io.kadai.common.api.BaseQuery.SortDirection.DESCENDING;
import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.security.WithAccessId;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.ThrowingConsumer;

/**
 * The QueryClassificationsWithSortingAccTest for all "query classification with sorting" scenarios.
 *
 * <p>Note: Instead of working with a clean database for each test, we create a scenario. In that
 * scenario we use {@linkplain ClassificationCustomField#CUSTOM_1} to filter out all the other
 * created {@linkplain Classification Classifications}. Therefore, all tests regarding {@linkplain
 * ClassificationCustomField ClassificationCustomFields} are executed first.
 *
 * <p>The alternative is to create a "big" scenario as a test setup. But that would result to the
 * same issues we have with the sql file.
 */
@KadaiIntegrationTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class ClassificationQuerySortingAccTest {

  @KadaiInject ClassificationService classificationService;

  @WithAccessId(user = "businessadmin")
  @TestFactory
  @Order(1)
  Stream<DynamicTest> should_FindClassifications_When_QueryingForCustomAttributeIn() {
    String testIdentifier = UUID.randomUUID().toString();

    Iterator<ClassificationCustomField> customFieldIterator =
        Arrays.stream(ClassificationCustomField.values()).iterator();
    ThrowingConsumer<ClassificationCustomField> test =
        customField -> {
          ClassificationSummary classificationSummary =
              defaultTestClassification()
                  .customAttribute(customField, testIdentifier)
                  .buildAndStoreAsSummary(classificationService);

          List<ClassificationSummary> classificationSummaryList =
              classificationService
                  .createClassificationQuery()
                  .customAttributeIn(customField, testIdentifier)
                  .list();

          ClassificationSummary masterClassificationSummary =
              classificationService
                  .getClassification(classificationSummary.getKey(), "")
                  .asSummary();
          assertThat(classificationSummaryList)
              .containsExactlyInAnyOrder(classificationSummary, masterClassificationSummary);
        };
    return DynamicTest.stream(
        customFieldIterator, c -> String.format("for %s", c.toString()), test);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByKeyAsc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByKey(ASCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getKey)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByParentKeyDesc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    ClassificationSummary parentClassificationSummary1 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    ClassificationSummary parentClassificationSummary2 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentKey(parentClassificationSummary1.getKey())
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentKey(parentClassificationSummary2.getKey())
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByParentKey(DESCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getParentKey)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByParentKeyAsc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    ClassificationSummary parentClassificationSummary1 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    ClassificationSummary parentClassificationSummary2 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentKey(parentClassificationSummary1.getKey())
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentKey(parentClassificationSummary2.getKey())
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByParentKey(ASCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getParentKey)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByParentIdDesc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    ClassificationSummary parentClassificationSummary1 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    ClassificationSummary parentClassificationSummary2 =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentId(parentClassificationSummary1.getId())
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .parentId(parentClassificationSummary2.getId())
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByParentId(DESCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getParentId)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByCategoryDesc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .category("MANUAL")
        .type("TASK")
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .category("EXTERNAL")
        .type("TASK")
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByCategory(DESCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getCategory)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByDomainAsc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .domain("DOMAIN_A")
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .domain("DOMAIN_B")
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByDomain(ASCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getDomain)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByPriorityDesc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .priority(1)
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .priority(99)
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByPriority(DESCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .isSortedAccordingTo(
            Comparator.comparingInt(ClassificationSummary::getPriority).reversed());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByNameAsc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .name("A")
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .name("B")
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByName(ASCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getName)
        .isSortedAccordingTo(Collator.getInstance(Locale.GERMANY));
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByServiceLevelDesc() throws Exception {
    String testIdentifier = UUID.randomUUID().toString();

    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .serviceLevel("P1D")
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .serviceLevel("P99D")
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByServiceLevel(DESCENDING)
            .list();

    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getServiceLevel)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
  }

  @WithAccessId(user = "businessadmin")
  @Test
  void should_OrderClassifications_When_QueryingForOrderByApplicationEntryPointAsc()
      throws Exception {
    String testIdentifier = UUID.randomUUID().toString();
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .applicationEntryPoint("A")
        .buildAndStoreAsSummary(classificationService);
    defaultTestClassification()
        .customAttribute(CUSTOM_1, testIdentifier)
        .applicationEntryPoint("B")
        .buildAndStoreAsSummary(classificationService);

    List<ClassificationSummary> classificationSummaryList =
        classificationService
            .createClassificationQuery()
            .customAttributeIn(CUSTOM_1, testIdentifier)
            .orderByApplicationEntryPoint(ASCENDING)
            .list();

    // Ensure that there are enough elements to compare
    assertThat(classificationSummaryList)
        .hasSize(4)
        .extracting(ClassificationSummary::getApplicationEntryPoint)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
  }

  @WithAccessId(user = "businessadmin")
  @TestFactory
  Stream<DynamicTest> should_OrderClassifications_When_QueryingForOrderByCustomAttributesAsc() {
    String testIdentifier = UUID.randomUUID().toString();
    Iterator<ClassificationCustomField> customFieldIterator =
        Arrays.stream(ClassificationCustomField.values()).iterator();
    ThrowingConsumer<ClassificationCustomField> test =
        customField -> {
          defaultTestClassification()
              .customAttribute(customField, "A" + testIdentifier)
              .buildAndStoreAsSummary(classificationService);
          defaultTestClassification()
              .customAttribute(customField, "B" + testIdentifier)
              .buildAndStoreAsSummary(classificationService);

          List<ClassificationSummary> classificationSummaryList =
              classificationService
                  .createClassificationQuery()
                  .customAttributeIn(customField, "A" + testIdentifier, "B" + testIdentifier)
                  .orderByCustomAttribute(customField, ASCENDING)
                  .list();

          // Ensure that there are enough elements to compare
          assertThat(classificationSummaryList)
              .hasSize(4)
              .extracting(c -> c.getCustomField(customField))
              .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
        };
    return DynamicTest.stream(
        customFieldIterator, c -> String.format("for %s", c.toString()), test);
  }

  @WithAccessId(user = "businessadmin")
  @TestFactory
  Stream<DynamicTest> should_OrderClassifications_When_QueryingForOrderByCustomAttributesDesc() {
    String testIdentifier = UUID.randomUUID().toString();
    Iterator<ClassificationCustomField> customFieldIterator =
        Arrays.stream(ClassificationCustomField.values()).iterator();
    ThrowingConsumer<ClassificationCustomField> test =
        customField -> {
          defaultTestClassification()
              .customAttribute(customField, "A" + testIdentifier)
              .buildAndStoreAsSummary(classificationService);
          defaultTestClassification()
              .customAttribute(customField, "B" + testIdentifier)
              .buildAndStoreAsSummary(classificationService);

          List<ClassificationSummary> classificationSummaryList =
              classificationService
                  .createClassificationQuery()
                  .customAttributeIn(customField, "A" + testIdentifier, "B" + testIdentifier)
                  .orderByCustomAttribute(customField, DESCENDING)
                  .list();

          // Ensure that there are enough elements to compare
          assertThat(classificationSummaryList)
              .hasSize(4)
              .extracting(c -> c.getCustomField(customField))
              .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
        };
    return DynamicTest.stream(
        customFieldIterator, c -> String.format("for %s", c.toString()), test);
  }
}
