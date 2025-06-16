import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { VehiculesComponent } from './vehicules.component';

const routes: Routes = [
  {
    path: '',
    component: VehiculesComponent
  }
];

@NgModule({
  declarations: [
    VehiculesComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class VehiculesModule { } 