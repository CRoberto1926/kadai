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

import { DebugElement } from '@angular/core';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { EMPTY, Observable, of } from 'rxjs';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { ComponentFixture, fakeAsync, flush, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { classificationStateMock, engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { ClassificationDetailsComponent } from './classification-details.component';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import {
  CopyClassification,
  RemoveSelectedClassification,
  RestoreSelectedClassification,
  SaveCreatedClassification,
  SaveModifiedClassification
} from '../../../shared/store/classification-store/classification.actions';
import { By } from '@angular/platform-browser';

jest.mock('angular-svg-icon');

const classificationServiceSpy: Partial<ClassificationsService> = {
  getClassification: jest.fn().mockReturnValue(EMPTY),
  getClassifications: jest.fn().mockReturnValue(EMPTY),
  postClassification: jest.fn().mockReturnValue(EMPTY),
  putClassification: jest.fn().mockReturnValue(EMPTY),
  deleteClassification: jest.fn().mockReturnValue(EMPTY)
};

const classificationCategoriesServiceSpy: Partial<ClassificationCategoriesService> = {
  getCustomisation: jest.fn().mockReturnValue(EMPTY)
};

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: jest.fn().mockReturnValue(of('A')),
  getSelectedDomain: jest.fn().mockReturnValue(EMPTY)
};

const formsValidatorServiceSpy: Partial<FormsValidatorService> = {
  isFieldValid: jest.fn().mockReturnValue(true),
  validateInputOverflow: jest.fn(),
  validateFormInformation: jest.fn().mockImplementation((): Promise<any> => Promise.resolve(true)),
  get inputOverflowObservable(): Observable<Map<string, boolean>> {
    return of(new Map<string, boolean>());
  }
};

const notificationServiceSpy: Partial<NotificationService> = {
  showError: jest.fn(),
  showSuccess: jest.fn(),
  showDialog: jest.fn()
};

describe('ClassificationDetailsComponent', () => {
  let fixture: ComponentFixture<ClassificationDetailsComponent>;
  let debugElement: DebugElement;
  let component: ClassificationDetailsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ClassificationDetailsComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        { provide: ClassificationsService, useValue: classificationServiceSpy },
        { provide: ClassificationCategoriesService, useValue: classificationCategoriesServiceSpy },
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: FormsValidatorService, useValue: formsValidatorServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationDetailsComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock,
      engineConfiguration: engineConfigurationMock
    });
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger onSave() when value exists and onSubmit() is called', async () => {
    component.onSave = jest.fn().mockImplementation();
    await component.onSubmit();
    expect(component.onSave).toHaveBeenCalled();
  });

  it('should show warning when onCopy() is called and isCreatingNewClassification is true', () => {
    component.isCreatingNewClassification = true;
    const notificationService = TestBed.inject(NotificationService);
    const showErrorSpy = jest.spyOn(notificationService, 'showError');
    component.onCopy();
    expect(showErrorSpy).toHaveBeenCalled();
  });

  it('should dispatch action when onCopy() is called and isCreatingNewClassification is false', async () => {
    component.isCreatingNewClassification = false;
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(CopyClassification)).subscribe(() => (isActionDispatched = true));
    component.onCopy();
    expect(isActionDispatched).toBe(true);
  });

  it('should return icon for category when getCategoryIcon() is called and category exists', (done) => {
    const categoryIcon = component.getCategoryIcon('AUTOMATIC');
    categoryIcon.subscribe((iconPair) => {
      expect(iconPair.left).toBe('assets/icons/categories/automatic.svg');
      expect(iconPair.right).toBe('AUTOMATIC');
      done();
    });
  });

  it('should return icon when getCategoryIcon() is called and category does not exist', (done) => {
    const categoryIcon = component.getCategoryIcon('WATER');
    categoryIcon.subscribe((iconPair) => {
      expect(iconPair.left).toBe('assets/icons/categories/missing-icon.svg');
      done();
    });
  });

  it('should dispatch SaveCreatedClassification action in onSave() when classificationId is undefined', async () => {
    component.classification = {};
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SaveCreatedClassification)).subscribe(() => (isActionDispatched = true));
    await component.onSave();
    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch SaveModifiedClassification action in onSave() when classificationId is defined', async () => {
    component.classification = { classificationId: 'ID01' };
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SaveModifiedClassification)).subscribe(() => (isActionDispatched = true));
    await component.onSave();
    expect(isActionDispatched).toBe(true);
  });

  it('should dispatch action in removeClassificationConfirmation() when classification and classificationId exist', () => {
    component.classification = { classificationId: 'ID01' };
    const requestInProgressService = TestBed.inject(RequestInProgressService);
    const setRequestInProgressSpy = jest.spyOn(requestInProgressService, 'setRequestInProgress');
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(RemoveSelectedClassification)).subscribe(() => (isActionDispatched = true));
    component.removeClassificationConfirmation();
    expect(setRequestInProgressSpy).toHaveBeenCalled();
    expect(isActionDispatched).toBe(true);
  });

  /* HTML */

  it('should not show details when spinner is running', () => {
    component.requestInProgress = true;
    component.classification = {};
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('.action-toolbar')).toBeFalsy();
    expect(debugElement.nativeElement.querySelector('.detailed-fields')).toBeFalsy();
  });

  it('should not show details when classification does not exist', () => {
    component.requestInProgress = false;
    component.classification = null;
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('.action-toolbar')).toBeFalsy();
    expect(debugElement.nativeElement.querySelector('.detailed-fields')).toBeFalsy();
  });

  it('should show details when classification exists and spinner is not running', () => {
    expect(debugElement.nativeElement.querySelector('.action-toolbar')).toBeTruthy();
    expect(debugElement.nativeElement.querySelector('.detailed-fields')).toBeTruthy();
  });

  /* HTML: TITLE + ACTION BUTTONS */
  it('should display headline with badge message when a new classification is created', () => {
    component.classification = { name: 'Recommendation', type: 'DOCUMENT' };
    component.isCreatingNewClassification = true;
    fixture.detectChanges();
    const headline = debugElement.nativeElement.querySelector('.action-toolbar__headline');
    expect(headline).toBeTruthy();
    expect(headline.textContent).toContain('Recommendation');
    expect(headline.textContent).toContain('DOCUMENT');
    const badgeMessage = headline.children[1];
    expect(badgeMessage).toBeTruthy();
    expect(badgeMessage.textContent.trim()).toBe('Creating new classification');
  });

  it('should call onSubmit() when button is clicked', async () => {
    const button = debugElement.nativeElement.querySelector('.action-toolbar__save-button');
    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Save');
    expect(button.textContent).toContain('save');
    component.onSubmit = jest.fn().mockImplementation();
    button.click();
    expect(component.onSubmit).toHaveBeenCalled();
  });

  it('should restore selected classification when button is clicked', async () => {
    const button = debugElement.nativeElement.querySelector('.action-toolbar').children[1].children[1];
    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Undo Changes');
    expect(button.textContent).toContain('restore');

    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(RestoreSelectedClassification)).subscribe(() => (isActionDispatched = true));
    button.click();
    expect(isActionDispatched).toBe(true);
  });

  it('should display button to show more actions', () => {
    const button = debugElement.nativeElement.querySelector('#action-toolbar__more-buttons');
    expect(button).toBeTruthy();
    button.click();
    fixture.detectChanges();
    const buttonsInDropdown = debugElement.queryAll(By.css('.action-toolbar__dropdown'));
    expect(buttonsInDropdown.length).toEqual(3);
  });

  it('should not show delete button when creating or copying a Classification', () => {
    component.classification.classificationId = null;
    const button = debugElement.nativeElement.querySelector('#action-toolbar__more-buttons');
    expect(button).toBeTruthy();
    button.click();
    fixture.detectChanges();
    const buttonsInDropdown = debugElement.queryAll(By.css('.action-toolbar__dropdown'));
    expect(buttonsInDropdown.length).toEqual(2);
  });

  it('should call onCopy() when button is clicked', () => {
    const button = debugElement.nativeElement.querySelector('#action-toolbar__more-buttons');
    expect(button).toBeTruthy();
    button.click();
    fixture.detectChanges();
    const copyButton = debugElement.queryAll(By.css('.action-toolbar__dropdown'))[0];
    expect(copyButton.nativeElement.textContent).toContain('content_copy');
    expect(copyButton.nativeElement.textContent).toContain('Copy');
    component.onCopy = jest.fn().mockImplementation();
    copyButton.nativeElement.click();
    expect(component.onCopy).toHaveBeenCalled();
  });

  it('should call onRemoveClassification() when button is clicked', () => {
    const button = debugElement.nativeElement.querySelector('#action-toolbar__more-buttons');
    expect(button).toBeTruthy();
    button.click();
    fixture.detectChanges();
    const deleteButton = debugElement.queryAll(By.css('.action-toolbar__dropdown'))[1];
    expect(deleteButton.nativeElement.textContent).toContain('delete');
    expect(deleteButton.nativeElement.textContent).toContain('Delete');

    const onRemoveClassificationSpy = jest.spyOn(component, 'onRemoveClassification');
    deleteButton.nativeElement.click();
    expect(onRemoveClassificationSpy).toHaveBeenCalled();
    onRemoveClassificationSpy.mockReset();

    const notificationService = TestBed.inject(NotificationService);
    const showDialogSpy = jest.spyOn(notificationService, 'showDialog');
    button.click();
    expect(showDialogSpy).toHaveBeenCalled();
  });

  it('should call onClose() when button is clicked', () => {
    const button = debugElement.nativeElement.querySelector('#action-toolbar__more-buttons');
    expect(button).toBeTruthy();
    button.click();
    fixture.detectChanges();
    const closeButton = debugElement.queryAll(By.css('.action-toolbar__dropdown'))[2];
    expect(closeButton.nativeElement.textContent).toContain('close');
    expect(closeButton.nativeElement.textContent).toContain('close');
    component.onCloseClassification = jest.fn().mockImplementation();
    closeButton.nativeElement.click();
    expect(component.onCloseClassification).toHaveBeenCalled();
  });

  /* DETAILED FIELDS */
  it('should display field-error-display component', () => {
    expect(debugElement.nativeElement.querySelector('kadai-shared-field-error-display')).toBeTruthy();
  });

  it('should display form field for key', () => {
    expect(debugElement.nativeElement.querySelector('#classification-key')).toBeTruthy();
  });

  it('should display form field for name', () => {
    expect(debugElement.nativeElement.querySelector('#classification-name')).toBeTruthy();
  });

  it('should display form field for service level', () => {
    expect(debugElement.nativeElement.querySelector('#classification-service-level')).toBeTruthy();
  });

  it('should display form field for priority', () => {
    expect(debugElement.nativeElement.querySelector('#classification-priority')).toBeTruthy();
  });

  it('should display form field for domain', () => {
    expect(debugElement.nativeElement.querySelector('#classification-domain')).toBeTruthy();
  });

  it('should display form field for application entry point', () => {
    expect(debugElement.nativeElement.querySelector('#classification-application-entry-point')).toBeTruthy();
  });

  it('should display form field for description', () => {
    expect(debugElement.nativeElement.querySelector('#classification-description')).toBeTruthy();
  });

  it('should change isValidInDomain when button is clicked', () => {
    const button = debugElement.nativeElement.querySelector('.detailed-fields__domain-checkbox-icon').parentNode;
    expect(button).toBeTruthy();
    component.classification.isValidInDomain = false;
    button.click();
    expect(component.classification.isValidInDomain).toBe(true);
    button.click();
    expect(component.classification.isValidInDomain).toBe(false);
  });

  it('should not show custom fields with attribute visible = false', () => {
    const inputCustoms = debugElement.queryAll(By.css('.detailed-fields__input-custom-field'));
    expect(inputCustoms).toHaveLength(7);
  });

  it('should save custom field input at position 4 when custom field at position 3 is not visible', fakeAsync(() => {
    const newValue = 'New value';

    let inputCustom3 = debugElement.nativeElement.querySelector('#classification-custom-3');
    let inputCustom4 = debugElement.nativeElement.querySelector('#classification-custom-4');
    expect(inputCustom3).toBeFalsy();
    expect(inputCustom4).toBeTruthy();
    inputCustom4.value = newValue;
    inputCustom4.dispatchEvent(new Event('input'));

    tick();
    fixture.detectChanges();
    flush();

    expect(component.classification['custom3']).toBe(undefined);
    expect(component.classification['custom4']).toBe(newValue);
  }));
});
