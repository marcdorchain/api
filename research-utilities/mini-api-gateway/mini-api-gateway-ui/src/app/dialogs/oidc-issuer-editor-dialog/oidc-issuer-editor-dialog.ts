/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component, Inject } from "@angular/core";
import {
    MatDialog,
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
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import { arrayEquals } from "../../utils/utils";
import { OIDCIssuerService } from "../../services/oidc-issuer.service";

@Component({
    selector: 'app-oidc-issuer-editor-dialog',
    templateUrl: 'oidc-issuer-editor-dialog.html',
    styleUrl: 'oidc-issuer-editor-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatFormFieldModule, MatInputModule, FormsModule, ReactiveFormsModule, MatButtonToggleModule],
})
export class OIDCIssuerEditorDialog {

    matchMode = IssuerMatchMode;
    

    form = new FormGroup({
        issuer: new FormControl<string>('', Validators.required),
        tokenIntrospectionEndpoint: new FormControl<string>(''),
        matchMode: new FormControl<IssuerMatchMode>(IssuerMatchMode.FULL_MATCH, Validators.required),
        clientId: new FormControl<string>(''),
        clientSecret: new FormControl<string>('')
    })

    constructor(@Inject(MAT_DIALOG_DATA) public data: OIDCIssuer | undefined, private oidcService: OIDCIssuerService, private dialogRef: MatDialogRef<OIDCIssuerEditorDialog>) {
        if(data){
            this.form.reset(data);
        }
    }

    private getFormValue(name: string){
        const value = this.form.get(name)?.value;
        if(value){
            return value;
        }else{
            return undefined;
        }
    }

    private generateValue(): OIDCIssuer {
        return {
            id: this.data?.id,
            issuer: this.getFormValue("issuer"),
            matchMode: this.getFormValue("matchMode"),
            clientId: this.getFormValue("clientId"),
            clientSecret: this.getFormValue("clientSecret"),
            tokenIntrospectionEndpoint: this.getFormValue("tokenIntrospectionEndpoint")
        }
    }

    hasChanges(){
        return !this.data 
            || this.data.clientId != this.getFormValue("clientId")
            || this.data.clientSecret != this.getFormValue("clientSecret")
            || this.data.issuer != this.getFormValue("issuer")
            || this.data.matchMode != this.getFormValue("matchMode")
            || this.data.tokenIntrospectionEndpoint != this.getFormValue("tokenIntrospectionEndpoint");
    }

    save(){
        if(this.form.valid){
            if(this.data){
                this.oidcService.updateIssuer(this.generateValue()).subscribe({
                    next: (value) => {
                        this.data = value;
                    }
                });
            }else{
                this.oidcService.createIssuer(this.generateValue()).subscribe({
                    next: (value) => {
                        this.dialogRef.close(true);
                    }
                })
            }
        }
    }
}