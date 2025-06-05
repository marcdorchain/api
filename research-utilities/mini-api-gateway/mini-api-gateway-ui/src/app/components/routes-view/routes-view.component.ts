/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import { Route } from '../../models/route.interface';
import { RoutesService } from '../../services/routes.service';
import { SelectionModel } from '@angular/cdk/collections';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialog } from '../../dialogs/confirmation-dialog/confirmation-dialog';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { RouteListConfirmationDialog } from '../../dialogs/route-list-confirmation-dialog/route-list-confirmation-dialog';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { RouteListResultDialog } from '../../dialogs/route-list-result-dialog/route-list-result-dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import {MAT_SLIDE_TOGGLE_DEFAULT_OPTIONS, MatSlideToggle, MatSlideToggleDefaultOptions, MatSlideToggleModule} from '@angular/material/slide-toggle';
import { RouteCreationDialogComponent } from '../../dialogs/route-creation-dialog/route-creation-dialog';

@Component({
  selector: 'app-routes-view',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule, MatTableModule, MatCheckboxModule, MatIconModule, MatButtonModule, MatPaginatorModule, CommonModule, MatMenuModule, MatTooltipModule, RouterLink, MatSlideToggleModule],
  templateUrl: './routes-view.component.html',
  styleUrl: './routes-view.component.scss'
})
export class RoutesViewComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['select', 'id', 'name', 'version', 'active','actions'];
  routes: Route[] = [];
  hasLoadedOnce = false;
  dataSource = new MatTableDataSource<Route>();
  selection = new SelectionModel<Route>(true, []);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private routesService: RoutesService, private dialog: MatDialog, private router: Router){}

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (route, filter) => route.name.toLocaleLowerCase().includes(filter) || (!Number.isNaN(Number.parseInt(filter)) && Number.parseInt(filter) == route.id);
  }

  ngOnInit(): void {
    this.loadRoutes();
  }

  loadRoutes(){
    this.routesService.getRoutes().subscribe({
      next: (routes) => {
        this.routes = routes;
        this.dataSource.data = this.routes;
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

  toggleActive(element: MatSlideToggle, route: Route){
    let action = route.active ? "Deactivate" : "Activate";
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      data: {title: action + " Route \""+route.name+"\"", text: "Do you want to "+action.toLocaleLowerCase()+" the route \""+route.name+"\"?"},
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result === true){
        let req = Object.assign({}, route);
        req.active = !route.active;
        this.routesService.updateRoute(req).subscribe({
          next: (value) => {
            route.active = !route.active;
            this.routes[this.routes.indexOf(route)] = value;
            this.dataSource.data = this.routes;
          },
        })
      }else{
        element.checked = route.active;
      }
    });
  }

  deleteSingle(element: Route){
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      data: {title: "Delete Route \""+element.name+"\"", text: "Do you want to delete the route \""+element.name+"\"?", color: "warn"},
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result === true){
        this.routesService.deleteRoute(element.id!).subscribe({
          next: (value) => {
            element.active = !element.active;
            this.routes.splice(this.routes.indexOf(element),1);
            this.dataSource.data = this.routes;
          },
        });
      }
    });
  }

  activateSelected(value: boolean){
    if(!this.selection.isEmpty()){
      const action = value ? "Activate" : "Deactivate";
      const dialogRef = this.dialog.open(RouteListConfirmationDialog, {
        data: {title: action+" routes", text: "Do you want to "+action.toLocaleLowerCase()+" the following routes?", list: this.selection.selected},
      });
      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          const selection = this.selection.selected;
          let requests: Observable<Route | any>[] = [];
          for (const route of selection){
            const req = Object.assign({}, route);
            req.active = value;
            requests.push(this.routesService.updateRoute(req).pipe(
              catchError((err) =>  of(err))
            ));
          }
          forkJoin(requests).subscribe(
            (values) => {
              const action2 = value ? "Activating" : "Deactivating";
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
                  results.push({route: selection[i], result: false, error: err});
                }else{
                  results.push({route: selection[i], result: true});
                  this.routes[this.routes.indexOf(selection[i])] = values[i];
                }
              }
              this.dialog.open(RouteListResultDialog, {
                data: {title: "Operation result", text: "Please review the result of "+action2.toLocaleLowerCase()+" the following routes:", list: results},
              });
              this.selection.clear();
              this.dataSource.data = this.routes;
            }
          )
        }
      })
    }
  }

  deleteSelected(){
    if(!this.selection.isEmpty()){
      const dialogRef = this.dialog.open(RouteListConfirmationDialog, {
        data: {title: "Delete routes", text: "Do you want to delete the following routes?", list: this.selection.selected, color: "warn"},
      });
      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          const selection = this.selection.selected;
          let requests: Observable<Route | any>[] = [];
          for (const route of selection){
            requests.push(this.routesService.deleteRoute(route.id!).pipe(
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
                  results.push({route: selection[i], result: false, error: err});
                }else{
                  results.push({route: selection[i], result: true});
                }
              }
              this.dialog.open(RouteListResultDialog, {
                data: {title: "Operation result", text: "Please review the result of deleting the following routes:", list: results},
              });
              this.selection.clear();
              this.loadRoutes();
            }
          )
        }
      })
    }
  }

  create(){
    this.dialog.open(RouteCreationDialogComponent, {}).afterClosed().subscribe({
      next: (value) => {
        if(value !== false){
          const route = value as Route;
          this.router.navigate(["/routes", route.id])
        }
      }
    })
  }
}
