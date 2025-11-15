import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProprietaireGuard } from '../../guards/proprietaire.guard';

import { DashboardProprietaireComponent } from './dashboard-proprietaire/dashboard-proprietaire.component';
import { MesBornesComponent } from './mes-bornes/mes-bornes.component';
import { DemandesReservationComponent } from './demandes-reservation/demandes-reservation.component';
import { HistoriqueReservationsComponent } from './historique-reservations/historique-reservations.component';

const routes: Routes = [
  { 
    path: '', 
    component: DashboardProprietaireComponent,
    canActivate: [ProprietaireGuard]
  },
  { 
    path: 'bornes', 
    component: MesBornesComponent,
    canActivate: [ProprietaireGuard]
  },
  { 
    path: 'demandes', 
    component: DemandesReservationComponent,
    canActivate: [ProprietaireGuard]
  },
  { 
    path: 'reservations', 
    component: HistoriqueReservationsComponent,
    canActivate: [ProprietaireGuard]
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
