/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
export enum IssuerMatchMode {
    FULL_MATCH = "FULL_MATCH",
    PREFIX_MATCH = "PREFIX_MATCH"
}

export interface OIDCIssuer {
    id?: number;
    issuer: string;
    tokenIntrospectionEndpoint?: string;
    matchMode: IssuerMatchMode;
    clientId?: string;
    clientSecret?: string;
}