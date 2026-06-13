import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { DashboardSummary } from './dashboard-summary.model';

@Injectable({ providedIn: 'root' })
export class DashboardSummaryService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/dashboard/summary', 'project-service');

  getSummary(organizationId: number): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(this.resourceUrl, {
      headers: new HttpHeaders({ 'X-Organization-Id': `${organizationId}` }),
    });
  }
}
