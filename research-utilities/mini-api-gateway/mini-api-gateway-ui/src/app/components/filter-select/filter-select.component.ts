/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { RouteFilterType } from '../../models/route-filter.interface';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { RouteFilterTypeNameMapping } from '../../utils/validators';
import { CommonModule } from '@angular/common';

interface SelectNode {
  name: string;
  description?: string;
  value?: RouteFilterType;
  children?: SelectNode[];
}

@Component({
  selector: 'app-filter-select',
  standalone: true,
  imports: [MatTreeModule, MatIconModule, MatButtonModule, MatRippleModule, CommonModule],
  templateUrl: './filter-select.component.html',
  styleUrl: './filter-select.component.scss'
})
export class FilterSelectComponent {

  @Input() mode: 'select' | 'emit-only' = 'select';

  selected?: SelectNode;

  @Output() select = new EventEmitter<RouteFilterType>;

  @Input() set selection(selection: RouteFilterType | undefined){
    if(selection){
      this.preSelect(selection, this.dataSource.data);
    }
  }

  @Input() set expanded(expanded: boolean){
    if(expanded === true){
      this.treeControl.expandAll();
    }else{
      this.treeControl.collapseAll();
    }
  }

  private preSelect(selection: RouteFilterType, source: SelectNode[]){
    for (const node of source) {
      if(node.value == selection){
        this.selected = node;
        return;
      }else if(node.children){
        this.preSelect(selection, node.children)
      }
    }
  }

  treeControl = new NestedTreeControl<SelectNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<SelectNode>();

  constructor() {
    this.dataSource.data = [
      {name: "Pre-Request Action", children: [
        {name: RouteFilterTypeNameMapping.get(RouteFilterType.EXTERNAL_CALL)!, value: RouteFilterType.EXTERNAL_CALL},
      ]},
      {name: "Request Transformation", children: [
        {name: RouteFilterTypeNameMapping.get(RouteFilterType.SET_HEADERS)!, value: RouteFilterType.SET_HEADERS},
        {name: RouteFilterTypeNameMapping.get(RouteFilterType.OAUTH2_CLIENT)!, value: RouteFilterType.OAUTH2_CLIENT},
        {name: RouteFilterTypeNameMapping.get(RouteFilterType.REMOVE_REQUEST_HEADERS)!, value: RouteFilterType.REMOVE_REQUEST_HEADERS}
      ]}
    ];
    this.treeControl.dataNodes = this.dataSource.data;
  }

  hasChild = (_: number, node: SelectNode) => !!node.children && node.children.length > 0;

  selectNode(node: SelectNode){
    this.select.emit(node.value);
    if(this.mode == 'select'){
      this.selected = node;
    }
  }

}
