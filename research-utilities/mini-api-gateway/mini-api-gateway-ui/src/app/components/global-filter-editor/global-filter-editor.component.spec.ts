/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalFilterEditorComponent } from './global-filter-editor.component';

describe('RouteEditorComponent', () => {
  let component: GlobalFilterEditorComponent;
  let fixture: ComponentFixture<GlobalFilterEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalFilterEditorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GlobalFilterEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
