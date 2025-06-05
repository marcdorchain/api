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

export interface RouteResult {
    route: Route;
    result: boolean;
    error?: string;
}

export interface ResultDialogData {
    title: string;
    text: string;
    list: RouteResult[];
}

@Component({
    selector: 'app-route-list-result-dialog',
    templateUrl: 'route-list-result-dialog.html',
    styleUrl: 'route-list-result-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class RouteListResultDialog {
    displayedColumns = ['id','name','version','result'];

    constructor(@Inject(MAT_DIALOG_DATA) public data: ResultDialogData) {
        for (let i = 0; i < data.list.length; i++) {
            if(data.list[i].error != undefined){
                this.displayedColumns.push('error');
                break;
            }
        }
    }
}