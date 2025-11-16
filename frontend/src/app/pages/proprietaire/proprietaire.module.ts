import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { DashboardProprietaireComponent } from './dashboard-proprietaire/dashboard-proprietaire.component';
import { MesBornesComponent } from './mes-bornes/mes-bornes.component';
import { DemandesReservationComponent } from './demandes-reservation/demandes-reservation.component';
import { HistoriqueReservationsComponent } from './historique-reservations/historique-reservations.component';

const routes: Routes = [
  // Dashboard propriétaire (vue d'ensemble)
  { 
    path: '', 
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  { 
    path: 'dashboard', 
    component: DashboardProprietaireComponent
  },
  
  // Gestion des bornes
  { 
    path: 'mes-bornes', 
    component: MesBornesComponent
  },
  
  // Gestion des demandes de réservation
  { 
    path: 'demandes', 
    component: DemandesReservationComponent
  },
  
  // Historique des réservations sur mes bornes
  { 
    path: 'historique', 
    component: HistoriqueReservationsComponent
  }
];

@NgModule({
  declarations: [
    DashboardProprietaireComponent,
    MesBornesComponent,
    DemandesReservationComponent,
    HistoriqueReservationsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class ProprietaireModule { }
