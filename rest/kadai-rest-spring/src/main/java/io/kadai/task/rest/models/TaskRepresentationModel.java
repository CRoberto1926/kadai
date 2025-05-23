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

package io.kadai.task.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kadai.task.api.models.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

@Schema(description = "EntityModel class for Task")
@JsonIgnoreProperties("attachmentSummaries")
public class TaskRepresentationModel extends TaskSummaryRepresentationModel {

  // All objects have to be serializable
  @Schema(name = "customAttributes", description = "Additional information of the task.")
  private List<CustomAttribute> customAttributes = Collections.emptyList();

  @Schema(name = "callbackInfo", description = "Callback Information of the task.")
  private List<CustomAttribute> callbackInfo = Collections.emptyList();

  @Schema(name = "attachments", description = "Attachments of the task.")
  private List<AttachmentRepresentationModel> attachments = new ArrayList<>();

  public List<CustomAttribute> getCustomAttributes() {
    return customAttributes;
  }

  public void setCustomAttributes(List<CustomAttribute> customAttributes) {
    this.customAttributes = customAttributes;
  }

  public List<CustomAttribute> getCallbackInfo() {
    return callbackInfo;
  }

  public void setCallbackInfo(List<CustomAttribute> callbackInfo) {
    this.callbackInfo = callbackInfo;
  }

  public List<AttachmentRepresentationModel> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<AttachmentRepresentationModel> attachments) {
    this.attachments = attachments;
  }

  /**
   * A CustomAttribute is a user customized attribute which is saved as a Map and can be retreived
   * from either {@link Task#getCustomAttributeMap()} or {@link Task#getCallbackInfo()}.
   */
  @Schema(description = "A CustomAttribute is a user customized attribute which is saved as a Map.")
  public static class CustomAttribute {

    @Schema(name = "key", description = "the key of the custom attribute.")
    private String key;

    @Schema(name = "value", description = "the value of the custom attribute.")
    private String value;

    public static CustomAttribute of(Entry<String, String> entry) {
      return of(entry.getKey(), entry.getValue());
    }

    public static CustomAttribute of(String key, String value) {
      CustomAttribute customAttribute = new CustomAttribute();
      customAttribute.setKey(key);
      customAttribute.setValue(value);
      return customAttribute;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
