/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
import {
    Directive,
    EmbeddedViewRef,
    Input,
    OnChanges,
    SimpleChanges,
    TemplateRef,
    ViewContainerRef
  } from '@angular/core';
  
  @Directive({
    selector: '[recreateViewKey]',
    standalone: true
  })
  export class RecreateViewDirective implements OnChanges {
    @Input('recreateViewKey') key: any;
  
    viewRef?: EmbeddedViewRef<any>;
  
    constructor(private templateRef: TemplateRef<any>, private viewContainer: ViewContainerRef) {}
  
    ngOnChanges(changes: SimpleChanges): void {
      if (changes['key']) {
        if (this.viewRef) {
          this.destroyView();
        }
  
        this.createView();
      }
    }
  
    private createView() {
      this.viewRef = this.viewContainer.createEmbeddedView(this.templateRef);
    }
  
    private destroyView() {
      this.viewRef!.destroy();
      this.viewRef = undefined;
    }
  }