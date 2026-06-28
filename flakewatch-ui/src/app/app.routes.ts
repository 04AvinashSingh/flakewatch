import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { IntegrateComponent } from './integrate.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'integrate', component: IntegrateComponent }
];
