import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoginComponent } from './login/login.component';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { ApiResponse } from '../../models/api-response.model';
import { AuthResponse, Utilisateur } from '../../models/utilisateur.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let toastService: jasmine.SpyObj<ToastService>;
  let router: Router;

  const mockUser: Utilisateur = {
    idUtilisateur: 1,
    pseudo: 'testuser',
    nom: 'Test',
    prenom: 'User',
    email: 'test@example.com',
    role: 'client',
    actif: true,
    dateCreation: new Date(),
    dateModification: new Date()
  };

  const mockLoginResponse: ApiResponse<AuthResponse> = {
    result: 'SUCCESS',
    message: 'Connexion réussie',
    data: {
      token: 'fake-token',
      user: mockUser
    }
  };

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']);

    await TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ 
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy }
      ]
    })
    .compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
    router = TestBed.inject(Router);

    // Configure default response
    authService.login.and.returnValue(of(mockLoginResponse));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('login', () => {
    it('should call login service and navigate on success', () => {
      const navigateSpy = spyOn(router, 'navigate');
      const loginCredentials = {
        pseudo: 'testuser',
        password: 'password123'
      };
      
      component.loginForm.setValue(loginCredentials);
      component.onSubmit();

      expect(authService.login).toHaveBeenCalledWith(loginCredentials);
      expect(toastService.showSuccess).toHaveBeenCalledWith('Connexion réussie');
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });

    it('should show error toast on login failure', () => {
      const errorResponse = {
        result: 'ERROR',
        message: 'Invalid credentials',
        data: undefined
      } as ApiResponse<AuthResponse>;

      authService.login.and.returnValue(throwError(() => errorResponse));

      component.loginForm.setValue({
        pseudo: 'testuser',
        password: 'wrongpass'
      });

      component.onSubmit();

      expect(toastService.showError).toHaveBeenCalledWith('Invalid credentials');
    });
  });
});
