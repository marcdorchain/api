/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OidcIssuerListComponent } from './oidc-issuer-list.component';

describe('OidcIssuerListComponent', () => {
  let component: OidcIssuerListComponent;
  let fixture: ComponentFixture<OidcIssuerListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OidcIssuerListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OidcIssuerListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
