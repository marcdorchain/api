/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
export interface Route {
    id?: number;
    name: string;
    version: string;
    specification?: string;
    endpoint: string;
    active: boolean;
    filters?: any[];
    globalFilters?: string[];
    paths?: RoutePath[];
    settings: RouteSettings;
    gatewayEndpoint?: string;
}

export interface RoutePath {
    path: string;
    method: 'POST' |'GET'|'PUT'|'PATCH'|'DELETE'|'HEAD'|'OPTIONS'|'TRACE',
    active: boolean
}

export interface RouteSettings {
    alwaysAllowHEAD: boolean;
    alwaysAllowOPTIONS: boolean;
}