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

package io.kadai.simplehistory.impl;

import static io.kadai.common.api.BaseQuery.toLowerCopy;

import io.kadai.common.api.TimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.simplehistory.impl.workbasket.WorkbasketHistoryQuery;
import io.kadai.simplehistory.impl.workbasket.WorkbasketHistoryQueryColumnName;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import io.kadai.workbasket.api.WorkbasketCustomField;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.RowBounds;

public class WorkbasketHistoryQueryImpl implements WorkbasketHistoryQuery {

  private static final String LINK_TO_MAPPER =
      "io.kadai.simplehistory.impl.workbasket.WorkbasketHistoryQueryMapper.queryHistoryEvents";
  private static final String LINK_TO_VALUE_MAPPER =
      "io.kadai.simplehistory.impl.workbasket."
          + "WorkbasketHistoryQueryMapper.queryHistoryColumnValues";
  private static final String LINK_TO_COUNTER =
      "io.kadai.simplehistory.impl.workbasket.WorkbasketHistoryQueryMapper.countHistoryEvents";

  private final InternalKadaiEngine internalKadaiEngine;
  private final List<String> orderColumns;

  @SuppressWarnings("unused")
  private WorkbasketHistoryQueryColumnName columnName;

  private List<String> orderBy;
  private String[] idIn;
  private String[] workbasketIdIn;
  private String[] eventTypeIn;
  private TimeInterval[] createdIn;
  private String[] userIdIn;
  private String[] domainIn;
  private String[] keyIn;
  private String[] typeIn;
  private String[] ownerIn;
  private String[] custom1In;
  private String[] custom2In;
  private String[] custom3In;
  private String[] custom4In;
  private String[] orgLevel1In;
  private String[] orgLevel2In;
  private String[] orgLevel3In;
  private String[] orgLevel4In;

  private String[] workbasketIdLike;
  private String[] eventTypeLike;
  private String[] userIdLike;
  private String[] domainLike;
  private String[] keyLike;
  private String[] typeLike;
  private String[] ownerLike;
  private String[] custom1Like;
  private String[] custom2Like;
  private String[] custom3Like;
  private String[] custom4Like;
  private String[] orgLevel1Like;
  private String[] orgLevel2Like;
  private String[] orgLevel3Like;
  private String[] orgLevel4Like;

  public WorkbasketHistoryQueryImpl(InternalKadaiEngine internalKadaiEngine) {
    this.internalKadaiEngine = internalKadaiEngine;
    this.orderBy = new ArrayList<>();
    this.orderColumns = new ArrayList<>();
  }

  public String[] getIdIn() {
    return idIn;
  }

  public String[] getWorkbasketIdIn() {
    return workbasketIdIn;
  }

  public String[] getEventTypeIn() {
    return eventTypeIn;
  }

  public TimeInterval[] getCreatedIn() {
    return createdIn;
  }

  public String[] getUserIdIn() {
    return userIdIn;
  }

  public String[] getDomainIn() {
    return domainIn;
  }

  public String[] getKeyIn() {
    return keyIn;
  }

  public String[] getTypeIn() {
    return typeIn;
  }

  public String[] getOwnerIn() {
    return ownerIn;
  }

  public String[] getCustom1In() {
    return custom1In;
  }

  public String[] getCustom2In() {
    return custom2In;
  }

  public String[] getCustom3In() {
    return custom3In;
  }

  public String[] getCustom4In() {
    return custom4In;
  }

  public String[] getOrgLevel1In() {
    return orgLevel1In;
  }

  public String[] getOrgLevel2In() {
    return orgLevel2In;
  }

  public String[] getOrgLevel3In() {
    return orgLevel3In;
  }

  public String[] getOrgLevel4In() {
    return orgLevel4In;
  }

  public String[] getWorkbasketIdLike() {
    return workbasketIdLike;
  }

  public String[] getEventTypeLike() {
    return eventTypeLike;
  }

  public String[] getUserIdLike() {
    return userIdLike;
  }

  public String[] getDomainLike() {
    return domainLike;
  }

  public String[] getKeyLike() {
    return keyLike;
  }

  public String[] getTypeLike() {
    return typeLike;
  }

  public String[] getOwnerLike() {
    return ownerLike;
  }

  public String[] getCustom1Like() {
    return custom1Like;
  }

  public String[] getCustom2Like() {
    return custom2Like;
  }

  public String[] getCustom3Like() {
    return custom3Like;
  }

  public String[] getCustom4Like() {
    return custom4Like;
  }

  public String[] getOrgLevel1Like() {
    return orgLevel1Like;
  }

  public String[] getOrgLevel2Like() {
    return orgLevel2Like;
  }

  public String[] getOrgLevel3Like() {
    return orgLevel3Like;
  }

  public String[] getOrgLevel4Like() {
    return orgLevel4Like;
  }

  @Override
  public WorkbasketHistoryQuery idIn(String... idIn) {
    this.idIn = idIn;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery workbasketIdIn(String... workbasketId) {
    this.workbasketIdIn = workbasketId;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery eventTypeIn(String... eventType) {
    this.eventTypeIn = eventType;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery createdWithin(TimeInterval... createdIn) {
    this.createdIn = createdIn;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery userIdIn(String... userId) {
    this.userIdIn = userId;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery domainIn(String... domain) {
    this.domainIn = domain;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery keyIn(String... workbasketKey) {
    this.keyIn = workbasketKey;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery typeIn(String... workbasketType) {
    this.typeIn = workbasketType;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery ownerIn(String... ownerIn) {
    this.ownerIn = ownerIn;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel1In(String... orgLevel1) {
    this.orgLevel1In = orgLevel1;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel2In(String... orgLevel2) {
    this.orgLevel2In = orgLevel2;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel3In(String... orgLevel3) {
    this.orgLevel3In = orgLevel3;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel4In(String... orgLevel4) {
    this.orgLevel4In = orgLevel4;
    return this;
  }

  @Override
  public WorkbasketHistoryQuery customAttributeIn(
      WorkbasketCustomField customField, String... searchArguments) {
    switch (customField) {
      case CUSTOM_1:
        custom1In = searchArguments;
        break;
      case CUSTOM_2:
        custom2In = searchArguments;
        break;
      case CUSTOM_3:
        custom3In = searchArguments;
        break;
      case CUSTOM_4:
        custom4In = searchArguments;
        break;
      default:
        throw new SystemException("Unknown customField '" + customField + "'");
    }
    return this;
  }

  @Override
  public WorkbasketHistoryQuery customAttributeLike(
      WorkbasketCustomField customField, String... searchArguments) {
    switch (customField) {
      case CUSTOM_1:
        custom1Like = toLowerCopy(searchArguments);
        break;
      case CUSTOM_2:
        custom2Like = toLowerCopy(searchArguments);
        break;
      case CUSTOM_3:
        custom3Like = toLowerCopy(searchArguments);
        break;
      case CUSTOM_4:
        custom4Like = toLowerCopy(searchArguments);
        break;
      default:
        throw new SystemException("Unknown customField '" + customField + "'");
    }
    return this;
  }

  @Override
  public WorkbasketHistoryQuery workbasketIdLike(String... workbasketId) {
    this.workbasketIdLike = toLowerCopy(workbasketId);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery eventTypeLike(String... eventType) {
    this.eventTypeLike = toLowerCopy(eventType);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery userIdLike(String... userId) {
    this.userIdLike = toLowerCopy(userId);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery domainLike(String... domain) {
    this.domainLike = toLowerCopy(domain);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery workbasketKeyLike(String... workbasketKey) {
    this.keyLike = toLowerCopy(workbasketKey);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery workbasketTypeLike(String... workbasketType) {
    this.typeLike = toLowerCopy(workbasketType);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery ownerLike(String... ownerLike) {
    this.ownerLike = toLowerCopy(ownerLike);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel1Like(String... orgLevel1) {
    this.orgLevel1Like = toLowerCopy(orgLevel1);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel2Like(String... orgLevel2) {
    this.orgLevel2Like = toLowerCopy(orgLevel2);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel3Like(String... orgLevel3) {
    this.orgLevel3Like = toLowerCopy(orgLevel3);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orgLevel4Like(String... orgLevel4) {
    this.orgLevel4Like = toLowerCopy(orgLevel4);
    return this;
  }

  @Override
  public WorkbasketHistoryQuery orderByWorkbasketId(SortDirection sortDirection) {
    return addOrderCriteria("WORKBASKET_ID", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByEventType(SortDirection sortDirection) {
    return addOrderCriteria("EVENT_TYPE", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByCreated(SortDirection sortDirection) {
    return addOrderCriteria("CREATED", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByUserId(SortDirection sortDirection) {
    return addOrderCriteria("USER_ID", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderById(SortDirection sortDirection) {
    return addOrderCriteria("ID", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByDomain(SortDirection sortDirection) {
    return addOrderCriteria("DOMAIN", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByKey(SortDirection sortDirection) {
    return addOrderCriteria("KEY", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByType(SortDirection sortDirection) {
    return addOrderCriteria("TYPE", sortDirection);
  }

  @Override
  public WorkbasketHistoryQuery orderByCustomAttribute(int num, SortDirection sortDirection)
      throws InvalidArgumentException {
    return switch (num) {
      case 1 -> addOrderCriteria("CUSTOM_1", sortDirection);
      case 2 -> addOrderCriteria("CUSTOM_2", sortDirection);
      case 3 -> addOrderCriteria("CUSTOM_3", sortDirection);
      case 4 -> addOrderCriteria("CUSTOM_4", sortDirection);
      default ->
          throw new InvalidArgumentException(
              "Custom number has to be between 1 and 4, but this is: " + num);
    };
  }

  @Override
  public WorkbasketHistoryQuery orderByOrgLevel(int num, SortDirection sortDirection)
      throws InvalidArgumentException {
    return switch (num) {
      case 1 -> addOrderCriteria("ORGLEVEL_1", sortDirection);
      case 2 -> addOrderCriteria("ORGLEVEL_2", sortDirection);
      case 3 -> addOrderCriteria("ORGLEVEL_3", sortDirection);
      case 4 -> addOrderCriteria("ORGLEVEL_4", sortDirection);
      default ->
          throw new InvalidArgumentException(
              "Org number has to be between 1 and 4, but this is: " + num);
    };
  }

  @Override
  public List<WorkbasketHistoryEvent> list() {
    try {
      internalKadaiEngine.openConnection();
      return internalKadaiEngine.getSqlSession().selectList(LINK_TO_MAPPER, this);
    } finally {
      internalKadaiEngine.returnConnection();
    }
  }

  @Override
  public List<WorkbasketHistoryEvent> list(int offset, int limit) {
    try {
      internalKadaiEngine.openConnection();
      RowBounds rowBounds = new RowBounds(offset, limit);
      return internalKadaiEngine.getSqlSession().selectList(LINK_TO_MAPPER, this, rowBounds);
    } finally {
      internalKadaiEngine.returnConnection();
    }
  }

  @Override
  public List<String> listValues(
      WorkbasketHistoryQueryColumnName dbColumnName, SortDirection sortDirection) {
    this.columnName = dbColumnName;
    List<String> cacheOrderBy = this.orderBy;
    this.orderBy.clear();
    this.addOrderCriteria(columnName.toString(), sortDirection);

    try {
      internalKadaiEngine.openConnection();
      return internalKadaiEngine.getSqlSession().selectList(LINK_TO_VALUE_MAPPER, this);
    } finally {
      this.orderBy = cacheOrderBy;
      this.columnName = null;
      this.orderColumns.remove(orderColumns.size() - 1);
      internalKadaiEngine.returnConnection();
    }
  }

  @Override
  public WorkbasketHistoryEvent single() {
    try {
      internalKadaiEngine.openConnection();
      return internalKadaiEngine.getSqlSession().selectOne(LINK_TO_MAPPER, this);
    } finally {
      internalKadaiEngine.returnConnection();
    }
  }

  @Override
  public long count() {
    try {
      internalKadaiEngine.openConnection();
      Long rowCount = internalKadaiEngine.getSqlSession().selectOne(LINK_TO_COUNTER, this);
      return (rowCount == null) ? 0L : rowCount;
    } finally {
      internalKadaiEngine.returnConnection();
    }
  }

  private WorkbasketHistoryQueryImpl addOrderCriteria(
      String columnName, SortDirection sortDirection) {
    String orderByDirection =
        " " + (sortDirection == null ? SortDirection.ASCENDING : sortDirection);
    orderBy.add(columnName + orderByDirection);
    orderColumns.add(columnName);
    return this;
  }
}
