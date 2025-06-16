import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../../shared/shared.module';
import { ReservationsAdminComponent } from './reservations.component';

const routes: Routes = [
  {
    path: '',
    component: ReservationsAdminComponent
  }
];

@NgModule({
  declarations: [],
  imports: [
    SharedModule,
    RouterModule.forChild(routes),
    ReservationsAdminComponent
  ]
})
export class ReservationsAdminModule { }