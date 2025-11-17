import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BornesComponent } from './bornes.component';
import { FilterPipe } from '../../shared/pipes/filter.pipe';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  { path: '', component: BornesComponent }
];

@NgModule({
  declarations: [
    BornesComponent,
    FilterPipe
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    SharedModule
  ]
})
export class BornesModule { } 