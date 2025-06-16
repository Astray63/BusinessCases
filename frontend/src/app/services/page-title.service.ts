import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PageTitleService {
  private titleSubject = new BehaviorSubject<string>('');
  private readonly baseTitle = 'E-Charge Admin';

  constructor(private titleService: Title) {}

  setTitle(title: string): void {
    const fullTitle = title ? `${title} | ${this.baseTitle}` : this.baseTitle;
    this.titleSubject.next(title);
    this.titleService.setTitle(fullTitle);
  }

  getTitle(): Observable<string> {
    return this.titleSubject.asObservable();
  }

  getCurrentTitle(): string {
    return this.titleSubject.value;
  }
}