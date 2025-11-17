import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  // ============================================
  // 🏠 PUBLIC ROUTES
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
  // 🔐 PROTECTED ROUTES
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
  // 🔵 ESPACE CLIENT
  // ============================================
  { 
    path: 'client', 
    loadChildren: () => import('./pages/client/client.module').then(m => m.ClientModule),
    canActivate: [AuthGuard]
  },
  
  // ============================================
  // � ESPACE PROPRIÉTAIRE
  // ============================================
  {
    path: 'proprietaire', 
    loadChildren: () => import('./pages/proprietaire/proprietaire.module').then(m => m.ProprietaireModule),
    canActivate: [AuthGuard]
  },
  
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
