import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface Breadcrumb {
  label: string;
  link?: string;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          <a routerLink="/admin">
            <i class="bi bi-house-door"></i>
          </a>
        </li>
        <li *ngFor="let item of items; let last = last" 
            class="breadcrumb-item"
            [class.active]="last">
          <ng-container *ngIf="!last && item.link">
            <a [routerLink]="item.link">{{ item.label }}</a>
          </ng-container>
          <ng-container *ngIf="last || !item.link">
            {{ item.label }}
          </ng-container>
        </li>
      </ol>
    </nav>
  `,
  styles: [`
    .breadcrumb {
      background-color: #fff;
      border-radius: 0.25rem;
      padding: 0.75rem 1rem;
      margin-bottom: 1rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }

    .breadcrumb-item {
      + .breadcrumb-item::before {
        content: "›";
        color: #6c757d;
      }

      a {
        color: #007bff;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }

      &.active {
        color: #6c757d;
      }
    }
  `]
})
export class BreadcrumbComponent {
  @Input() items: Breadcrumb[] = [];
}