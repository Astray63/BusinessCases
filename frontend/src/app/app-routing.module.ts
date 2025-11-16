import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { ProprietaireGuard } from './guards/proprietaire.guard';

const routes: Routes = [
  // ============================================
  // 🏠 DEFAULT & PUBLIC ROUTES
  // ============================================
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { 
    path: 'home', 
    loadChildren: () => import('./pages/home/home.module').then(m => m.HomeModule) 
  },
  { 
    path: 'auth', 
    loadChildren: () => import('./pages/auth/auth.module').then(m => m.AuthModule) 
  },
  
  // ============================================
  // 🔐 PROTECTED ROUTES - Général
  // ============================================
  { 
    path: 'dashboard', 
    loadChildren: () => import('./pages/dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [AuthGuard]
  },
  { 
    path: 'profile', 
    loadChildren: () => import('./pages/profile/profile.module').then(m => m.ProfileModule),
    canActivate: [AuthGuard]
  },
  
  // ============================================
  // 🔵 MODE CLIENT - Recherche et réservation de bornes
  // ============================================
  // Note: Toutes les routes client nécessitent juste l'authentification
  { 
    path: 'client', 
    loadChildren: () => import('./pages/client/client.module').then(m => m.ClientModule),
    canActivate: [AuthGuard]
  },
  
  // ============================================
  // 🟢 MODE PROPRIÉTAIRE - Gestion de mes bornes
  // ============================================
  // Note: Ces routes nécessitent d'être authentifié ET de posséder au moins 1 borne
  { 
    path: 'proprietaire', 
    loadChildren: () => import('./pages/proprietaire/proprietaire.module').then(m => m.ProprietaireModule),
    canActivate: [AuthGuard, ProprietaireGuard]
  },
  
  // Route spéciale pour devenir propriétaire (première borne)
  // Pas de ProprietaireGuard ici car l'utilisateur n'a pas encore de borne
  { 
    path: 'devenir-proprietaire',
    redirectTo: 'proprietaire/mes-bornes', // Temporaire, à créer un module dédié si besoin
    pathMatch: 'full'
  },
  
  // ============================================
  // 🔧 ADMIN - Administration
  // ============================================
  { 
    path: 'admin', 
    loadChildren: () => import('./pages/admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard, AdminGuard]
  },
  
  // ============================================
  // 🔄 LEGACY REDIRECTS - Pour compatibilité avec anciennes URLs
  // ============================================
  { path: 'bornes', redirectTo: 'client/recherche', pathMatch: 'full' },
  { path: 'lieux', redirectTo: 'client/lieux', pathMatch: 'full' },
  { path: 'reservations', redirectTo: 'client/mes-reservations', pathMatch: 'full' },
  { path: 'reservation', redirectTo: 'client/mes-reservations', pathMatch: 'full' },
  { path: 'mes-bornes', redirectTo: 'proprietaire/mes-bornes', pathMatch: 'full' },
  
  // ============================================
  // 🚫 WILDCARD - 404
  // ============================================
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
