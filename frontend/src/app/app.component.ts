import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { ToastComponent } from './components/toasts/toast.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-root',
  standalone: false,
  template: `
    <app-header></app-header>
    <main class="container py-4">
      <router-outlet></router-outlet>
    </main>
    <app-footer></app-footer>
    <app-toast></app-toast>
    <app-loading-spinner></app-loading-spinner>
  `,
  styles: [`
    main {
      min-height: calc(100vh - 120px);
      padding: 20px 0;
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'Electricity Business';
  
  constructor(private router: Router) {}
  
  ngOnInit(): void {
    // Scroll to top when navigating to a new page
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        window.scrollTo(0, 0);
      });
  }
}
