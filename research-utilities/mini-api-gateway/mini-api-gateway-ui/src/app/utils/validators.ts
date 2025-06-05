/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { ValidatorFn, ValidationErrors, Validator, AbstractControl, FormGroup } from "@angular/forms";
import { RouteFilterType } from "../models/route-filter.interface";

const urlRegex = /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-]+|(?:www\.|[\-;:&=\+\$,\w]+@)[A-Za-z0-9\.\-]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w]*))?)/;
export const urlValidator: ValidatorFn = (control) => {
    if(control.value == null || control.value.length == 0 || urlRegex.test(control.value)){
      return null;
    }else{
      return {"url":true} as ValidationErrors;
    }
  }

export const RouteFilterTypeNameMapping: Map<RouteFilterType, string> = new Map([
    [RouteFilterType.EXTERNAL_CALL, "External Call"],
    [RouteFilterType.OAUTH2_CLIENT, "OAuth 2.0 Client Authorization"],
    [RouteFilterType.SET_HEADERS, "Set Headers"],
    [RouteFilterType.REMOVE_REQUEST_HEADERS, "Remove Incoming Headers"]
])

export const OneOfValidator = (...controls: string[]): ValidatorFn => {
  return (control: AbstractControl<any, any>): ValidationErrors | null => {
    if(control instanceof FormGroup){
      let groupControls = (control as FormGroup).controls;
      for(let controli of controls){
        if(groupControls[controli]){
          const value = groupControls[controli].value;
          if(groupControls[controli].valid && value != undefined && value != null && (!(value instanceof String || typeof value === "string") || (value as string).trim().length != 0)){
            return null;
          }
        }
      }
      return {"required": controls};
    }else{
      throw Error("OneOfValidaor must be used on FormGroup")
    }
  }
}

export const NonEmptyArrayValidator = (): ValidatorFn => {
  return (control: AbstractControl<any, any>): ValidationErrors | null => {
    console.log(control)
    if(control.value == null || (control.value as Array<any>).length < 1){
      return {"required":control};
    }else{
      return null;
    }
  }
}

//  class OneOfValidators implements Validator {

//   private controls: string[];

//   constructor(...controls: string[]){this.controls = controls}

//   validate(control: AbstractControl<any, any>): ValidationErrors | null {
//     if(control.parent instanceof FormGroup){
//       let group = control.parent as FormGroup;
//       for(let control of this.controls){
//         const value = group.controls[control].value;
//         if(group.controls[control].valid && value != undefined && value != null && (!(value instanceof String || typeof value === "string") || (value as string).trim().length != 0)){
//           return null;
//         }
//       }
//       return {"required": this.controls};
//     }else{
//       throw Error("OneOfValidator can only be used on controls in a FormGroup");
//     }
//   }
//   registerOnValidatorChange?(fn: () => void): void {
//     //Not needed
//   }