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

/**
 * Modules
 */
import { NgxsModule } from '@ngxs/store';
import { NgxsReduxDevtoolsPluginModule } from '@ngxs/devtools-plugin';
import { AlertModule } from 'ngx-bootstrap/alert';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { TabsModule } from 'ngx-bootstrap/tabs';

/**
 * Services
 */
import { StartupService } from 'app/shared/services/startup/startup.service';
/**
 * Components
 */
/**
 * Store
 */
import { environment } from '../environments/environment';
import { STATES } from './shared/store';
import { NgxsRouterPluginModule } from '@ngxs/router-plugin';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';

import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

TabsModule.forRoot();
AlertModule.forRoot();
AngularSvgIconModule.forRoot();
NgxsModule.forRoot(STATES, { developmentMode: !environment.production });
NgxsReduxDevtoolsPluginModule.forRoot({ disabled: environment.production, maxAge: 25 });
NgxsRouterPluginModule.forRoot();
provideHttpClient(withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }));
registerLocaleData(localeDe);

export function startupServiceFactory(startupService: StartupService): () => Promise<any> {
  return (): Promise<any> => startupService.load();
}
