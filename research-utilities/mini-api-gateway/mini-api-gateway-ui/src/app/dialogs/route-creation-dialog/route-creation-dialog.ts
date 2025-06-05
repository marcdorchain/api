/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component, Inject } from "@angular/core";
import {
    MAT_DIALOG_DATA,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatDialogRef
} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import { IssuerMatchMode, OIDCIssuer } from "../../models/oidc-issuer.interface";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { OIDCIssuerService } from "../../services/oidc-issuer.service";
import { MatDividerModule } from "@angular/material/divider";
import { MatIconModule } from "@angular/material/icon";
import { Route } from "../../models/route.interface";
import { RoutesService } from "../../services/routes.service";
import { Router } from "@angular/router";
import { OneOfValidator, urlValidator } from "../../utils/validators";

@Component({
    selector: 'app-route-creation-dialog',
    templateUrl: 'route-creation-dialog.html',
    styleUrl: 'route-creation-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatFormFieldModule, MatInputModule, FormsModule, ReactiveFormsModule, MatDividerModule, MatIconModule],
})
export class RouteCreationDialogComponent {

    matchMode = IssuerMatchMode;
    

    form = new FormGroup({
        name: new FormControl<string>('', Validators.required),
        version: new FormControl<string>('', Validators.required),
        endpoint: new FormControl<string>('', urlValidator),
        specification: new FormControl<string>(''),
    }, OneOfValidator("endpoint","specification"))

    constructor(private routeService: RoutesService, private dialogRef: MatDialogRef<RouteCreationDialogComponent>) {}

    private getFormValue(name: string){
        const value = this.form.get(name)?.value;
        if(value){
            return value;
        }else{
            return undefined;
        }
    }

    private generateValue(): Route {
        return {
            name: this.getFormValue("name"),
            version: this.getFormValue("version"),
            endpoint: this.getFormValue("endpoint"),
            specification: this.getFormValue("specification"),
            active: false,
            settings: {
                alwaysAllowHEAD: true,
                alwaysAllowOPTIONS: true
            }
        }
    }

    loadSpecificationFromFile(event: Event){
        let file = (event.target as HTMLInputElement).files?.[0];
        if(file){
          let reader = new FileReader()
          reader.onload = (e) => {
            this.form.get("specification")?.setValue(e.target?.result as string);
            (event.target as HTMLInputElement).value = '';
          }
          reader.readAsText(file, "UTF-8");
        }
      }

    save(){
        if(this.form.valid){
            this.routeService.createRoute(this.generateValue()).subscribe({
                next: (value) => {
                    this.dialogRef.close(value);
                }
            })
        }
    }
}