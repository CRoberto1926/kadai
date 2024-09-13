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

package io.kadai.workbasket.api;

import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.user.api.models.User;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketInUseException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;

/** This service manages Workbaskets. */
public interface WorkbasketService {

  // region Workbasket

  // CREATE

  /**
   * Returns a new {@linkplain Workbasket} which is not inserted.
   *
   * @param key the {@linkplain Workbasket#getKey() key} used to identify the {@linkplain
   *     Workbasket}
   * @param domain the {@linkplain Workbasket#getDomain() domain} of the new {@linkplain Workbasket}
   * @return new {@linkplain Workbasket}
   */
  Workbasket newWorkbasket(String key, String domain);

  /**
   * Creates a new {@linkplain Workbasket}. <br>
   * The default values are:
   *
   * <ul>
   *   <li><b>id</b> - generated by {@linkplain io.kadai.common.internal.util.IdGenerator
   *       IdGenerator}
   * </ul>
   *
   * @param workbasket The {@linkplain Workbasket} to create
   * @return the created and inserted {@linkplain Workbasket}
   * @throws InvalidArgumentException If a required property of the {@linkplain Workbasket} is not
   *     set.
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   * @throws WorkbasketAlreadyExistException if the {@linkplain Workbasket} exists already
   * @throws DomainNotFoundException if the {@linkplain Workbasket#getDomain() domain} does not
   *     exist in the configuration.
   */
  Workbasket createWorkbasket(Workbasket workbasket)
      throws InvalidArgumentException,
          WorkbasketAlreadyExistException,
          DomainNotFoundException,
          NotAuthorizedException;

  // READ

  /**
   * Get the {@linkplain Workbasket} specified by the given {@linkplain WorkbasketSummary#getId()
   * id}.
   *
   * @param workbasketId the {@linkplain WorkbasketSummary#getId() id} of the {@linkplain
   *     Workbasket} requested
   * @return the requested Workbasket
   * @throws WorkbasketNotFoundException If the {@linkplain Workbasket} with workbasketId is not
   *     found
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ} for the requested {@linkplain Workbasket}
   */
  Workbasket getWorkbasket(String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * Get the {@linkplain Workbasket} specified by the given {@linkplain WorkbasketSummary#getKey()
   * key} and {@linkplain WorkbasketSummary#getDomain() domain}.
   *
   * @param workbasketKey the {@linkplain WorkbasketSummary#getKey() key} of the {@linkplain
   *     Workbasket} requested
   * @param domain the {@linkplain WorkbasketSummary#getDomain() domain} of the {@linkplain
   *     Workbasket}
   * @return the requested {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException If the {@linkplain Workbasket} with workbasketId is not
   *     found
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ} for the requested {@linkplain Workbasket}
   */
  Workbasket getWorkbasket(String workbasketKey, String domain)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  // UPDATE

  /**
   * Update the given {@linkplain Workbasket}.
   *
   * @param workbasket The {@linkplain Workbasket} to update
   * @return the updated {@linkplain Workbasket}
   * @throws InvalidArgumentException if {@linkplain Workbasket#getName() name} or {@linkplain
   *     Workbasket#getType() type} of the {@linkplain Workbasket} is invalid
   * @throws NotAuthorizedOnWorkbasketException This is never thrown
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} cannot be found.
   * @throws ConcurrencyException if an attempt is made to update the {@linkplain Workbasket} and
   *     another user updated it already; that's the case if the given modified timestamp differs
   *     from the one in the database
   */
  Workbasket updateWorkbasket(Workbasket workbasket)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          ConcurrencyException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  // DELETE

  /**
   * Deletes the {@linkplain Workbasket} by the given {@linkplain Workbasket#getId() id}.
   *
   * @param workbasketId {@linkplain Workbasket#getId() id} of the {@linkplain Workbasket} which
   *     should be deleted.
   * @return true if the {@linkplain Workbasket} was deleted successfully; false if the {@linkplain
   *     Workbasket} is marked for deletion
   * @throws NotAuthorizedOnWorkbasketException This is never thrown
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} does not exist
   * @throws WorkbasketInUseException if the {@linkplain Workbasket} does contain task-content
   * @throws InvalidArgumentException if the workbasketId is NULL or EMPTY
   */
  boolean deleteWorkbasket(String workbasketId)
      throws WorkbasketNotFoundException,
          WorkbasketInUseException,
          InvalidArgumentException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * Deletes the list of {@linkplain Workbasket}s specified via {@linkplain Workbasket#getId() ids}.
   *
   * @param workbasketsIds the {@linkplain Workbasket#getId() ids} of the {@linkplain Workbasket}s
   *     to delete.
   * @return the result of the operations and an Exception for each failed workbasket deletion
   * @throws InvalidArgumentException if the WorkbasketIds parameter List is NULL or empty
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   */
  BulkOperationResults<String, KadaiException> deleteWorkbaskets(List<String> workbasketsIds)
      throws InvalidArgumentException, NotAuthorizedException;

  // endregion

  // region DistributionTarget

  // CREATE

  /**
   * Set the distribution targets for a {@linkplain Workbasket}.
   *
   * @param sourceWorkbasketId the {@linkplain Workbasket#getId() id} of the source {@linkplain
   *     Workbasket} for which the distribution targets are to be set
   * @param targetWorkbasketIds a list of the ids of the target {@linkplain Workbasket}s
   * @throws NotAuthorizedOnWorkbasketException This is never thrown
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws WorkbasketNotFoundException if either the source {@linkplain Workbasket} or any of the
   *     target {@linkplain Workbasket}s don't exist
   */
  void setDistributionTargets(String sourceWorkbasketId, List<String> targetWorkbasketIds)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  // READ

  /**
   * Returns the distribution targets for a given {@linkplain Workbasket}.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} of the referenced {@linkplain
   *     Workbasket}
   * @return the distribution targets of the specified {@linkplain Workbasket}
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ READ permission} for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} doesn't exist
   */
  List<WorkbasketSummary> getDistributionTargets(String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * Returns the distribution targets for a given {@linkplain Workbasket}.
   *
   * @param workbasketKey the {@linkplain Workbasket#getKey() key} of the referenced {@linkplain
   *     Workbasket}
   * @param domain the {@linkplain Workbasket#getDomain() domain} of the referenced {@linkplain
   *     Workbasket}
   * @return the distribution targets of the specified {@linkplain Workbasket}
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ READ permission} for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} doesn't exist
   */
  List<WorkbasketSummary> getDistributionTargets(String workbasketKey, String domain)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * Returns the distribution sources for a given {@linkplain Workbasket}.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} of the referenced {@linkplain
   *     Workbasket}
   * @return the workbaskets that are distribution sources of the specified {@linkplain Workbasket}.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ READ permission} for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} doesn't exist
   */
  List<WorkbasketSummary> getDistributionSources(String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * Returns the distribution sources for a given {@linkplain Workbasket}.
   *
   * @param workbasketKey the {@linkplain Workbasket#getKey() key} of the referenced {@linkplain
   *     Workbasket}
   * @param domain the {@linkplain Workbasket#getDomain() domain} of the referenced {@linkplain
   *     Workbasket}
   * @return the workbaskets that are distribution sources of the specified {@linkplain Workbasket}.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no {@linkplain
   *     WorkbasketPermission#READ READ permission} for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} doesn't exist
   */
  List<WorkbasketSummary> getDistributionSources(String workbasketKey, String domain)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  // UPDATE

  /**
   * Add a distribution target to a {@linkplain Workbasket}. If the specified distribution target
   * exists already, the method silently returns without doing anything.
   *
   * @param sourceWorkbasketId the {@linkplain Workbasket#getId() id} of the source {@linkplain
   *     Workbasket}
   * @param targetWorkbasketId the {@linkplain Workbasket#getId() id} of the target {@linkplain
   *     Workbasket}
   * @throws NotAuthorizedOnWorkbasketException if the current user doesn't have {@linkplain
   *     WorkbasketPermission#READ READ permission} for the source {@linkplain Workbasket}s
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws WorkbasketNotFoundException if either the source {@linkplain Workbasket} or the target
   *     {@linkplain Workbasket} doesn't exist
   */
  void addDistributionTarget(String sourceWorkbasketId, String targetWorkbasketId)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  // DELETE

  /**
   * Remove a distribution target from a {@linkplain Workbasket}. If the specified distribution
   * target doesn't exist, the method silently returns without doing anything.
   *
   * @param sourceWorkbasketId The {@linkplain Workbasket#getId() id} of the source {@linkplain
   *     Workbasket}
   * @param targetWorkbasketId The {@linkplain Workbasket#getId() id} of the target {@linkplain
   *     Workbasket}
   * @throws NotAuthorizedOnWorkbasketException If the current user doesn't have {@linkplain
   *     WorkbasketPermission#READ READ permission} for the source {@linkplain Workbasket}
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   */
  void removeDistributionTarget(String sourceWorkbasketId, String targetWorkbasketId)
      throws NotAuthorizedException, NotAuthorizedOnWorkbasketException;

  // endregion

  // region WorkbasketAccessItem

  // CREATE

  /**
   * Returns a new {@linkplain WorkbasketAccessItem} which is not inserted.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} used to identify the referenced
   *     {@linkplain Workbasket}
   * @param accessId the group id or user id for which access is controlled
   * @return new {@linkplain WorkbasketAccessItem}
   */
  WorkbasketAccessItem newWorkbasketAccessItem(String workbasketId, String accessId);

  /**
   * Create and insert a new {@linkplain WorkbasketAccessItem} with a {@linkplain
   * WorkbasketAccessItem#getWorkbasketId() workbasketId}, an {@linkplain
   * WorkbasketAccessItem#getAccessId() accessId} and {@linkplain
   * WorkbasketAccessItem#getPermission(WorkbasketPermission) permissions}.
   *
   * @param workbasketAccessItem the new {@linkplain WorkbasketAccessItem}
   * @return the created {@linkplain WorkbasketAccessItem}
   * @throws InvalidArgumentException if the preconditions don't match the required ones.
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   * @throws WorkbasketNotFoundException if the {@linkplain WorkbasketAccessItem} refers to a not
   *     existing workbasket
   * @throws WorkbasketAccessItemAlreadyExistException if there exists already a {@linkplain
   *     WorkbasketAccessItem} for the same {@linkplain WorkbasketAccessItem#getAccessId() accessId}
   *     and {@linkplain Workbasket}
   */
  WorkbasketAccessItem createWorkbasketAccessItem(WorkbasketAccessItem workbasketAccessItem)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          NotAuthorizedException;

  /**
   * Setting up the new {@linkplain WorkbasketAccessItem}s for a {@linkplain Workbasket}. Already
   * stored values will be completely replaced by the current ones.
   *
   * <p>Preconditions for each {@linkplain WorkbasketAccessItem} then {@code wbAccessItems}:
   *
   * <ul>
   *   <li>{@linkplain WorkbasketAccessItem#getWorkbasketId()} is not null
   *   <li>{@linkplain WorkbasketAccessItem#getWorkbasketId()} is equal to {@code workbasketId}
   *   <li>{@linkplain WorkbasketAccessItem#getAccessId()} is unique
   * </ul>
   *
   * @param workbasketId {@linkplain Workbasket#getId() id} of the access-target {@linkplain
   *     Workbasket}.
   * @param wbAccessItems List of {@linkplain WorkbasketAccessItem}s which does replace all current
   *     stored ones.
   * @throws InvalidArgumentException will be thrown when the parameter {@code wbAccessItems} is
   *     NULL or member doesn't match the preconditions
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws NotAuthorizedOnWorkbasketException This is never thrown
   * @throws WorkbasketAccessItemAlreadyExistException if {@code wbAccessItems} contains multiple
   *     {@linkplain WorkbasketAccessItem} with the same {@linkplain
   *     WorkbasketAccessItem#getAccessId() accessId}.
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} cannot be found for the
   *     given {@linkplain Workbasket#getId() id}.
   */
  void setWorkbasketAccessItems(String workbasketId, List<WorkbasketAccessItem> wbAccessItems)
      throws InvalidArgumentException,
          WorkbasketAccessItemAlreadyExistException,
          WorkbasketNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  // READ

  /**
   * Get all {@linkplain WorkbasketAccessItem}s for a {@linkplain Workbasket}.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} of the {@linkplain Workbasket}
   * @return List of {@linkplain WorkbasketAccessItem}s for the {@linkplain Workbasket}
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN} or {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN}
   */
  List<WorkbasketAccessItem> getWorkbasketAccessItems(String workbasketId)
      throws NotAuthorizedException;

  // UPDATE

  /**
   * This method updates a {@linkplain WorkbasketAccessItem}.
   *
   * @param workbasketAccessItem the {@linkplain WorkbasketAccessItem}
   * @return the updated entity
   * @throws InvalidArgumentException if {@linkplain WorkbasketAccessItem#getAccessId() accessId} or
   *     {@linkplain WorkbasketAccessItem#getWorkbasketId() workbasketId} is changed in the
   *     {@linkplain WorkbasketAccessItem}
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   */
  WorkbasketAccessItem updateWorkbasketAccessItem(WorkbasketAccessItem workbasketAccessItem)
      throws InvalidArgumentException, NotAuthorizedException;

  // DELETE

  /**
   * Deletes a specific {@linkplain WorkbasketAccessItem}.
   *
   * @param id the {@linkplain WorkbasketAccessItem#getId() id} of the {@linkplain
   *     WorkbasketAccessItem} to be deleted
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   */
  void deleteWorkbasketAccessItem(String id) throws NotAuthorizedException;

  /**
   * Deletes all {@linkplain WorkbasketAccessItem}s using the given {@linkplain
   * WorkbasketAccessItem#getAccessId()}.
   *
   * @param accessId {@linkplain User#getId() id} of a kadai-{@linkplain User}.
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   */
  void deleteWorkbasketAccessItemsForAccessId(String accessId) throws NotAuthorizedException;

  // endregion

  // region Query
  /**
   * This method provides a query builder for querying the database.
   *
   * @return a {@linkplain WorkbasketQuery}
   */
  WorkbasketQuery createWorkbasketQuery();

  /**
   * This method provides a query builder for querying the database.
   *
   * @return a {@linkplain WorkbasketAccessItemQuery}
   * @throws NotAuthorizedException if the current user is not member of role {@linkplain
   *     io.kadai.common.api.KadaiRole#ADMIN admin} or {@linkplain
   *     io.kadai.common.api.KadaiRole#BUSINESS_ADMIN business-admin}
   */
  WorkbasketAccessItemQuery createWorkbasketAccessItemQuery() throws NotAuthorizedException;

  // endregion

  // region Permission and Authorization

  /**
   * Returns a set with all {@linkplain WorkbasketPermission permissions} of the current user at
   * this {@linkplain Workbasket}.<br>
   * If the workbasketId is invalid, an empty List of {@linkplain WorkbasketPermission}s is returned
   * since there is no distinction made between the situation that the {@linkplain Workbasket} is
   * not found and the caller has no {@linkplain WorkbasketPermission permissions} on the
   * {@linkplain Workbasket}.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} of the referenced {@linkplain
   *     Workbasket}
   * @return a {@link List} with all {@link WorkbasketPermission}s of the caller on the requested
   *     {@linkplain Workbasket}.
   */
  List<WorkbasketPermission> getPermissionsForWorkbasket(String workbasketId);

  /**
   * This method checks the authorization for the actual User.
   *
   * @param workbasketId the {@linkplain Workbasket#getId() id} of the {@linkplain Workbasket} we
   *     want to access
   * @param permission the needed {@linkplain WorkbasketPermission}; if more than one {@linkplain
   *     WorkbasketPermission permission} is specified, the current user needs all of them
   * @throws NotAuthorizedOnWorkbasketException if the current user has not the requested
   *     authorization for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if the {@linkplain Workbasket} cannot be found for the
   *     given {@linkplain Workbasket#getId() id}.
   */
  void checkAuthorization(String workbasketId, WorkbasketPermission... permission)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * This method checks the authorization for the actual User.
   *
   * @param workbasketKey the {@linkplain Workbasket#getKey() key} of the {@linkplain Workbasket} we
   *     want to access
   * @param domain the {@linkplain Workbasket#getDomain() domain} of the {@linkplain Workbasket} we
   *     want to access
   * @param permission the needed {@linkplain WorkbasketPermission}; if more than one {@linkplain
   *     WorkbasketPermission permission} is specified, the current user needs all of them.
   * @throws NotAuthorizedOnWorkbasketException if the current user has not the requested
   *     {@linkplain WorkbasketPermission permission} for the specified {@linkplain Workbasket}
   * @throws WorkbasketNotFoundException if no {@linkplain Workbasket} can be found for the given
   *     {@linkplain Workbasket#getKey() key} and {@linkplain Workbasket#getDomain() domain} values.
   */
  void checkAuthorization(String workbasketKey, String domain, WorkbasketPermission... permission)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  // endregion

}
