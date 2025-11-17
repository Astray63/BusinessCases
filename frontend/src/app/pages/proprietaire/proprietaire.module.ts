import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { MesBornesComponent } from './mes-bornes/mes-bornes.component';
import { DemandesReservationComponent } from './demandes-reservation/demandes-reservation.component';
import { HistoriqueReservationsComponent } from './historique-reservations/historique-reservations.component';

const routes: Routes = [
  // Redirection vers le dashboard principal
  { 
    path: '', 
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  { 
    path: 'dashboard', 
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  
  // Gestion des lieux
  { 
    path: 'mes-lieux', 
    loadChildren: () => import('../lieux/lieux.module').then(m => m.LieuxModule)
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
