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
    MatDialogClose
} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import { MatTableModule } from "@angular/material/table";
import { OIDCIssuer } from "../../models/oidc-issuer.interface";

export interface OIDCIssuerResult {
    issuer: OIDCIssuer;
    result: boolean;
    error?: string;
}

export interface ResultDialogData {
    title: string;
    text: string;
    list: OIDCIssuerResult[];
}

@Component({
    selector: 'app-oidc-issuer-list-result-dialog',
    templateUrl: 'oidc-issuer-list-result-dialog.html',
    styleUrl: 'oidc-issuer-list-result-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class OIDCIssuerListResultDialog {
    displayedColumns = ['issuer','result'];

    constructor(@Inject(MAT_DIALOG_DATA) public data: ResultDialogData) {
        for (let i = 0; i < data.list.length; i++) {
            if(data.list[i].error != undefined){
                this.displayedColumns.push('error');
                break;
            }
        }
    }
}