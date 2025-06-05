/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExternalCall_HTTPMethod, ExternalCall_RouteFilter, OAuth2Client_RouteFilter, RemoveRequestHeaders_RouteFilter, RouteFilter, RouteFilterType, SetHeaders_RouteFilter, ZDZWLicenseVerification_RouteFilter, ZDZWSmartContractVerification_RouteFilter } from '../../models/route-filter.interface';
import { NonEmptyArrayValidator, urlValidator } from '../../utils/validators';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-filter-form',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, MatInputModule, CommonModule, MatIconModule, MatButtonModule, MatSelectModule],
  templateUrl: './filter-form.component.html',
  styleUrl: './filter-form.component.scss'
})
export class FilterFormComponent implements OnInit {

  filterType = RouteFilterType;
  requestMethods = ExternalCall_HTTPMethod;
  
  @Input() value?: RouteFilter;

  @Output() valueChange = new EventEmitter<RouteFilter>;

  @Input() control?: AbstractControl<RouteFilter> | AbstractControl<RouteFilter | null>;

  @Input({ alias: "type", required: true })
  type!: RouteFilterType;

  form: FormGroup = new FormGroup({});
  Object = Object;

  constructor(private fb: FormBuilder){}

  ngOnInit(): void {
    this.type = this.type;
    console.log(this.type);
    console.log(this.value)
    switch (this.type) {
      case RouteFilterType.ZDZW_SMARTCONTRACT_VERIFICATION:
        break;
      case RouteFilterType.ZDZW_LICENSE_VERIFICATION:
        this.initZDZWLicenseVerification();
        break;
      case RouteFilterType.EXTERNAL_CALL:
        this.initExternalCall();
        break;
      case RouteFilterType.SET_HEADERS:
        this.initSetHeaders();
        break;
      case RouteFilterType.OAUTH2_CLIENT:
        this.initOAuth2Client();
        break;
      case RouteFilterType.REMOVE_REQUEST_HEADERS:
        this.initRemoveRequestHeaders();
    }
    this.onChange()
  }

  private initZDZWLicenseVerification(){
    const input = this.value ? this.value as ZDZWLicenseVerification_RouteFilter : undefined;
    this.form.addControl("appId", new FormControl<string>(input?.appId || '', [Validators.required]));
  }

  private initExternalCall(){
    const input = this.value ? this.value as ExternalCall_RouteFilter : undefined;
    this.form.addControl("uri", new FormControl<string>(input?.uri || '', [Validators.required, urlValidator]));
    this.form.addControl("method", new FormControl<ExternalCall_HTTPMethod|string>(input?.method || ExternalCall_HTTPMethod.GET ,Validators.required))
    this.form.addControl("body", new FormControl<string>(input?.body || ''));
    this.form.addControl("headers", new FormArray<FormGroup>([]));
    if(input?.headers){
      for(let entry of Object.entries(input.headers)){
        if(entry[1] instanceof Array){
          for(let val of entry[1]){
            this.getFormArrayWithGroups("headers").push(
              this.fb.group({
                key: new FormControl(entry[0], [Validators.required]),
                value: new FormControl(val, [Validators.required])
            }));
          }
        }else{
          this.getFormArrayWithGroups("headers").push(
            this.fb.group({
              key: new FormControl(entry[0], [Validators.required]),
              value: new FormControl(entry[1], [Validators.required])
          }));
        }
      };
    }
    this.form.addControl("responseMapping", new FormArray<FormGroup>([]));
    if(input?.responseMapping){
      for(let entry of Object.entries(input.responseMapping)){
        this.getFormArrayWithGroups("responseMapping").push(
          this.fb.group({
            key: new FormControl(entry[0], [Validators.required]),
            value: new FormControl(entry[1], [Validators.required])
        }));
      };
    }
  }

  private initOAuth2Client(){
    const input = this.value ? this.value as OAuth2Client_RouteFilter : undefined;
    this.form.addControl("clientId", new FormControl<string>(input?.clientId || '', [Validators.required]));
    this.form.addControl("clientSecret", new FormControl<string>(input?.clientSecret || '', [Validators.required]));
    this.form.addControl("issuerUrl", new FormControl<string>(input?.issuerUrl || '', [Validators.required, urlValidator]));
  }

  private initSetHeaders(){
    const input = this.value ? this.value as SetHeaders_RouteFilter : undefined;
    this.form.addControl("headers", new FormArray<FormGroup>([], Validators.required));
    if(input?.headers){
      for(let entry of Object.entries(input.headers)){
        this.getFormArrayWithGroups("headers").push(
          this.fb.group({
            key: new FormControl(entry[0], [Validators.required]),
            value: new FormControl(entry[1], [Validators.required])
        }));
      };
    }
  }

  private initRemoveRequestHeaders(){
    const input = this.value ? this.value as RemoveRequestHeaders_RouteFilter : undefined;
    this.form.addControl("headersToRemove", new FormControl<string[]>(input?.headersToRemove || [], [NonEmptyArrayValidator()]));
  }

  public addHeaderToRemove(input: HTMLInputElement){
    let val = input.value.trim();
    if(val.length > 0 && !this.form.controls['headersToRemove'].value.includes(val)){
      this.form.controls['headersToRemove'].setValue([...this.form.controls['headersToRemove'].value, val]);
      input.value = '';
      this.onChange();
    }
  }

  getFormArrayWithGroups(name: string) {
    return this.form.get(name) as FormArray<FormGroup>;
  }

  addKVField(array: FormArray<FormGroup>){
    array.push(
      this.fb.group({
          key: new FormControl("", [Validators.required]),
          value: new FormControl("", [Validators.required])
      })
    )
    this.onChange();
  }

  removeKVField(array: FormArray, index: number) {
    array.removeAt(index);
    this.onChange();
  }

  onChange(){
    let output: RouteFilter | undefined;
    if(this.form.invalid){
      output = {type: this.type};
      this.control?.setErrors({filter: "Invalid"});
      console.log("invalid", this.type)
    }else{
      switch(this.type){
        case RouteFilterType.ZDZW_LICENSE_VERIFICATION:
          output = this.outputZDZWLicenseVerification();
          break;
        case RouteFilterType.ZDZW_SMARTCONTRACT_VERIFICATION:
          output = this.outputZDZWSmartcontractVerification();
          break;
        case RouteFilterType.EXTERNAL_CALL:
          output = this.outputExternalCall();
          break;
        case RouteFilterType.SET_HEADERS:
          output = this.outputSetHeaders();
          break;
        case RouteFilterType.OAUTH2_CLIENT:
          output = this.outputOAuth2Client();
          break;
        case RouteFilterType.REMOVE_REQUEST_HEADERS:
          output = this.outputRemoveRequestHeaders();
      }
      this.control?.setValue(output!);
      this.control?.setErrors(null);
      this.valueChange.emit(output);
    }
  }

  getFormValue(name: string){
    let control = this.form.get(name);
    if(control == null){
      return undefined;
    }else{
      if(control.value instanceof String || typeof control.value === "string"){
        if(control.value.length == 0 || control.value.trim().length == 0){
          return undefined;
        }else{
          return control.value;
        }
      }else if(control.value instanceof Array && control.value.length == 0){
        return undefined;
      }else{
        return control.value;
      }
    }
  }

  getFormValueAsMap(name: string, multi: boolean): {[key: string]: any} | undefined{
    let map: Map<string, any>;
    if(multi){
      map = new Map<string, string | string[]>();
    }else{
      map = new Map<string, string>();
    }
    if(this.getFormArrayWithGroups(name).controls.length == 0){
      return undefined;
    }
    for(let group of this.getFormArrayWithGroups(name).controls.values()){
      let key = group.controls["key"].value
      if(multi && map.has(key)){
        if(map.get(key) instanceof Array){
          map.get(key).push(group.controls["value"].value);
        }else{
          map.set(key, [map.get(key), group.controls["value"].value]);
        }
      }else{
        map.set(key, group.controls["value"].value);
      }
    }
    return Object.fromEntries(map);
  }

  private outputZDZWLicenseVerification(): ZDZWLicenseVerification_RouteFilter {
    return {
      type: RouteFilterType.ZDZW_LICENSE_VERIFICATION,
      appId: this.getFormValue("appId")
    }
  }

  private outputZDZWSmartcontractVerification(): ZDZWSmartContractVerification_RouteFilter {
    return {
      type: RouteFilterType.ZDZW_SMARTCONTRACT_VERIFICATION
    }
  }

  private outputExternalCall(): ExternalCall_RouteFilter {
    return {
      type: RouteFilterType.EXTERNAL_CALL,
      method: this.getFormValue("method"),
      uri: this.getFormValue("uri"),
      body: this.getFormValue("body"),
      headers: this.getFormValueAsMap("headers", true),
      responseMapping: this.getFormValueAsMap("responseMapping", false)
    }
  }

  private outputOAuth2Client(): OAuth2Client_RouteFilter {
    return {
      type: RouteFilterType.OAUTH2_CLIENT,
      clientId: this.getFormValue("clientId"),
      clientSecret: this.getFormValue("clientSecret"),
      issuerUrl: this.getFormValue("issuerUrl")
    }
  }

  private outputSetHeaders(): SetHeaders_RouteFilter {
    return {
      type: RouteFilterType.SET_HEADERS,
      headers: this.getFormValueAsMap("headers", false)!,
    }
  }

  private outputRemoveRequestHeaders(): RemoveRequestHeaders_RouteFilter {
    return {
      type: RouteFilterType.REMOVE_REQUEST_HEADERS,
      headersToRemove: this.form.get("headersToRemove")?.value || [],
    }
  }

}
