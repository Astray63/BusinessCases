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
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
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
    // Fermer le dropdown si on clique en dehors
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown')) {
      this.isDropdownOpen = false;
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
    this.isMenuOpen = false;
    this.isDropdownOpen = false;
  }
}