import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SessionRoutingModule } from './session-routing.module';
import { SessionComponent } from './session.component';
import { MaterialModule } from '../material.module';

@NgModule({
  declarations: [SessionComponent],
  imports: [CommonModule, SessionRoutingModule, MaterialModule],
})
export class SessionModule {}
