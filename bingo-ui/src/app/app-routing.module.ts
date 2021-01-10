import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { StartComponent } from './component/start/start.component';

const routes: Routes = [
  {
    path: '',
    component: StartComponent,
  },
  {
    path: 'status',
    loadChildren: () =>
      import('./status/status.module').then((m) => m.StatusModule),
  },
  {
    path: 'session',
    loadChildren: () =>
      import('./session/session.module').then((m) => m.SessionModule),
  },
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
