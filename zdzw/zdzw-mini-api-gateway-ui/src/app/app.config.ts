/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { APP_BASE_HREF } from '@angular/common';
import { DynamicEnvironment } from './services/dynamic-environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    importProvidersFrom(HttpClientModule),
    {provide: APP_BASE_HREF, useValue: '/ui/'},
    DynamicEnvironment,
    {
      provide: APP_INITIALIZER, 
      useFactory: (dynEnvironment: DynamicEnvironment) => () => dynEnvironment.loadEnvironment(),
      deps: [DynamicEnvironment],
      multi: true
    }
  ]
};