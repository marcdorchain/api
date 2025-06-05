/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
export enum RouteFilterType {
    EXTERNAL_CALL = "EXTERNAL_CALL",
    SET_HEADERS = "SET_HEADERS",
    OAUTH2_CLIENT = "OAUTH2_CLIENT",
    REMOVE_REQUEST_HEADERS = "REMOVE_REQUEST_HEADERS"
}

export interface RouteFilter {
    type: RouteFilterType;
}

export enum ExternalCall_HTTPMethod {
    CONNECT = "CONNECT",
    DELETE = "DELETE",
    GET = "GET",
    HEAD = "HEAD",
    OPTIONS = "OPTIONS",
    POST = "POST",
    PUT = "PUT",
    PATCH = "PATCH",
    TRACE = "TRACE"
}

export interface ExternalCall_RouteFilter extends RouteFilter {
    type: RouteFilterType.EXTERNAL_CALL;
    uri: string;
    method: ExternalCall_HTTPMethod | string;
    body?: string;
    headers?: {[key: string]: string | string[]};
    responseMapping?: {[key: string]: string};
}

export interface OAuth2Client_RouteFilter extends RouteFilter {
    type: RouteFilterType.OAUTH2_CLIENT;
    clientId: string;
    clientSecret: string;
    issuerUrl: string;
}

export interface SetHeaders_RouteFilter extends RouteFilter {
    type: RouteFilterType.SET_HEADERS;
    headers: {[key: string]: string};
}

export interface RemoveRequestHeaders_RouteFilter extends RouteFilter {
    type: RouteFilterType.REMOVE_REQUEST_HEADERS;
    headersToRemove: string[];
}