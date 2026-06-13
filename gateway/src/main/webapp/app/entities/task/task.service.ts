import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import {
  AssignTaskRequest,
  ChangeTaskStatusRequest,
  Comment,
  CreateCommentRequest,
  CreateTaskRequest,
  Task,
  UpdateTaskRequest,
} from './task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/tasks', 'project-service');

  create(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(this.resourceUrl, request, { headers: this.organizationHeaders() });
  }

  update(taskId: number, request: UpdateTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.resourceUrl}/${taskId}`, request, { headers: this.organizationHeaders() });
  }

  changeStatus(taskId: number, request: ChangeTaskStatusRequest): Observable<Task> {
    return this.http.patch<Task>(`${this.resourceUrl}/${taskId}/status`, request, { headers: this.organizationHeaders() });
  }

  assign(taskId: number, request: AssignTaskRequest): Observable<Task> {
    return this.http.patch<Task>(`${this.resourceUrl}/${taskId}/assign`, request, { headers: this.organizationHeaders() });
  }

  unassign(taskId: number): Observable<Task> {
    return this.http.patch<Task>(`${this.resourceUrl}/${taskId}/unassign`, {}, { headers: this.organizationHeaders() });
  }

  delete(taskId: number): Observable<unknown> {
    return this.http.delete(`${this.resourceUrl}/${taskId}`, { headers: this.organizationHeaders() });
  }

  addComment(taskId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.resourceUrl}/${taskId}/comments`, request, { headers: this.organizationHeaders() });
  }

  getComments(taskId: number, page = 0, size = 20, sort = 'id,desc'): Observable<HttpResponse<Comment[]>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Comment[]>(`${this.resourceUrl}/${taskId}/comments`, {
      params,
      headers: this.organizationHeaders(),
      observe: 'response',
    });
  }

  private organizationHeaders(): HttpHeaders {
    const organizationId = this.activeOrganizationService.selectedOrganizationId();
    return organizationId == null ? new HttpHeaders() : new HttpHeaders({ 'X-Organization-Id': `${organizationId}` });
  }
}
