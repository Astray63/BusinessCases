import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Utilisateur } from '../models/utilisateur.model';
import { BorneService } from './borne.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserContextService {
  private isProprietaireSubject = new BehaviorSubject<boolean>(false);
  private nombreBornesSubject = new BehaviorSubject<number>(0);

  public isProprietaire$ = this.isProprietaireSubject.asObservable();
  public nombreBornes$ = this.nombreBornesSubject.asObservable();

  constructor(
    private borneService: BorneService,
    private authService: AuthService
  ) {
    // Écouter les changements d'utilisateur
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.checkProprietaireStatus(user);
      } else {
        this.resetStatus();
      }
    });
  }

  /**
   * Vérifie si l'utilisateur possède des bornes
   */
  private checkProprietaireStatus(user: Utilisateur): void {
    this.borneService.getBornesByProprietaire(user.idUtilisateur).subscribe({
      next: (response: any) => {
        if (response.result === 'SUCCESS' && response.data) {
          const bornes = response.data;
          const nombreBornes = bornes.length;
          
          this.nombreBornesSubject.next(nombreBornes);
          this.isProprietaireSubject.next(nombreBornes > 0);
          
          // Mettre à jour l'utilisateur dans le AuthService
          if (user) {
            user.isProprietaire = nombreBornes > 0;
            user.nombreBornes = nombreBornes;
          }
        } else {
          this.resetStatus();
        }
      },
      error: () => {
        this.resetStatus();
      }
    });
  }

  /**
   * Réinitialise le statut propriétaire
   */
  private resetStatus(): void {
    this.isProprietaireSubject.next(false);
    this.nombreBornesSubject.next(0);
  }

  /**
   * Force une vérification du statut propriétaire
   */
  public refreshProprietaireStatus(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.checkProprietaireStatus(user);
      }
    }).unsubscribe();
  }

  /**
   * Retourne le statut actuel (synchrone)
   */
  public isCurrentUserProprietaire(): boolean {
    return this.isProprietaireSubject.value;
  }

  /**
   * Retourne le nombre de bornes actuel (synchrone)
   */
  public getCurrentNombreBornes(): number {
    return this.nombreBornesSubject.value;
  }
}
