import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { LieuxComponent } from './lieux.component';
import { LieuFormComponent } from './lieu-form/lieu-form.component';

const routes: Routes = [
  { path: '', component: LieuxComponent },
  { path: 'ajouter', component: LieuFormComponent },
  { path: ':id/modifier', component: LieuFormComponent }
];

@NgModule({
  declarations: [
    LieuxComponent,
    LieuFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class LieuxModule { }
