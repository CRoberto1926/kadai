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

import { APP_INITIALIZER, enableProdMode, importProvidersFrom } from '@angular/core';
import { environment } from 'environments/environment';
import { WindowRefService } from 'app/shared/services/window/window.service';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { OrientationService } from 'app/shared/services/orientation/orientation.service';
import { SelectedRouteService } from 'app/shared/services/selected-route/selected-route';
import { StartupService } from 'app/shared/services/startup/startup.service';
import { startupServiceFactory } from './app/app.module';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { FormsValidatorService } from './app/shared/services/forms-validator/forms-validator.service';
import { NotificationService } from './app/shared/services/notifications/notification.service';
import { ClassificationCategoriesService } from './app/shared/services/classification-categories/classification-categories.service';
import { SidenavService } from './app/shared/services/sidenav/sidenav.service';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { AlertModule } from 'ngx-bootstrap/alert';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from './app/app-routing.module';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TreeModule } from '@ali-hm/angular-tree-component';
import { SharedModule } from 'app/shared/shared.module';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NgxsModule } from '@ngxs/store';
import { STATES } from './app/shared/store';
import { NgxsReduxDevtoolsPluginModule } from '@ngxs/devtools-plugin';
import { NgxsRouterPluginModule } from '@ngxs/router-plugin';
import { NavBarComponent } from 'app/shared/components/nav-bar/nav-bar.component';
import { UserInformationComponent } from 'app/shared/components/user-information/user-information.component';
import { NoAccessComponent } from 'app/shared/components/no-access/no-access.component';
import { SidenavListComponent } from 'app/shared/components/sidenav-list/sidenav-list.component';
import { AppComponent } from './app/app.component';

const PROVIDERS = [
  WindowRefService,
  DomainService,
  RequestInProgressService,
  OrientationService,
  SelectedRouteService,
  StartupService,
  MasterAndDetailService,
  KadaiEngineService,
  FormsValidatorService,
  NotificationService,
  ClassificationCategoriesService,
  SidenavService,
  {
    provide: APP_INITIALIZER,
    useFactory: startupServiceFactory,
    deps: [StartupService],
    multi: true
  },
  provideHttpClient(withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }))
];
const MODULES = [
  TabsModule.forRoot(),
  AlertModule.forRoot(),
  BrowserModule,
  FormsModule,
  AppRoutingModule,
  AngularSvgIconModule.forRoot(),
  BrowserAnimationsModule,
  ReactiveFormsModule,
  TreeModule,
  SharedModule,
  MatSidenavModule,
  MatCheckboxModule,
  MatGridListModule,
  MatListModule,
  MatButtonModule,
  MatIconModule,
  MatSelectModule,
  MatToolbarModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  NgxsModule.forRoot(STATES, { developmentMode: !environment.production }),
  NgxsReduxDevtoolsPluginModule.forRoot({ disabled: environment.production, maxAge: 25 }),
  NgxsRouterPluginModule.forRoot()
];
const DECLARATIONS = [NavBarComponent, UserInformationComponent, NoAccessComponent, SidenavListComponent];

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [importProvidersFrom(...MODULES, ...DECLARATIONS), ...PROVIDERS]
});
