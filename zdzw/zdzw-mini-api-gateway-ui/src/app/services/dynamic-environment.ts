/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class DynamicEnvironment {

   private _environment: any;

   constructor(private httpClient: HttpClient) {}

   public async loadEnvironment(){
      this._environment = await firstValueFrom(this.httpClient.get<any>("/ui/config.json"));
      return Promise.resolve();
   }

   get environment() : any {
      return this._environment;
   }
}