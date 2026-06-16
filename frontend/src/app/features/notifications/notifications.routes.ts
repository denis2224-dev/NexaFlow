import { Routes } from '@angular/router';

const notificationRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./notifications.component'),
    title: 'Notifications',
  },
];

export default notificationRoutes;
