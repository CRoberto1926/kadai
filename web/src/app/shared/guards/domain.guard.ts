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

import { of } from 'rxjs';
import { CanActivate } from '@angular/router';
import { Injectable } from '@angular/core';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { catchError, map } from 'rxjs/operators';

@Injectable()
export class DomainGuard implements CanActivate {
  constructor(private domainService: DomainService) {}

  canActivate() {
    return this.domainService.getDomains().pipe(
      map(() => true),
      catchError(() => {
        return of(false);
      })
    );
  }
}
