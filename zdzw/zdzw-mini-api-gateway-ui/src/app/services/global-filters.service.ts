/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Route } from "../models/route.interface";
import { AuthorizationService } from "./auth.service";
import { GlobalFilter } from "../models/global-filter.interface";

@Injectable({ providedIn: 'root' })
export class GlobalFiltersService {

    private basePath = window.location.origin+"/global-filters";

    constructor(private httpClient: HttpClient, private authService: AuthorizationService){}

    public getGlobalFilters(){
        return this.httpClient.get<GlobalFilter[]>(this.basePath, 
            {headers: this.authService.setAuthorizationHeader()})
    }

    public getGlobalFilter(name: string){
        return this.httpClient.get<GlobalFilter>(this.basePath + "/" + encodeURI(name), 
            {headers: this.authService.setAuthorizationHeader()})
    }

    public createGlobalFilter(filter: GlobalFilter){
        return this.httpClient.post<GlobalFilter>(this.basePath, filter, 
            {headers: this.authService.setAuthorizationHeader()})
    }

    public updateGlobalFilter(filter: GlobalFilter){
        if(filter.name == undefined){
            throw Error("Global filter name to update required");
        }
        return this.httpClient.put<GlobalFilter>(this.basePath + "/" + encodeURI(filter.name), filter.filter,
            {headers: this.authService.setAuthorizationHeader()});
    }

    public deleteGlobalFilter(name: string){
        return this.httpClient.delete<void>(this.basePath + "/" + encodeURI(name),
            {headers: this.authService.setAuthorizationHeader()});
    }

}