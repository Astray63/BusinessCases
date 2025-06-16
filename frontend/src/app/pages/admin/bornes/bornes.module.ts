import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../../shared/shared.module';
import { BornesAdminComponent } from '../bornes/bornes.component';
import { BorneFormModalComponent } from './borne-form-modal/borne-form-modal.component';

const routes: Routes = [
  {
    path: '',
    component: BornesAdminComponent
  }
];

@NgModule({
  declarations: [
    BorneFormModalComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes),
    BornesAdminComponent
  ]
})
export class BornesAdminModule { }