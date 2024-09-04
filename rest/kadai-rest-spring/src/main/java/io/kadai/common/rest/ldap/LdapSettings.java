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

package io.kadai.common.rest.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Required settings to run ldap. */
@Configuration
@ConfigurationProperties(prefix = "kadai.ldap")
public class LdapSettings {

  private String userSearchBase;
  private String userSearchFilterName;
  private String userSearchFilterValue;
  private String userFirstnameAttribute;
  private String userLastnameAttribute;
  private String userFullnameAttribute;
  private String userPhoneAttribute;
  private String userMobilePhoneAttribute;
  private String userEmailAttribute;
  private String userIdAttribute;
  private String userOrglevel1Attribute;
  private String userOrglevel2Attribute;
  private String userOrglevel3Attribute;
  private String userOrglevel4Attribute;
  private String userMemberOfGroupAttribute;
  private String userPermissionsAttribute;
  private String permissionSearchBase;
  private String permissionSearchFilterName;
  private String permissionSearchFilterValue;
  private String permissionNameAttribute;
  private String groupSearchBase;
  private String baseDn;
  private String groupSearchFilterName;
  private String groupSearchFilterValue;
  private String groupNameAttribute;
  private String minSearchForLength;
  private String maxNumberOfReturnedAccessIds;
  private String groupsOfUser;
  //private String groupsOfUser.name;
  //private String groupsOfUser.type;
  private String permissionsOfUser;
  //private String permissionsOfUser.name;
  //private String permissionsOfUser.type;
  private String useDnForGroups;

  public String getUserSearchBase() {
    return userSearchBase;
  }

  public void setUserSearchBase(String userSearchBase) {
    this.userSearchBase = userSearchBase;
  }

  public String getUserSearchFilterName() {
    return userSearchFilterName;
  }

  public void setUserSearchFilterName(String userSearchFilterName) {
    this.userSearchFilterName = userSearchFilterName;
  }

  public String getUserSearchFilterValue() {
    return userSearchFilterValue;
  }

  public void setUserSearchFilterValue(String userSearchFilterValue) {
    this.userSearchFilterValue = userSearchFilterValue;
  }

  public String getUserFirstnameAttribute() {
    return userFirstnameAttribute;
  }

  public void setUserFirstnameAttribute(String userFirstnameAttribute) {
    this.userFirstnameAttribute = userFirstnameAttribute;
  }

  public String getUserLastnameAttribute() {
    return userLastnameAttribute;
  }

  public void setUserLastnameAttribute(String userLastnameAttribute) {
    this.userLastnameAttribute = userLastnameAttribute;
  }

  public String getUserFullnameAttribute() {
    return userFullnameAttribute;
  }

  public void setUserFullnameAttribute(String userFullnameAttribute) {
    this.userFullnameAttribute = userFullnameAttribute;
  }

  public String getUserPhoneAttribute() {
    return userPhoneAttribute;
  }

  public void setUserPhoneAttribute(String userPhoneAttribute) {
    this.userPhoneAttribute = userPhoneAttribute;
  }

  public String getUserMobilePhoneAttribute() {
    return userMobilePhoneAttribute;
  }

  public void setUserMobilePhoneAttribute(String userMobilePhoneAttribute) {
    this.userMobilePhoneAttribute = userMobilePhoneAttribute;
  }

  public String getUserEmailAttribute() {
    return userEmailAttribute;
  }

  public void setUserEmailAttribute(String userEmailAttribute) {
    this.userEmailAttribute = userEmailAttribute;
  }

  public String getUserIdAttribute() {
    return userIdAttribute;
  }

  public void setUserIdAttribute(String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }

  public String getUserOrglevel1Attribute() {
    return userOrglevel1Attribute;
  }

  public void setUserOrglevel1Attribute(String userOrglevel1Attribute) {
    this.userOrglevel1Attribute = userOrglevel1Attribute;
  }

  public String getUserOrglevel2Attribute() {
    return userOrglevel2Attribute;
  }

  public void setUserOrglevel2Attribute(String userOrglevel2Attribute) {
    this.userOrglevel2Attribute = userOrglevel2Attribute;
  }

  public String getUserOrglevel3Attribute() {
    return userOrglevel3Attribute;
  }

  public void setUserOrglevel3Attribute(String userOrglevel3Attribute) {
    this.userOrglevel3Attribute = userOrglevel3Attribute;
  }

  public String getUserOrglevel4Attribute() {
    return userOrglevel4Attribute;
  }

  public void setUserOrglevel4Attribute(String userOrglevel4Attribute) {
    this.userOrglevel4Attribute = userOrglevel4Attribute;
  }

  public String getUserMemberOfGroupAttribute() {
    return userMemberOfGroupAttribute;
  }

  public void setUserMemberOfGroupAttribute(String userMemberOfGroupAttribute) {
    this.userMemberOfGroupAttribute = userMemberOfGroupAttribute;
  }

  public String getUserPermissionsAttribute() {
    return userPermissionsAttribute;
  }

  public void setUserPermissionsAttribute(String userPermissionsAttribute) {
    this.userPermissionsAttribute = userPermissionsAttribute;
  }

  public String getPermissionSearchBase() {
    return permissionSearchBase;
  }

  public void setPermissionSearchBase(String permissionSearchBase) {
    this.permissionSearchBase = permissionSearchBase;
  }

  public String getPermissionSearchFilterName() {
    return permissionSearchFilterName;
  }

  public void setPermissionSearchFilterName(String permissionSearchFilterName) {
    this.permissionSearchFilterName = permissionSearchFilterName;
  }

  public String getPermissionSearchFilterValue() {
    return permissionSearchFilterValue;
  }

  public void setPermissionSearchFilterValue(String permissionSearchFilterValue) {
    this.permissionSearchFilterValue = permissionSearchFilterValue;
  }

  public String getPermissionNameAttribute() {
    return permissionNameAttribute;
  }

  public void setPermissionNameAttribute(String permissionNameAttribute) {
    this.permissionNameAttribute = permissionNameAttribute;
  }

  public String getGroupSearchBase() {
    return groupSearchBase;
  }

  public void setGroupSearchBase(String groupSearchBase) {
    this.groupSearchBase = groupSearchBase;
  }

  public String getBaseDn() {
    return baseDn;
  }

  public void setBaseDn(String baseDn) {
    this.baseDn = baseDn;
  }

  public String getGroupSearchFilterName() {
    return groupSearchFilterName;
  }

  public void setGroupSearchFilterName(String groupSearchFilterName) {
    this.groupSearchFilterName = groupSearchFilterName;
  }

  public String getGroupSearchFilterValue() {
    return groupSearchFilterValue;
  }

  public void setGroupSearchFilterValue(String groupSearchFilterValue) {
    this.groupSearchFilterValue = groupSearchFilterValue;
  }

  public String getGroupNameAttribute() {
    return groupNameAttribute;
  }

  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }

  public String getMinSearchForLength() {
    return minSearchForLength;
  }

  public void setMinSearchForLength(String minSearchForLength) {
    this.minSearchForLength = minSearchForLength;
  }

  public String getMaxNumberOfReturnedAccessIds() {
    return maxNumberOfReturnedAccessIds;
  }

  public void setMaxNumberOfReturnedAccessIds(String maxNumberOfReturnedAccessIds) {
    this.maxNumberOfReturnedAccessIds = maxNumberOfReturnedAccessIds;
  }

  public String getGroupsOfUser() {
    return groupsOfUser;
  }

  public void setGroupsOfUser(String groupsOfUser) {
    this.groupsOfUser = groupsOfUser;
  }

  public String getPermissionsOfUser() {
    return permissionsOfUser;
  }

  public void setPermissionsOfUser(String permissionsOfUser) {
    this.permissionsOfUser = permissionsOfUser;
  }

  public String getUseDnForGroups() {
    return useDnForGroups;
  }

  public void setUseDnForGroups(String useDnForGroups) {
    this.useDnForGroups = useDnForGroups;
  }
}
