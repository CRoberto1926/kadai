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

package io.kadai.simplehistory.impl.classification;

import io.kadai.common.api.QueryColumnName;

/** Enum containing the column names for {@link ClassificationHistoryQueryMapper}. */
public enum ClassificationHistoryQueryColumnName implements QueryColumnName {
  ID("id"),
  EVENT_TYPE("event_type"),
  CREATED("created"),
  USER_ID("user_id"),
  CLASSIFICATION_ID("classification_id"),
  APPLICATION_ENTRY_POINT("application_entry_point"),
  CATEGORY("category"),
  DOMAIN("domain"),
  KEY("key"),
  NAME("name"),
  PARENT_ID("parent_id"),
  PARENT_KEY("parent_key"),
  PRIORITY("priority"),
  SERVICE_LEVEL("service_level"),
  TYPE("type"),
  CUSTOM_1("custom_1"),
  CUSTOM_2("custom_2"),
  CUSTOM_3("custom_3"),
  CUSTOM_4("custom_4"),
  CUSTOM_5("custom_5"),
  CUSTOM_6("custom_6"),
  CUSTOM_7("custom_7"),
  CUSTOM_8("custom_8");

  private final String name;

  ClassificationHistoryQueryColumnName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
