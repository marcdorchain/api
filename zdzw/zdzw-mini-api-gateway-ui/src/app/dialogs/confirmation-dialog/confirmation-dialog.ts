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
    MatDialogClose
} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface ConfirmationDialogData {
    title: string;
    text: string;
    trueOption?: string;
    falseOption?: string;
    color?: "primary" | "accent" | "warn";
}

@Component({
    selector: 'app-confirmation-dialog',
    templateUrl: 'confirmation-dialog.html',
    styleUrl: 'confirmation-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule],
})
export class ConfirmationDialog {
    color: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmationDialogData) {
        this.color = data.color ? data.color : "primary";
    }
}