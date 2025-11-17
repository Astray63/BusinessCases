import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastsComponent } from '../components/toasts/toasts.component';
import { BorneCardComponent } from '../components/borne-card/borne-card.component';

@NgModule({
  declarations: [
    BorneCardComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    NgbModule,
    ToastsComponent
  ],
  exports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    NgbModule,
    ToastsComponent,
    BorneCardComponent
  ]
})
export class SharedModule { }