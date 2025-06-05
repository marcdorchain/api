/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoutesViewComponent } from './routes-view.component';

describe('RoutesViewComponent', () => {
  let component: RoutesViewComponent;
  let fixture: ComponentFixture<RoutesViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoutesViewComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RoutesViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
