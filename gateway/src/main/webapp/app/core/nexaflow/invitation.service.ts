import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Invitation, InvitationAcceptRequest, InvitationCreateRequest, Workspace } from './nexaflow.model';

@Injectable({ providedIn: 'root' })
export class InvitationService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  private readonly workspaceResourceUrl = this.applicationConfigService.getEndpointFor('api/workspaces', 'user-service');

  createForWorkspace(organizationId: number, request: InvitationCreateRequest): Observable<Invitation> {
    return this.http.post<Invitation>(`${this.workspaceResourceUrl}/${organizationId}/invitations`, request);
  }

  getForWorkspace(organizationId: number): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(`${this.workspaceResourceUrl}/${organizationId}/invitations`);
  }

  accept(request: InvitationAcceptRequest): Observable<Workspace> {
    return this.http.post<Workspace>(`${this.workspaceResourceUrl}/invitations/accept`, request);
  }

  revokeFromWorkspace(organizationId: number, invitationId: number): Observable<unknown> {
    return this.http.delete<unknown>(`${this.workspaceResourceUrl}/${organizationId}/invitations/${invitationId}`);
  }
}
