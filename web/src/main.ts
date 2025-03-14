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

import { APP_INITIALIZER, enableProdMode, importProvidersFrom, inject, LOCALE_ID } from '@angular/core';
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
import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpXsrfTokenExtractor,
  provideHttpClient,
  withInterceptors,
  withXsrfConfiguration
} from '@angular/common/http';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { AlertModule } from 'ngx-bootstrap/alert';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { appRoutes } from './app/app.routes';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TreeModule } from '@ali-hm/angular-tree-component';
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
import { CommonModule, registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import { SettingsService } from './app/settings/services/settings-service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { ReportTableComponent } from './app/monitor/components/report-table/report-table.component';
import { MonitorComponent } from './app/monitor/components/monitor/monitor.component';
import { TaskPriorityReportComponent } from './app/monitor/components/task-priority-report/task-priority-report.component';
import { TaskPriorityReportFilterComponent } from './app/monitor/components/task-priority-report-filter/task-priority-report-filter.component';
import { CanvasComponent } from './app/monitor/components/canvas/canvas.component';
import { TimestampReportComponent } from './app/monitor/components/timestamp-report/timestamp-report.component';
import { WorkbasketReportComponent } from './app/monitor/components/workbasket-report/workbasket-report.component';
import { WorkbasketReportPlannedDateComponent } from './app/monitor/components/workbasket-report-planned-date/workbasket-report-planned-date.component';
import { WorkbasketReportDueDateComponent } from './app/monitor/components/workbasket-report-due-date/workbasket-report-due-date.component';
import { TaskReportComponent } from './app/monitor/components/task-report/task-report.component';
import { ClassificationReportComponent } from './app/monitor/components/classification-report/classification-report.component';
import { MonitorService } from './app/monitor/services/monitor.service';
import { MapToIterable } from './app/shared/pipes/map-to-iterable.pipe';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { TaskListToolbarComponent } from './app/workplace/components/task-list-toolbar/task-list-toolbar.component';
import { TaskMasterComponent } from './app/workplace/components/task-master/task-master.component';
import { TaskDetailsComponent } from './app/workplace/components/task-details/task-details.component';
import { TaskInformationComponent } from './app/workplace/components/task-information/task-information.component';
import { TaskAttributeValueComponent } from './app/workplace/components/task-attribute-value/task-attribute-value.component';
import { TaskCustomFieldsComponent } from './app/workplace/components/task-custom-fields/task-custom-fields.component';
import { TaskProcessingComponent } from './app/workplace/components/task-processing/task-processing.component';
import { TaskStatusDetailsComponent } from './app/workplace/components/task-status-details/task-status-details.component';
import { TaskListComponent } from './app/workplace/components/task-list/task-list.component';
import { TaskService } from './app/workplace/services/task.service';
import { WorkplaceService } from './app/workplace/services/workplace.service';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { provideRouter, RouterModule, withHashLocation } from '@angular/router';
import { SpinnerComponent } from './app/shared/components/spinner/spinner.component';
import { MasterAndDetailComponent } from './app/shared/components/master-and-detail/master-and-detail.component';
import { KadaiTreeComponent } from './app/administration/components/tree/tree.component';
import { TypeAheadComponent } from './app/shared/components/type-ahead/type-ahead.component';
import { MapValuesPipe } from './app/shared/pipes/map-values.pipe';
import { RemoveNoneTypePipe } from './app/shared/pipes/remove-empty-type.pipe';
import { SpreadNumberPipe } from './app/shared/pipes/spread-number.pipe';
import { DateTimeZonePipe } from './app/shared/pipes/date-time-zone.pipe';
import { NumberToArray } from './app/shared/pipes/number-to-array.pipe';
import { OrderBy } from './app/shared/pipes/order-by.pipe';
import { SortComponent } from './app/shared/components/sort/sort.component';
import { IconTypeComponent } from './app/administration/components/type-icon/icon-type.component';
import { FieldErrorDisplayComponent } from './app/shared/components/field-error-display/field-error-display.component';
import { PaginationComponent } from './app/shared/components/pagination/pagination.component';
import { ProgressSpinnerComponent } from './app/shared/components/progress-spinner/progress-spinner.component';
import { DialogPopUpComponent } from './app/shared/components/popup/dialog-pop-up.component';
import { WorkbasketFilterComponent } from './app/shared/components/workbasket-filter/workbasket-filter.component';
import { TaskFilterComponent } from './app/shared/components/task-filter/task-filter.component';
import { DragAndDropDirective } from './app/shared/directives/drag-and-drop.directive';
import { ResizableWidthDirective } from './app/shared/directives/resizable-width.directive';
import { tap } from 'rxjs/operators';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule } from '@angular/material/paginator';
import { AccessIdsService } from './app/shared/services/access-ids/access-ids.service';
import { ClassificationsService } from './app/shared/services/classifications/classifications.service';
import { WorkbasketService } from './app/shared/services/workbasket/workbasket.service';
import { ObtainMessageService } from './app/shared/services/obtain-message/obtain-message.service';
import { provideHotToastConfig } from '@ngneat/hot-toast';
import { MatSortModule } from '@angular/material/sort';
import { TaskHistoryQueryComponent } from './app/history/task-history-query/task-history-query.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { WorkbasketOverviewComponent } from './app/administration/components/workbasket-overview/workbasket-overview.component';
import { WorkbasketListComponent } from './app/administration/components/workbasket-list/workbasket-list.component';
import { WorkbasketListToolbarComponent } from './app/administration/components/workbasket-list-toolbar/workbasket-list-toolbar.component';
import { WorkbasketAccessItemsComponent } from './app/administration/components/workbasket-access-items/workbasket-access-items.component';
import { WorkbasketDetailsComponent } from './app/administration/components/workbasket-details/workbasket-details.component';
import { WorkbasketInformationComponent } from './app/administration/components/workbasket-information/workbasket-information.component';
import { WorkbasketDistributionTargetsComponent } from './app/administration/components/workbasket-distribution-targets/workbasket-distribution-targets.component';
import { WorkbasketDistributionTargetsListComponent } from './app/administration/components/workbasket-distribution-targets-list/workbasket-distribution-targets-list.component';
import { ClassificationOverviewComponent } from './app/administration/components/classification-overview/classification-overview.component';
import { ClassificationListComponent } from './app/administration/components/classification-list/classification-list.component';
import { ClassificationTypesSelectorComponent } from './app/administration/components/classification-types-selector/classification-types-selector.component';
import { ClassificationDetailsComponent } from './app/administration/components/classification-details/classification-details.component';
import { ImportExportComponent } from './app/administration/components/import-export/import-export.component';
import { AccessItemsManagementComponent } from './app/administration/components/access-items-management/access-items-management.component';
import { AdministrationOverviewComponent } from './app/administration/components/administration-overview/administration-overview.component';
import { MatRippleModule } from '@angular/material/core';
import { ClassificationDefinitionService } from './app/administration/services/classification-definition.service';
import { WorkbasketDefinitionService } from './app/administration/services/workbasket-definition.service';
import { ImportExportService } from './app/administration/services/import-export.service';
import { RoutingUploadComponent } from '@task-routing/components/routing-upload/routing-upload.component';
import { TaskState } from '@task/store/task.state';
import { TaskOverviewComponent } from '@task/components/task-overview/task-overview.component';
import { TaskContainerComponent } from '@task/components/task-container/task-container.component';
import { TaskDetailsContainerComponent } from '@task/components/task-details-container/task-details-container.component';

registerLocaleData(localeDe);

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
  SettingsService,
  {
    provide: APP_INITIALIZER,
    useFactory: startupServiceFactory,
    deps: [StartupService],
    multi: true
  },
  provideHttpClient(
    withXsrfConfiguration({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    })
  )
];
const MODULES = [
  TabsModule.forRoot(),
  AlertModule.forRoot(),
  BrowserModule,
  FormsModule,
  AngularSvgIconModule.forRoot(),
  BrowserAnimationsModule,
  ReactiveFormsModule,
  TreeModule,
  MatSidenavModule,
  MatCheckboxModule,
  MatGridListModule,
  MatListModule,
  TypeaheadModule.forRoot(),
  AccordionModule.forRoot(),
  BsDropdownModule.forRoot(),
  CommonModule,
  FormsModule,
  AngularSvgIconModule,
  AlertModule,
  MatFormFieldModule,
  MatAutocompleteModule,
  MatInputModule,
  CommonModule,
  FormsModule,
  TaskListToolbarComponent,
  TaskMasterComponent,
  TaskDetailsComponent,
  TaskInformationComponent,
  TaskAttributeValueComponent,
  TaskCustomFieldsComponent,
  TaskProcessingComponent,
  TaskStatusDetailsComponent,
  TaskListComponent,
  AlertModule.forRoot(),
  TabsModule.forRoot(),
  AngularSvgIconModule,
  MatTabsModule,
  MatButtonModule,
  MatTableModule,
  MatExpansionModule,
  MatCheckboxModule,
  MatDividerModule,
  MatButtonModule,
  ReportTableComponent,
  MonitorComponent,
  TaskPriorityReportComponent,
  TaskPriorityReportFilterComponent,
  CanvasComponent,
  TimestampReportComponent,
  WorkbasketReportComponent,
  WorkbasketReportPlannedDateComponent,
  WorkbasketReportDueDateComponent,
  TaskReportComponent,
  ClassificationReportComponent,
  MatIconModule,
  MatSelectModule,
  MatToolbarModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  NgxsModule.forRoot(STATES, { developmentMode: !environment.production }),
  NgxsReduxDevtoolsPluginModule.forRoot({
    disabled: environment.production,
    maxAge: 25
  }),
  NgxsRouterPluginModule.forRoot(),
  MonitorService,
  MapToIterable,
  NgxsModule.forFeature([TaskState]),
  CommonModule,
  FormsModule,
  AlertModule.forRoot(),
  TypeaheadModule.forRoot(),
  AccordionModule.forRoot(),
  BsDatepickerModule.forRoot(),
  AngularSvgIconModule,
  MatDialogModule,
  MatButtonModule,
  RouterModule,
  TreeModule,
  MatAutocompleteModule,

  SpinnerComponent,
  MasterAndDetailComponent,
  KadaiTreeComponent,
  TypeAheadComponent,
  MapValuesPipe,
  RemoveNoneTypePipe,
  SpreadNumberPipe,
  DateTimeZonePipe,
  NumberToArray,
  OrderBy,
  MapToIterable,
  SortComponent,
  IconTypeComponent,
  FieldErrorDisplayComponent,
  PaginationComponent,
  ProgressSpinnerComponent,
  DialogPopUpComponent,
  WorkbasketFilterComponent,
  TaskFilterComponent,
  DragAndDropDirective,
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  MatTableModule,
  MatSortModule,
  TaskHistoryQueryComponent,
  ResizableWidthDirective
];
const DECLARATIONS = [NavBarComponent, UserInformationComponent, NoAccessComponent, SidenavListComponent];

if (environment.production) {
  enableProdMode();
}

export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  let request = req;
  if (!environment.production) {
    request = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(request);
};

export const httpClientInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const requestInProgressService = inject(RequestInProgressService);
  const tokenExtractor = inject(HttpXsrfTokenExtractor);
  const notificationService = inject(NotificationService);

  let req = request.clone();
  if (req.headers.get('Content-Type') === 'multipart/form-data') {
    const headers = new HttpHeaders();
    req = req.clone({ headers });
  } else {
    req = req.clone({ setHeaders: { 'Content-Type': 'application/hal+json' } });
  }
  let token = tokenExtractor.getToken();
  if (token !== null) {
    req = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
  }
  if (!environment.production) {
    req = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(req).pipe(
    tap(
      () => {},
      (error) => {
        requestInProgressService.setRequestInProgress(false);
        if (
          error.status !== 404 &&
          (!(error instanceof HttpErrorResponse) || error.url.indexOf('environment-information.json') === -1)
        ) {
          const { key, messageVariables } = error.error.error || {
            key: 'FALLBACK',
            messageVariables: {}
          };
          notificationService.showError(key, messageVariables);
        }
      }
    )
  );
};
bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(),
    TaskService,
    ClassificationCategoriesService,
    WorkplaceService,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatSelectModule,
    ReactiveFormsModule,
    CommonModule,
    CommonModule,
    RoutingUploadComponent,
    FormsModule,
    ReactiveFormsModule,
    AngularSvgIconModule,
    AlertModule,
    TypeaheadModule,
    InfiniteScrollModule,
    ScrollingModule,
    WorkbasketOverviewComponent,
    WorkbasketListComponent,
    WorkbasketListToolbarComponent,
    WorkbasketAccessItemsComponent,
    WorkbasketDetailsComponent,
    WorkbasketInformationComponent,
    WorkbasketDistributionTargetsComponent,
    WorkbasketDistributionTargetsListComponent,
    ClassificationOverviewComponent,
    ClassificationListComponent,
    ClassificationTypesSelectorComponent,
    ClassificationDetailsComponent,
    ImportExportComponent,
    AccessItemsManagementComponent,
    MatFormFieldModule,
    MatSelectModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatInputModule,
    MatTooltipModule,
    MatDividerModule,
    CommonModule,
    TaskListComponent,
    TaskOverviewComponent,
    TaskDetailsComponent,
    TaskContainerComponent,
    TaskDetailsContainerComponent,
    ClassificationDefinitionService,
    WorkbasketDefinitionService,
    ClassificationCategoriesService,
    ImportExportService,
    MatListModule,
    MatProgressBarModule,
    MatToolbarModule,
    MatCheckboxModule,
    MatRippleModule,
    MatTableModule,
    MatDialogModule,
    MatExpansionModule,
    AdministrationOverviewComponent,
    MatProgressSpinnerModule,
    AccessIdsService,
    ClassificationsService,
    WorkbasketService,
    ObtainMessageService,
    provideRouter(appRoutes, withHashLocation()),
    provideHttpClient(withInterceptors([tokenInterceptor])),
    importProvidersFrom(...MODULES, ...DECLARATIONS),
    ...PROVIDERS,
    {
      provide: LOCALE_ID,
      useValue: 'de'
    },
    provideHttpClient(withInterceptors([httpClientInterceptor])),
    provideHotToastConfig({
      style: {
        'max-width': '520px'
      }
    })
  ]
});
