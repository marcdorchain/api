/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { CUSTOM_ELEMENTS_SCHEMA, Component, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AuthorizationService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { MatDividerModule } from '@angular/material/divider';
import { DynamicEnvironment } from '../../services/dynamic-environment';

@Component({
  selector: 'app-login-view',
  standalone: true,
  imports: [MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, FormsModule, ReactiveFormsModule, MatIconModule, MatDividerModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './login-view.component.html',
  styleUrl: './login-view.component.scss'
})
export class LoginViewComponent implements OnDestroy{

  //private checkAuthInterval;

  constructor(private authService: AuthorizationService, private router: Router, public dynEnvironment: DynamicEnvironment){
    // this.checkAuthInterval = setInterval(
    //   () => {
    //     if(authService.isAuthorized()){
    //       this.router.navigateByUrl("/", {onSameUrlNavigation: 'reload'});
    //     }
    //   },
    //   500
    // )
  }

  ngOnDestroy(): void {
    //clearInterval(this.checkAuthInterval);
  }

  form = new FormGroup({
    username: new FormControl<string>('', Validators.required),
    password: new FormControl<string>('', Validators.required)
  })

  login(){
    if(this.form.valid){
      this.authService.login({
        username: this.form.controls.username.value!, 
        password: this.form.controls.password.value!
      }).subscribe({
        next: (result) => {
          if(result){
            this.router.navigateByUrl("/", {onSameUrlNavigation: 'reload'});
          }else{

          }
        }
      })
    }
  }

}
