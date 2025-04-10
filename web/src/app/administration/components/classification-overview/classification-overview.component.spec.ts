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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Component, DebugElement } from '@angular/core';
import { Actions, NgxsModule, ofActionDispatched, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { ClassificationOverviewComponent } from './classification-overview.component';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';
import {
  CreateClassification,
  SelectClassification
} from '../../../shared/store/classification-store/classification.actions';
import { classificationStateMock } from '../../../shared/store/mock-data/mock-store';

@Component({ selector: 'kadai-administration-classification-list', template: '' })
class ClassificationListStub {}

@Component({ selector: 'kadai-administration-classification-details', template: '' })
class ClassificationDetailsStub {}

@Component({ selector: 'svg-icon', template: '' })
class SvgIconStub {}

const routeParamsMock = { id: 'new-classification' };
const activatedRouteMock = {
  firstChild: {
    params: of(routeParamsMock)
  }
};

const classificationCategoriesServiceSpy = jest.fn();
const classificationServiceSpy: Partial<ClassificationsService> = {
  getClassification: jest.fn().mockReturnValue(of()),
  getClassifications: jest.fn().mockReturnValue(of())
};
const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: jest.fn().mockReturnValue(of()),
  getSelectedDomain: jest.fn().mockReturnValue(of('A'))
};

describe('ClassificationOverviewComponent', () => {
  let fixture: ComponentFixture<ClassificationOverviewComponent>;
  let debugElement: DebugElement;
  let component: ClassificationOverviewComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([ClassificationState])],
      declarations: [ClassificationOverviewComponent],
      providers: [
        ClassificationDetailsStub,
        ClassificationListStub,
        SvgIconStub,
        {
          provide: ClassificationsService,
          useValue: classificationServiceSpy
        },
        {
          provide: ClassificationCategoriesService,
          useValue: classificationCategoriesServiceSpy
        },
        { provide: DomainService, useValue: domainServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: activatedRouteMock
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationOverviewComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock
    });
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should always display classification list', () => {
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-list')).toBeTruthy();
  });

  it('should display classification details when showDetail is true', () => {
    component.showDetail = true;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-details')).toBeTruthy();
  });

  it('should show empty page with icon and text when showDetail is false', () => {
    component.showDetail = false;
    fixture.detectChanges();
    const emptyPage = fixture.debugElement.nativeElement.querySelector('.select-classification');
    expect(emptyPage.textContent).toBe('Select a classification');
    expect(debugElement.nativeElement.querySelector('svg-icon')).toBeTruthy();
    expect(debugElement.nativeElement.querySelector('kadai-administration-classification-details')).toBeFalsy();
  });

  it('should set routerParams property when firstChild of route exists', () => {
    expect(component.routerParams).toBe(routeParamsMock);
  });

  it('should dispatch SelectClassification action when routerParams id exists', async () => {
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SelectClassification)).subscribe(() => (isActionDispatched = true));
    component.ngOnInit();
    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch CreateClassification action when routerParams id contains new-classification', async () => {
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (isActionDispatched = true));
    component.ngOnInit();
    expect(isActionDispatched).toBe(true);
  });
});
