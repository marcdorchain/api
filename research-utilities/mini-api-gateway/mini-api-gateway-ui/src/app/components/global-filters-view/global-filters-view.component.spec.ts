/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalFiltersViewComponent } from './global-filters-view.component';

describe('GlobalFiltersViewComponent', () => {
  let component: GlobalFiltersViewComponent;
  let fixture: ComponentFixture<GlobalFiltersViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalFiltersViewComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GlobalFiltersViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
