import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StatusRoutingModule } from './status-routing.module';
import { StatusComponent } from './status.component';
import { HighchartsChartModule } from 'highcharts-angular';
import { MaterialModule } from '../material.module';

@NgModule({
  declarations: [StatusComponent],
  imports: [
    CommonModule,
    StatusRoutingModule,
    MaterialModule,
    HighchartsChartModule,
  ],
})
export class StatusModule {}
