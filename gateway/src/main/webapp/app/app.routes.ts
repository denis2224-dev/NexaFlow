import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/shared/jhipster/constants';

import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home'),
    title: 'home.title',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar'),
    outlet: 'navbar',
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'app',
    canActivate: [UserRouteAccessService],
    loadComponent: () => import('./layouts/app-shell/app-shell'),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard'),
        title: 'global.menu.app.dashboard',
      },
      {
        path: 'organizations',
        loadComponent: () => import('./features/organizations/organizations'),
        title: 'global.menu.app.organizations',
      },
      {
        path: 'billing',
        loadComponent: () => import('./features/billing/billing'),
        title: 'Billing & Subscription',
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile'),
        title: 'global.menu.app.profile',
      },
    ],
  },
  {
    path: 'projects',
    canActivate: [UserRouteAccessService],
    loadComponent: () => import('./layouts/app-shell/app-shell'),
    children: [
      {
        path: '',
        loadComponent: () => import('./entities/project/projects.component'),
        title: 'global.menu.app.projects',
      },
      {
        path: ':id',
        loadComponent: () => import('./entities/project/project-detail.component'),
        title: 'global.menu.app.projectDetails',
      },
    ],
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login'),
    title: 'login.title',
  },
  {
    path: '',
    loadChildren: () => import('./entities/entity.routes'),
  },
  ...errorRoute,
];

export default routes;
