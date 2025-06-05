/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { SelectionModel } from '@angular/cdk/collections';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { GlobalFilter } from '../../models/global-filter.interface';
import { GlobalFiltersService } from '../../services/global-filters.service';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { ConfirmationDialog } from '../../dialogs/confirmation-dialog/confirmation-dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, of, forkJoin } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { GlobalFilterListResultDialog } from '../../dialogs/global-filter-list-result-dialog/global-filter-list-result-dialog';
import { GlobalFilterListConfirmationDialog } from '../../dialogs/global-filter-list-confirmation-dialog/global-filter-list-confirmation-dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-global-filters-view',
  standalone: true,
  imports: [MatTableModule, MatIconModule, MatButtonModule, MatMenuModule, MatPaginatorModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, MatTooltipModule, RouterLink, CommonModule],
  templateUrl: './global-filters-view.component.html',
  styleUrl: './global-filters-view.component.scss'
})
export class GlobalFiltersViewComponent implements OnInit ,AfterViewInit{
  displayedColumns: string[] = ['select', 'name', 'actions'];
  filters: GlobalFilter[] = [];
  hasLoadedOnce = false;
  dataSource = new MatTableDataSource<GlobalFilter>();
  selection = new SelectionModel<GlobalFilter>(true, []);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private globalFiltersService: GlobalFiltersService, private dialog: MatDialog){}

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (gfilter, filter) => gfilter.name.toLocaleLowerCase().includes(filter);
  }

  ngOnInit(): void {
    this.loadGlobalFilters();
  }

  loadGlobalFilters(){
    this.globalFiltersService.getGlobalFilters().subscribe({
      next: (filters) => {
        this.filters = filters;
        this.dataSource.data = this.filters;
        this.hasLoadedOnce = true;
      },
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  toggleAllRows() {
    this.isAllSelected() ?
    this.selection.clear() :
    this.dataSource.data.forEach(row => this.selection.select(row));
  }

  deleteSingle(element: GlobalFilter){
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      data: {title: "Delete Global Filter \""+element.name+"\"", text: "Do you want to delete the Global Filter \""+element.name+"\"?", color: "warn"},
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result === true){
        this.globalFiltersService.deleteGlobalFilter(element.name).subscribe({
          next: (value) => {
            this.filters.splice(this.filters.indexOf(element),1);
            this.dataSource.data = this.filters;
          },
        });
      }
    });
  }

  deleteSelected(){
    if(!this.selection.isEmpty()){
      const dialogRef = this.dialog.open(GlobalFilterListConfirmationDialog, {
        data: {title: "Delete Global Filters", text: "Do you want to delete the following Global Filters?", list: this.selection.selected, color: "warn"},
      });
      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          const selection = this.selection.selected;
          let requests: Observable<GlobalFilter | any>[] = [];
          for (const filter of selection){
            requests.push(this.globalFiltersService.deleteGlobalFilter(filter.name).pipe(
              catchError((err) =>  of(err))
            ));
          }
          forkJoin(requests).subscribe(
            (values) => {
              let results = [];
              for (let i = 0; i < selection.length; i++) {
                console.log(values[i])
                if(values[i] instanceof HttpErrorResponse){
                  let err: any = (values[i] as HttpErrorResponse);
                  if(Object.hasOwn(err.error, "message")){
                    err = err.error.message;
                  }else{
                    err = err.status + " " + err.statusText;
                  }
                  results.push({filter: selection[i], result: false, error: err});
                }else{
                  results.push({filter: selection[i], result: true});
                }
              }
              this.dialog.open(GlobalFilterListResultDialog, {
                data: {title: "Operation result", text: "Please review the result of deleting the following Global Filters:", list: results},
              });
              this.selection.clear();
              this.loadGlobalFilters();
            }
          )
        }
      })
    }
  }
  

}
