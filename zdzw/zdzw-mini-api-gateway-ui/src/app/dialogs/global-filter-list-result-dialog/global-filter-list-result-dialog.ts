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

export interface GlobalFilterResult {
    filter: GlobalFilter;
    result: boolean;
    error?: string;
}

export interface ResultDialogData {
    title: string;
    text: string;
    list: GlobalFilterResult[];
}

@Component({
    selector: 'app-global-filter-list-result-dialog',
    templateUrl: 'global-filter-list-result-dialog.html',
    styleUrl: 'global-filter-list-result-dialog.scss',
    standalone: true,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButtonModule, MatTableModule],
})
export class GlobalFilterListResultDialog {
    displayedColumns = ['name','result'];

    constructor(@Inject(MAT_DIALOG_DATA) public data: ResultDialogData) {
        for (let i = 0; i < data.list.length; i++) {
            if(data.list[i].error != undefined){
                this.displayedColumns.push('error');
                break;
            }
        }
    }
}