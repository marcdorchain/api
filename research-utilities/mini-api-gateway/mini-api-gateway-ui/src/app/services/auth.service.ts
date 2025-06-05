/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { CanActivateFn } from "@angular/router";
import { catchError, map, of, tap } from "rxjs";

interface Basic_Credentials {
    username: string;
    password: string;
}

export const authorizedGuard: CanActivateFn = (route, state) => {
    return inject(AuthorizationService).isAuthorized();
}

@Injectable({ providedIn: 'root' })
export class AuthorizationService {

    constructor(private httpClient: HttpClient){}

    public getCredentials(): Basic_Credentials | null {
        let username = localStorage.getItem("username");
        let password = localStorage.getItem("password");
        if(username == null || password == null){
            return null;
        }else{
            return {
                username: username,
                password: password
            }
        }
    }

    private setCredentials(credentials: Basic_Credentials){
        localStorage.setItem("username", credentials.username);
        localStorage.setItem("password", credentials.password);
    }

    public logout(){
        localStorage.removeItem("username");
        localStorage.removeItem("password");
    }

    public login(credentials: Basic_Credentials){
        return this.httpClient.get<any>(window.location.origin+"/routes/0", 
            {headers: {"Authorization": this.getAuthorizationHeader(credentials), "X-Requested-With": "XMLHttpRequest"}}).pipe(map(
                (result) => true
            ), catchError((err) => {
                if(err instanceof HttpErrorResponse){
                    const error = err as HttpErrorResponse;
                    return of(error.status == 404 || error.status == 204)
                }else{
                    return of(false);
                }
            }),tap((result) => {
                if(result){
                    this.setCredentials(credentials);
                }
            }));
    }

    public isAuthorized(){
        return this.getCredentials() != null;
    }

    public getAuthorizationHeader(credentials?: Basic_Credentials) {
        if(credentials == undefined){
            credentials = this.getCredentials()!
        }
        return "Basic " + btoa(credentials?.username+":"+credentials?.password);
        //return "Basic " + btoa("admin:admin");
    }

    public setAuthorizationHeader(headers?: HttpHeaders){
        if(headers == undefined)
            headers = new HttpHeaders();

        return headers.set("Authorization", this.getAuthorizationHeader());
    }

}