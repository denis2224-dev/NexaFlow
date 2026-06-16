import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Workspace, WorkspaceCreateRequest } from './nexaflow.model';

@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/workspaces', 'user-service');

  getMyWorkspaces(): Observable<Workspace[]> {
    return this.http.get<Workspace[]>(`${this.resourceUrl}/my`);
  }

  create(request: WorkspaceCreateRequest): Observable<Workspace> {
    return this.http.post<Workspace>(this.resourceUrl, request);
  }
}
