/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Route } from "../models/route.interface";
import { AuthorizationService } from "./auth.service";
import { OIDCIssuer } from "../models/oidc-issuer.interface";

@Injectable({ providedIn: 'root' })
export class OIDCIssuerService {

    private basePath = window.location.origin+"/access/oidc-issuers";

    constructor(private httpClient: HttpClient, private authService: AuthorizationService){}

    public getIssuers(){
        // let params: HttpParams = new HttpParams();
        // if(ids)
        //     params = params.append("ids", ids.join(","));
        
        return this.httpClient.get<OIDCIssuer[]>(this.basePath, 
            {headers: this.authService.setAuthorizationHeader()});
    }

    public getIssuer(id: number){
        return this.httpClient.get<OIDCIssuer[]>(this.basePath + "/" + id, 
            {headers: this.authService.setAuthorizationHeader()});
    }

    public createIssuer(issuer: OIDCIssuer){
        return this.httpClient.post<OIDCIssuer>(this.basePath, issuer,
            {headers: this.authService.setAuthorizationHeader()});
    }

    public updateIssuer(issuer: OIDCIssuer){
        if(issuer.id == undefined){
            throw Error("ID for issuer to update required");
        }
        return this.httpClient.put<OIDCIssuer>(this.basePath + "/" + issuer.id, issuer,
            {headers: this.authService.setAuthorizationHeader()});
    }

    public deleteIssuer(id: number){
        return this.httpClient.delete<void>(this.basePath + "/" + id,
            {headers: this.authService.setAuthorizationHeader()});
    }

}