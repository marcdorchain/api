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
import { MatTableModule } from "@angular/material/table";
import { OIDCIssuer } from "../../models/oidc-issuer.interface";
import { OIDCIssuerEditorDialog } from "../oidc-issuer-editor-dialog/oidc-issuer-editor-dialog";

export interface ListConfirmationDialogData {
    title: string;
    text: string;
    list: OIDCIssuer[];
    trueOption?: string;
    falseOption?: string;
    color?: "primary" | "accent" | "warn";
}

@Component({
    selector: 'app-oidc-issuer-list-confirmation-dialog',
    templateUrl: 'oidc-issuer-list-confirmation-dialog.html',
    styleUrl: 'oidc-issuer-list-confirmation-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class OIDCIssuerListConfirmationDialog {
    color: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: ListConfirmationDialogData, private dialogRef: MatDialogRef<OIDCIssuerEditorDialog>) {
        this.color = data.color ? data.color : "primary";
    }
}