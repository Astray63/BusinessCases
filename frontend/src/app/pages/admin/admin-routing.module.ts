import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminComponent } from './admin.component';
import { AdminGuard } from '../../guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    canActivate: [AdminGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'bornes',
        loadComponent: () => import('./bornes/bornes.component').then(m => m.BornesAdminComponent)
      },
      {
        path: 'utilisateurs',
        loadChildren: () => import('./utilisateurs/utilisateurs.module').then(m => m.UtilisateursModule)
      },
      {
        path: 'reservations',
        loadComponent: () => import('./reservations/reservations.component').then(m => m.ReservationsAdminComponent)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}