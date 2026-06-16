import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Membership } from './nexaflow.model';

@Injectable({ providedIn: 'root' })
export class MembershipService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly workspaceResourceUrl = this.applicationConfigService.getEndpointFor('api/workspaces', 'user-service');

  getWorkspaceMembers(organizationId: number): Observable<Membership[]> {
    return this.http.get<Membership[]>(`${this.workspaceResourceUrl}/${organizationId}/members`);
  }
}
