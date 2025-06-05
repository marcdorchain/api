/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { CUSTOM_ELEMENTS_SCHEMA, Component } from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { AuthorizationService } from '../../services/auth.service';
import { DynamicEnvironment } from '../../services/dynamic-environment';

interface MenuItem {
  label: string;
  icon?: string;
  routerLink: string;
}

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule,MatToolbarModule, MatButtonModule, RouterLink, RouterLinkActive, MatMenuModule, MatIconModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {

  constructor(private authService: AuthorizationService, private router: Router, public dynEnvironment: DynamicEnvironment){}

  items: MenuItem[] = [
    {label: "Routes", routerLink: "/routes"},
    {label: "Global filters", routerLink: "/global-filters"},
    {label: "Settings", routerLink: "/settings"}
  ]

  logout(){
    this.authService.logout();
    this.router.navigateByUrl("/", {onSameUrlNavigation: 'reload'});
  }

  isAuthorizedByToken(){
    return this.authService.isAuthorized() && Object.hasOwn(this.authService.getCredentials()!, "token");
  }

}
