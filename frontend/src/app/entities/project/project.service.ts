import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { Task } from 'app/entities/task/task.model';
import { CreateProjectRequest, Project, UpdateProjectRequest } from './project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/projects', 'project-service');

  query(organizationId: number, page = 0, size = 20, sort = 'id,desc'): Observable<HttpResponse<Project[]>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Project[]>(this.resourceUrl, {
      params,
      headers: this.organizationHeaders(organizationId),
      observe: 'response',
    });
  }

  create(organizationId: number, request: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.resourceUrl, request, { headers: this.organizationHeaders(organizationId) });
  }

  find(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.resourceUrl}/${id}`, { headers: this.organizationHeaders() });
  }

  update(id: number, request: UpdateProjectRequest): Observable<Project> {
    return this.http.put<Project>(`${this.resourceUrl}/${id}`, request, { headers: this.organizationHeaders() });
  }

  archive(id: number): Observable<Project> {
    return this.http.patch<Project>(`${this.resourceUrl}/${id}/archive`, {}, { headers: this.organizationHeaders() });
  }

  unarchive(id: number): Observable<Project> {
    return this.http.patch<Project>(`${this.resourceUrl}/${id}/unarchive`, {}, { headers: this.organizationHeaders() });
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { headers: this.organizationHeaders() });
  }

  findTasks(projectId: number, page = 0, size = 50, sort = 'id,desc'): Observable<HttpResponse<Task[]>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Task[]>(`${this.resourceUrl}/${projectId}/tasks`, {
      params,
      headers: this.organizationHeaders(),
      observe: 'response',
    });
  }

  private organizationHeaders(organizationId = this.activeOrganizationService.selectedOrganizationId()): HttpHeaders {
    return organizationId == null ? new HttpHeaders() : new HttpHeaders({ 'X-Organization-Id': `${organizationId}` });
  }
}
