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
import { Route } from "../../models/route.interface";
import { MatTableModule } from "@angular/material/table";

export interface ListConfirmationDialogData {
    title: string;
    text: string;
    list: Route[];
    trueOption?: string;
    falseOption?: string;
    color?: "primary" | "accent" | "warn";
}

@Component({
    selector: 'app-route-list-confirmation-dialog',
    templateUrl: 'route-list-confirmation-dialog.html',
    styleUrl: 'route-list-confirmation-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class RouteListConfirmationDialog {
    color: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: ListConfirmationDialogData) {
        this.color = data.color ? data.color : "primary";
    }
}