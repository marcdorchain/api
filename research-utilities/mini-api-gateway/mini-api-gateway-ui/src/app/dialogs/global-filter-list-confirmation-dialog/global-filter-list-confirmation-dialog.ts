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
import { GlobalFilter } from "../../models/global-filter.interface";

export interface ListConfirmationDialogData {
    title: string;
    text: string;
    list: GlobalFilter[];
    trueOption?: string;
    falseOption?: string;
    color?: "primary" | "accent" | "warn";
}

@Component({
    selector: 'app-global-filter-list-confirmation-dialog',
    templateUrl: 'global-filter-list-confirmation-dialog.html',
    styleUrl: 'global-filter-list-confirmation-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class GlobalFilterListConfirmationDialog {
    color: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: ListConfirmationDialogData) {
        this.color = data.color ? data.color : "primary";
    }
}