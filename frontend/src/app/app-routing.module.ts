import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

const routes: Routes = [
  // Default route
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  
  // Public routes
  { path: 'home', loadChildren: () => import('./pages/home/home.module').then(m => m.HomeModule) },
  { path: 'auth', loadChildren: () => import('./pages/auth/auth.module').then(m => m.AuthModule) },
  
  // Protected routes - Dashboard principal
  { 
    path: 'dashboard', 
    loadChildren: () => import('./pages/dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [AuthGuard]
  },
  
  // Protected routes - Profile
  { 
    path: 'profile', 
    loadChildren: () => import('./pages/profile/profile.module').then(m => m.ProfileModule),
    canActivate: [AuthGuard]
  },
  
  // 🔹 MODE CLIENT - Recherche et réservation de bornes
  { 
    path: 'client/recherche', 
    loadChildren: () => import('./pages/bornes/bornes.module').then(m => m.BornesModule),
    canActivate: [AuthGuard]
  },
  { 
    path: 'client/lieux', 
    loadChildren: () => import('./pages/lieux/lieux.module').then(m => m.LieuxModule),
    canActivate: [AuthGuard]
  },
  { 
    path: 'client/mes-reservations', 
    loadChildren: () => import('./pages/reservation/reservation.module').then(m => m.ReservationModule),
    canActivate: [AuthGuard]
  },
  { 
    path: 'client/reservation', 
    loadChildren: () => import('./pages/reservation/reservation.module').then(m => m.ReservationModule),
    canActivate: [AuthGuard]
  },
  
  // 🔹 MODE PROPRIÉTAIRE - Gestion de mes bornes
  { 
    path: 'mes-bornes', 
    loadChildren: () => import('./pages/proprietaire/proprietaire.module').then(m => m.ProprietaireModule),
    canActivate: [AuthGuard]
  },
  
  // 🔧 ADMIN - Administration
  { 
    path: 'admin', 
    loadChildren: () => import('./pages/admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard, AdminGuard]
  },
  
  // Legacy redirects pour compatibilité
  { path: 'bornes', redirectTo: 'client/recherche', pathMatch: 'full' },
  { path: 'lieux', redirectTo: 'client/lieux', pathMatch: 'full' },
  { path: 'reservations', redirectTo: 'client/mes-reservations', pathMatch: 'full' },
  { path: 'reservation', redirectTo: 'client/reservation', pathMatch: 'full' },
  { path: 'proprietaire', redirectTo: 'mes-bornes', pathMatch: 'full' },
  
  // Wildcard route for 404
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
