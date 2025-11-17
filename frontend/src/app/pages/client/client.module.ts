import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  // Redirection par défaut vers recherche
  { 
    path: '', 
    redirectTo: 'recherche',
    pathMatch: 'full'
  },
  
  // Recherche de bornes (carte + liste)
  { 
    path: 'recherche', 
    loadChildren: () => import('../bornes/bornes.module').then(m => m.BornesModule)
  },
  
  // Mes réservations (en tant que client)
  { 
    path: 'mes-reservations', 
    loadChildren: () => import('../reservation/reservation.module').then(m => m.ReservationModule)
  }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ClientModule { }
