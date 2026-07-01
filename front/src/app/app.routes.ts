import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then((m) => m.MainLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
        canActivate: [guestGuard],
      },
      {
        path: 'register',
        loadComponent: () => import('./pages/register/register.component').then((m) => m.RegisterComponent),
        canActivate: [guestGuard],
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
        canActivate: [authGuard],
      },
      {
        path: 'themes',
        loadComponent: () => import('./pages/themes/themes.component').then((m) => m.ThemesComponent),
        canActivate: [authGuard],
      },
      {
        path: 'profile',
        loadComponent: () => import('./pages/profile/profile.component').then((m) => m.ProfileComponent),
        canActivate: [authGuard],
      },
      {
        path: 'profile/edit',
        loadComponent: () =>
          import('./pages/edit-profile/edit-profile.component').then((m) => m.EditProfileComponent),
        canActivate: [authGuard],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
