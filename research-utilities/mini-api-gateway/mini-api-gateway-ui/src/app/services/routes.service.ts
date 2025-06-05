/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Route } from "../models/route.interface";
import { AuthorizationService } from "./auth.service";

@Injectable({ providedIn: 'root' })
export class RoutesService {

    private basePath = window.location.origin+"/routes";

    constructor(private httpClient: HttpClient, private authService: AuthorizationService){}

    public getRoutes(ids?: number[]){
        let params: HttpParams = new HttpParams();
        if(ids)
            params = params.append("ids", ids.join(","));
        
        return this.httpClient.get<Route[]>(this.basePath, 
            {params: params, headers: this.authService.setAuthorizationHeader()})
    }

    public createRoute(route: Route){
        return this.httpClient.post<Route>(this.basePath, route,
            {headers: this.authService.setAuthorizationHeader()});
    }

    public updateRoute(route: Route){
        console.log(route);
        if(route.id == undefined){
            throw Error("ID for route to update required");
        }
        return this.httpClient.put<Route>(this.basePath + "/" + route.id, route,
            {headers: this.authService.setAuthorizationHeader()});
    }

    public deleteRoute(id: number){
        return this.httpClient.delete<void>(this.basePath + "/" + id,
            {headers: this.authService.setAuthorizationHeader()});
    }

}