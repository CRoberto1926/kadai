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

package io.kadai.classification.api;

import io.kadai.classification.api.exceptions.ClassificationAlreadyExistException;
import io.kadai.classification.api.exceptions.ClassificationInUseException;
import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.task.api.models.Task;

/**
 * The ClassificationService manages all operations on {@linkplain Classification Classifications}.
 */
public interface ClassificationService {

  // region Classification

  // CREATE

  /**
   * Instantiates a non-persistent/non-inserted {@linkplain Classification}.
   *
   * <p>The {@linkplain Classification} is initialized with unchangeable values for {@linkplain
   * Classification#getKey() key}, {@linkplain Classification#getDomain() domain} and {@linkplain
   * Classification#getType() type}. The {@linkplain Classification} will not be inserted into the
   * database until {@linkplain ClassificationService#createClassification(Classification)} call.
   *
   * @param key the {@linkplain Classification#getKey() key} of the {@linkplain Classification}
   * @param domain the {@linkplain Classification#getDomain() domain} of the new {@linkplain
   *     Classification}
   * @param type the {@linkplain Classification#getType() type} of the new {@linkplain
   *     Classification}
   * @return the instantiated {@linkplain Classification}
   */
  Classification newClassification(String key, String domain, String type);

  /**
   * Inserts a new {@linkplain Classification} after applying default values.
   *
   * <p>The {@linkplain Classification} will be added to master-domain, too - if not already
   * existing. <br>
   * The default values are:
   *
   * <ul>
   *   <li><b>{@linkplain Classification#getId() id}</b> - generated automatically
   *   <li><b>{@linkplain Classification#getParentId() parentId}</b> - ""
   *   <li><b>{@linkplain Classification#getParentKey() parentKey}</b> - ""
   *   <li><b>{@linkplain Classification#getServiceLevel() serviceLevel}</b> - "P0D"
   *   <li><b>{@linkplain Classification#getIsValidInDomain() isValidInDomain}</b> - true <br>
   *       if {@linkplain Classification#getDomain() domain} is the master domain: false
   * </ul>
   *
   * @param classification the {@linkplain Classification} to insert
   * @return the inserted {@linkplain Classification} with unique {@linkplain Classification#getId()
   *     id}.
   * @throws ClassificationAlreadyExistException if the {@linkplain Classification} already exists
   *     in the given {@linkplain Classification#getDomain() domain}.
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   * @throws DomainNotFoundException if the {@linkplain Classification#getDomain() domain} does not
   *     exist in the configuration
   * @throws MalformedServiceLevelException if the {@linkplain Classification#getServiceLevel()
   *     serviceLevel} does not comply with the ISO 8601 specification
   * @throws InvalidArgumentException if the {@linkplain Classification} contains invalid properties
   */
  Classification createClassification(Classification classification)
      throws ClassificationAlreadyExistException,
          DomainNotFoundException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException;

  // READ

  /**
   * Gets the {@linkplain Classification} identified by the provided {@linkplain
   * Classification#getId() id}.
   *
   * @param id the {@linkplain Classification#getId() id} of the searched-for {@linkplain
   *     Classification}
   * @return the {@linkplain Classification} identified by {@linkplain Classification#getId() id}
   * @throws ClassificationNotFoundException if no {@linkplain Classification} with the specified
   *     {@linkplain Classification#getId() id} was found
   */
  Classification getClassification(String id) throws ClassificationNotFoundException;

  /**
   * Gets the {@linkplain Classification} identified by the provided {@linkplain
   * Classification#getKey() key} and {@linkplain Classification#getDomain() domain}. If there's no
   * {@linkplain Classification} in the given {@linkplain Classification#getDomain() domain},
   * returns the {@linkplain Classification} from the master domain.
   *
   * @param key the {@linkplain Classification#getKey() key} of the searched-for {@linkplain
   *     Classification}
   * @param domain the {@linkplain Classification#getDomain() domain} of the searched-for
   *     {@linkplain Classification}
   * @return if exists: domain-specific {@linkplain Classification}, else master {@linkplain
   *     Classification}
   * @throws ClassificationNotFoundException if no {@linkplain Classification} with specified
   *     {@linkplain Classification#getKey() key} was found neither in the specified {@linkplain
   *     Classification#getDomain() domain} nor in the master {@linkplain Classification#getDomain()
   *     domain}
   */
  Classification getClassification(String key, String domain)
      throws ClassificationNotFoundException;

  // UPDATE

  /**
   * Updates the specified {@linkplain Classification}.
   *
   * @param classification the {@linkplain Classification} to update
   * @return the updated {@linkplain Classification}.
   * @throws ClassificationNotFoundException if the specified {@linkplain Classification} or its
   *     parent does not exist
   * @throws NotAuthorizedException if the caller is neither member of{@linkplain
   *     KadaiRole#BUSINESS_ADMIN} nor {@linkplain KadaiRole#ADMIN}
   * @throws ConcurrencyException if the {@linkplain Classification} was modified in the meantime
   *     and is not the most up to date anymore; that's the case if the given {@linkplain
   *     Classification#getModified() modified} timestamp differs from the one in the database
   * @throws MalformedServiceLevelException if the {@linkplain Classification#getServiceLevel()
   *     serviceLevel} does not comply with the ISO 8601 specification
   * @throws InvalidArgumentException if the {@linkplain Classification} contains invalid properties
   */
  Classification updateClassification(Classification classification)
      throws ClassificationNotFoundException,
          ConcurrencyException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException;

  // DELETE

  /**
   * Deletes a {@linkplain Classification} with all child {@linkplain Classification
   * Classifications}.
   *
   * @param id the {@linkplain Classification#getId() id} of the searched-for {@linkplain
   *     Classification}
   * @throws ClassificationInUseException if there are {@linkplain Task Tasks} which refer to this
   *     {@linkplain Classification}
   * @throws ClassificationNotFoundException if no {@linkplain Classification} with the specified
   *     {@linkplain Classification#getId() id} was found
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   */
  void deleteClassification(String id)
      throws ClassificationInUseException, ClassificationNotFoundException, NotAuthorizedException;

  /**
   * Deletes the {@linkplain Classification} identified by the provided {@linkplain
   * Classification#getKey() key} and {@linkplain Classification#getDomain() domain} with all its
   * child {@linkplain Classification Classifications}.
   *
   * @param classificationKey the {@linkplain Classification#getKey() key} of the {@linkplain
   *     Classification} you want to delete.
   * @param domain the {@linkplain Classification#getDomain() domain} of the the {@linkplain
   *     Classification} you want to delete. if "", the function tries to delete the {@linkplain
   *     Classification} from the master {@linkplain Classification#getDomain() domain} and any
   *     other {@linkplain Classification} with this {@linkplain Classification#getKey() key}
   * @throws ClassificationInUseException if there are {@linkplain Task Tasks} which refer to this
   *     {@linkplain Classification}
   * @throws ClassificationNotFoundException if no {@linkplain Classification} with the specified
   *     {@linkplain Classification#getKey() key} and {@linkplain Classification#getDomain() domain}
   *     was found
   * @throws NotAuthorizedException if the current user is not member of {@linkplain
   *     KadaiRole#BUSINESS_ADMIN} or {@linkplain KadaiRole#ADMIN}
   */
  void deleteClassification(String classificationKey, String domain)
      throws ClassificationInUseException, ClassificationNotFoundException, NotAuthorizedException;

  // endregion

  /**
   * Creates an empty {@linkplain ClassificationQuery}.
   *
   * @return a {@linkplain ClassificationQuery}
   */
  ClassificationQuery createClassificationQuery();
}
