/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { Route } from '../../models/route.interface';
import { RoutesService } from '../../services/routes.service';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormGroup, FormControl, FormsModule, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { RouteFilter, RouteFilterType } from '../../models/route-filter.interface';
import { ConfirmationDialog } from '../../dialogs/confirmation-dialog/confirmation-dialog';
import { MatDialog } from '@angular/material/dialog';
import { GlobalFilter } from '../../models/global-filter.interface';
import { GlobalFiltersService } from '../../services/global-filters.service';
import {
  CdkDragDrop,
  moveItemInArray,
  CdkDrag,
  CdkDropList,
} from '@angular/cdk/drag-drop';
import { RouteFilterTypeNameMapping, urlValidator } from '../../utils/validators';
import { FilterFormComponent } from "../filter-form/filter-form.component";
import { FilterSelectComponent } from '../filter-select/filter-select.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatExpansionModule} from '@angular/material/expansion';
import { arrayEquals } from '../../utils/utils';
import { MatDividerModule } from '@angular/material/divider';

@Component({
    selector: 'app-route-editor',
    standalone: true,
    templateUrl: './route-editor.component.html',
    styleUrl: './route-editor.component.scss',
    imports: [CommonModule, MatProgressSpinnerModule, MatTabsModule, MatIconModule, MatButtonModule, MatTooltipModule, MatFormFieldModule, MatInputModule, FormsModule, ReactiveFormsModule, CdkDropList, CdkDrag, FilterFormComponent, FilterSelectComponent, MatSidenavModule, MatExpansionModule, MatDividerModule]
})
export class RouteEditorComponent implements OnInit{

  routeFilterType = RouteFilterType;
  routeFilterTypeName = RouteFilterTypeNameMapping;

  route?: Route ;

  form = new FormGroup({
    endpoint: new FormControl<string|null>(null, [urlValidator]),
    specification: new FormControl<string|null>(null),
    filters: new FormArray<FormControl<RouteFilter|null>>([]),
    globalFilters: new FormControl<string[]|null>([]),
  });

  availableGlobalFilters: GlobalFilter[] = [];

  constructor(private angularRoute: ActivatedRoute, 
    private routesService: RoutesService, 
    private dialog: MatDialog, 
    private router: Router,
    private globalFiltersService: GlobalFiltersService){}

  ngOnInit(): void {
    this.angularRoute.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.routesService.getRoutes([Number.parseInt(params.get('id')!)]))
    ).subscribe({
      next: (result) => {
        this.intialise(result[0])
      }
    });
    this.globalFiltersService.getGlobalFilters().subscribe({
      next: (value) => this.availableGlobalFilters = value
    });
  }

  private intialise(route: Route){
    this.route = route; 
    //this.originalRoute = Object.assign({}, this.route);
    this.form.controls["filters"].clear();
    this.form.reset({
      endpoint: this.route.endpoint,
      specification: this.route.specification ? this.route.specification : null,
      globalFilters: this.route.globalFilters ?  this.route.globalFilters.concat() : []
    });
    if(route.filters){
      route.filters.forEach((filter) => this.form.controls["filters"].push(new FormControl<RouteFilter>(filter, Validators.required)));
    }
  }

  private getFieldValue(name: string): any{
    let value = this.form.get(name)?.value;
    if(value == null){
      return undefined;
    }else if(typeof value === "string" || value instanceof String ){
      if((value as string).trim().length == 0){
        return undefined;
      }else{
        return (value as string).trim();
      }
    }else if(value instanceof Array && value.length == 0){
      return undefined;
    }else{
      return value;
    }
  }

  

  hasChanges(): boolean {
    return this.route?.endpoint != this.getFieldValue('endpoint')
         || !arrayEquals(this.route?.filters!, this.getFilters()!)
         || !arrayEquals(this.route?.globalFilters!, this.getFieldValue('globalFilters'))
         || this.route?.specification != this.getFieldValue('specification');
  }

  loadSpecificationFromFile(event: Event){
    let file = (event.target as HTMLInputElement).files?.[0];
    if(file){
      let reader = new FileReader()
      reader.onload = (e) => {
        this.form.get("specification")?.setValue(e.target?.result as string);
        (event.target as HTMLInputElement).value = '';
      }
      reader.readAsText(file, "UTF-8");
    }
  }

  delete(){
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      data: {title: "Delete Route \""+this.route?.name+"\"", text: "Do you want to delete the route \""+this.route?.name+"\"?", color: "warn"},
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result === true){
        this.routesService.deleteRoute(this.route?.id!).subscribe({
          next: (value) => {
            this.router.navigateByUrl("/routes");
          },
        });
      }
    });
  }

  toggleActivation(){
    const action = this.route?.active ? "deactivate" : "activate";
    if(this.hasChanges()){
      const dialogRef = this.dialog.open(ConfirmationDialog, {
        data: {title: "Discard changes", text: "If you "+action+" the route now, your current changes are discarded. Do you want to proceed?", color: "warn"},
      });

      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          this.setActivation(!this.route?.active);
        }
      })
    }else{
      this.setActivation(!this.route?.active);
    }
  }

  private getFilters(){
    let controls = this.form.controls["filters"].controls;
    if(controls.length == 0){
      return undefined;
    }else{
      return controls.map((control) => control.value);
    }
  }

  saveChanges(){
    const req = Object.assign({}, this.route);
    req.endpoint = this.getFieldValue('endpoint');
    req.filters = this.getFilters();
    req.globalFilters = this.getFieldValue('globalFilters');
    req.specification = this.getFieldValue('specification');
    this.routesService.updateRoute(req).subscribe({
      next: (value) => {
        this.intialise(value);
      }
    })
  }

  private setActivation(value: boolean){
    const req = Object.assign({}, this.route);
    req.active = value;
    this.routesService.updateRoute(req).subscribe({
      next: (value) => {
        this.intialise(value);
      },
    });
  }

  drop(event: CdkDragDrop<any>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      this.form.controls['globalFilters'].value!.splice(event.currentIndex,0, this.availableGlobalFilters[event.previousIndex].name);
    }
  }

  reorderFilter(event: CdkDragDrop<any>) {
    moveItemInArray(this.form.controls["filters"].controls, event.previousIndex, event.currentIndex);
  }

  removeGlobalFilter(index: number){
    this.form.get('globalFilters')?.value?.splice(index, 1);
  }

  addFilter(type: RouteFilterType){
    this.form.controls["filters"].push(new FormControl<RouteFilter>({type: type}, Validators.required));
  }

  removeFilter(index: number){
    this.form.controls["filters"].removeAt(index);
  }

}
