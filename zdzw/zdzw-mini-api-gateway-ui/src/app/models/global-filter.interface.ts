/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { RouteFilter } from "./route-filter.interface";

export interface GlobalFilter {
    name: string;
    filter: RouteFilter;
}