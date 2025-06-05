/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { Component } from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { AuthorizationService } from '../../services/auth.service';

interface MenuItem {
  label: string;
  icon?: string;
  routerLink: string;
}

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule,MatToolbarModule, MatButtonModule, RouterLink, RouterLinkActive, MatMenuModule, MatIconModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {

  constructor(private authService: AuthorizationService, private router: Router){}

  items: MenuItem[] = [
    {label: "Routes", routerLink: "/routes"},
    {label: "Global filters", routerLink: "/global-filters"},
    {label: "Settings", routerLink: "/settings"}
  ]

  logout(){
    this.authService.logout();
    this.router.navigateByUrl("/", {onSameUrlNavigation: 'reload'});
  }

}
