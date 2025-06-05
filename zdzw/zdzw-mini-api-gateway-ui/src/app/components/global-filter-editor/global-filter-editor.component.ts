/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormGroup, FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouteFilter, RouteFilterType } from '../../models/route-filter.interface';
import { ConfirmationDialog } from '../../dialogs/confirmation-dialog/confirmation-dialog';
import { MatDialog } from '@angular/material/dialog';
import { GlobalFilter } from '../../models/global-filter.interface';
import { GlobalFiltersService } from '../../services/global-filters.service';
import { RouteFilterTypeNameMapping } from '../../utils/validators';
import { FilterFormComponent } from "../filter-form/filter-form.component";
import { FilterSelectComponent } from '../filter-select/filter-select.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import { RecreateViewDirective } from '../../directives/recreateViewKey.directive';
import {MatDividerModule} from '@angular/material/divider';
import { arrayEquals } from '../../utils/utils';

@Component({
    selector: 'app-global-filter-editor',
    standalone: true,
    templateUrl: './global-filter-editor.component.html',
    styleUrl: './global-filter-editor.component.scss',
    imports: [CommonModule, MatProgressSpinnerModule, MatIconModule, MatButtonModule, MatTooltipModule, MatFormFieldModule, MatInputModule, FormsModule, ReactiveFormsModule, FilterFormComponent, FilterSelectComponent, MatSidenavModule, RecreateViewDirective, MatDividerModule]
})
export class GlobalFilterEditorComponent implements OnInit{

  routeFilterType = RouteFilterType;
  routeFilterTypeName = RouteFilterTypeNameMapping;

  gfilter?: GlobalFilter;
  type!: RouteFilterType;
  //originalRoute?: Route;

  // private filtersValidator: ValidatorFn = (control) => {
  //   for(let subControl of (control as FormArray<FormControl>).controls){
  //     if(subControl.invalid){
  //       return {"invalid": true};
  //     }
  //   }
  //   return null;
  // }

  
  form = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    filter: new FormControl<RouteFilter|null>(null, Validators.required)
  });

  availableGlobalFilters: GlobalFilter[] = [];

  constructor(private angularRoute: ActivatedRoute, 
    private dialog: MatDialog, 
    private router: Router,
    private globalFiltersService: GlobalFiltersService){}

  ngOnInit(): void {
    this.angularRoute.paramMap.pipe(
      switchMap((params: ParamMap) => {
        let name = params.get('name')!;
        if(name != "_new"){
          return this.globalFiltersService.getGlobalFilter(name)
        }else{
          return of(undefined);
        }
      })
    ).subscribe({
      next: (result) => {
        if(result){
          this.intialise(result)
        }else{
          this.type = RouteFilterType.EXTERNAL_CALL;
        }
      }
    });
    this.globalFiltersService.getGlobalFilters().subscribe({
      next: (value) => this.availableGlobalFilters = value
    });
  }

  private intialise(gfilter: GlobalFilter){
    this.gfilter = gfilter; 
    this.type = gfilter.filter.type;
    //this.originalRoute = Object.assign({}, this.route);
    this.form.reset({
      name: this.gfilter.name,
      filter: Object.assign({}, this.gfilter.filter),
    });
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
    return !this.gfilter || !arrayEquals(Object.entries(this.gfilter!.filter), Object.entries(this.getFieldValue('filter')));
  }

  delete(){
    if(this.gfilter){
      const dialogRef = this.dialog.open(ConfirmationDialog, {
        data: {title: "Delete Global Filter \""+this.gfilter?.name+"\"", text: "Do you want to delete the Global Filter \""+this.gfilter?.name+"\"?", color: "warn"},
      });

      dialogRef.afterClosed().subscribe(result => {
        if(result === true){
          this.globalFiltersService.deleteGlobalFilter(this.gfilter?.name!).subscribe({
            next: (value) => {
              this.router.navigateByUrl("/global-filters");
            },
          });
        }
      });
    }else{
      this.router.navigate(["/global-filters"])
    }
  }

  saveChanges(){
    const req: GlobalFilter = {
      name: this.getFieldValue('name'),
      filter: this.getFieldValue('filter')
    }
    if(this.gfilter){
      this.globalFiltersService.updateGlobalFilter(req).subscribe({
        next: (value) => {
          this.intialise(value);
        }
      });
    }else{
      this.globalFiltersService.createGlobalFilter(req).subscribe({
        next: (value) => {
          this.router.navigate(["/global-filters",value.name])
        }
      });
    }
  }

}
