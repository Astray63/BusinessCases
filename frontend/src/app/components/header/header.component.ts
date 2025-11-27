import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserContextService } from '../../services/user-context.service';
import { Utilisateur } from '../../models/utilisateur.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html'
})
export class HeaderComponent implements OnInit {
  currentUser: Utilisateur | null = null;
  isMenuOpen = false;
  isDropdownOpen = false;
  isProprietaire = false;
  nombreBornes = 0;

  // Sous-menus desktop
  isClientMenuOpen = false;
  isProprietaireMenuOpen = false;

  constructor(
    private authService: AuthService,
    private userContextService: UserContextService,
    public router: Router
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    // Écouter le statut propriétaire
    this.userContextService.isProprietaire$.subscribe(isProprietaire => {
      this.isProprietaire = isProprietaire;
    });

    // Écouter le nombre de bornes
    this.userContextService.nombreBornes$.subscribe(nombreBornes => {
      this.nombreBornes = nombreBornes;
    });
  }

  toggleMenu(event: Event): void {
    event.stopPropagation();
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu(): void {
    this.isMenuOpen = false;
  }

  toggleDropdown(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
    this.isMenuOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    // Fermer les dropdowns si on clique en dehors
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown') && !target.closest('.submenu-trigger')) {
      this.isDropdownOpen = false;
      this.isClientMenuOpen = false;
      this.isProprietaireMenuOpen = false;
    }
  }

  toggleClientMenu(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.isClientMenuOpen = !this.isClientMenuOpen;
    this.isProprietaireMenuOpen = false;
  }

  toggleProprietaireMenu(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.isProprietaireMenuOpen = !this.isProprietaireMenuOpen;
    this.isClientMenuOpen = false;
  }

  closeAllMenus(): void {
    this.isClientMenuOpen = false;
    this.isProprietaireMenuOpen = false;
    this.isDropdownOpen = false;
    this.isMenuOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
    this.closeAllMenus();
  }
}