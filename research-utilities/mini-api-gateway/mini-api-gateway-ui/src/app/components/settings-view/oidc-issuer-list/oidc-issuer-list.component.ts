/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { OIDCIssuer } from '../../../models/oidc-issuer.interface';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialog } from '../../../dialogs/confirmation-dialog/confirmation-dialog';
import { OIDCIssuerService } from '../../../services/oidc-issuer.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, of, forkJoin } from 'rxjs';
import { OIDCIssuerListResultDialog } from '../../../dialogs/oidc-issuer-list-result-dialog/oidc-issuer-list-result-dialog';
import { OIDCIssuerListConfirmationDialog } from '../../../dialogs/oidc-issuer-list-confirmation-dialog/oidc-issuer-list-confirmation-dialog';
import { OIDCIssuerEditorDialog } from '../../../dialogs/oidc-issuer-editor-dialog/oidc-issuer-editor-dialog';

@Component({
  selector: 'app-oidc-issuer-list',
  standalone: true,
  imports: [MatButtonModule, MatMenuModule, MatTabsModule, MatIconModule, MatFormFieldModule, MatTableModule, MatPaginatorModule, MatInputModule, MatCheckboxModule, CommonModule],
  templateUrl: './oidc-issuer-list.component.html',
  styleUrl: './oidc-issuer-list.component.scss'
})
export class OidcIssuerListComponent implements OnInit, AfterViewInit{
  displayedColumns: string[] = ['select', 'id', 'issuer', 'actions'];
  issuers: OIDCIssuer[] = [];
  hasLoadedOnce = false;
  dataSource = new MatTableDataSource<OIDCIssuer>();
  selection = new SelectionModel<OIDCIssuer>(true, []);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private oidcService: OIDCIssuerService, private dialog: MatDialog){}

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (issuer, filter) => issuer.issuer.toLocaleLowerCase().includes(filter) || (!Number.isNaN(Number.parseInt(filter)) && Number.parseInt(filter) == issuer.id);
  }

  ngOnInit(): void {
    this.loadIssuers();
  }

  loadIssuers(){
    this.oidcService.getIssuers().subscribe({
      next: (result) => {
        this.issuers = result;
        this.dataSource.data = this.issuers;
        this.hasLoadedOnce = true;
      }
    })
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

  deleteSingle(element: OIDCIssuer){
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      data: {title: "Delete OpenID Connect Issuer", text: "Do you want to delete the OpenID Connect Issuer \""+element.issuer+"\"?", color: "warn"},
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result === true){
        this.oidcService.deleteIssuer(element.id!).subscribe({
          next: (value) => {
            this.issuers.splice(this.issuers.indexOf(element),1);
            this.dataSource.data = this.issuers;
          },
        });
      }
    });
  }

  deleteSelected(){
    if(!this.selection.isEmpty()){
      const dialogRef = this.dialog.open(OIDCIssuerListConfirmationDialog, {
        data: {title: "Delete issuers", text: "Do you want to delete the following OpenID Connect Issuers?", list: this.selection.selected, color: "warn"},
      });
      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          const selection = this.selection.selected;
          let requests: Observable<OIDCIssuer | any>[] = [];
          for (const issuer of selection){
            requests.push(this.oidcService.deleteIssuer(issuer.id!).pipe(
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
                  results.push({issuer: selection[i], result: false, error: err});
                }else{
                  results.push({issuer: selection[i], result: true});
                }
              }
              this.dialog.open(OIDCIssuerListResultDialog, {
                data: {title: "Operation result", text: "Please review the result of deleting the following OpenID Connect Issuers:", list: results},
              });
              this.selection.clear();
              this.loadIssuers();
            }
          )
        }
      })
    }
  }

  edit(issuer: OIDCIssuer | null){
    this.dialog.open(OIDCIssuerEditorDialog,
      { data: issuer }).afterClosed().subscribe({
        next: (value) => {
          if(value === true){
            this.loadIssuers()
          }
        }
      })
  }

}
