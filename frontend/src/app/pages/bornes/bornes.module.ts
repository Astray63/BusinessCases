import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { BornesComponent } from './bornes.component';

const routes: Routes = [
  {
    path: '',
    component: BornesComponent
  }
];

@NgModule({
  declarations: [
    BornesComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class BornesModule { } 