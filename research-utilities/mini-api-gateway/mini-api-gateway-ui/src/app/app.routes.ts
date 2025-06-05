/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Routes } from '@angular/router';
import { RoutesViewComponent } from './components/routes-view/routes-view.component';
import { GlobalFiltersViewComponent } from './components/global-filters-view/global-filters-view.component';
import { SettingsViewComponent } from './components/settings-view/settings-view.component';
import { RouteEditorComponent } from './components/route-editor/route-editor.component';
import { GlobalFilterEditorComponent } from './components/global-filter-editor/global-filter-editor.component';
import { WelcomeViewComponent } from './components/welcome-view/welcome-view.component';
import { authorizedGuard } from './services/auth.service';
import { LoginViewComponent } from './components/login-view/login-view.component';

export const routes: Routes = [
    {path: "", component: WelcomeViewComponent, canMatch: [authorizedGuard], pathMatch: "full"},
    {path: "", component: LoginViewComponent, pathMatch: "full"},
    {path: "routes", component: RoutesViewComponent, canActivate: [authorizedGuard]},
    {path: "routes/:id", component: RouteEditorComponent, canActivate: [authorizedGuard]},
    {path: "global-filters", component: GlobalFiltersViewComponent, canActivate: [authorizedGuard]},
    {path: "global-filters/:name", component: GlobalFilterEditorComponent, canActivate: [authorizedGuard]},
    {path: "settings", component: SettingsViewComponent, canActivate: [authorizedGuard]},
];
