import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { UserContextService } from '../services/user-context.service';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProprietaireGuard implements CanActivate {
  constructor(
    private userContextService: UserContextService,
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    // Vérifier d'abord que l'utilisateur est connecté
    return this.authService.currentUser$.pipe(
      take(1),
      map(user => {
        if (!user) {
          this.router.navigate(['/auth/login']);
          return false;
        }

        // Vérifier que l'utilisateur possède au moins une borne
        const isProprietaire = this.userContextService.isCurrentUserProprietaire();
        
        if (!isProprietaire) {
          alert('Vous devez posséder au moins une borne pour accéder à cette section.');
          this.router.navigate(['/dashboard']);
          return false;
        }

        return true;
      })
    );
  }
}
