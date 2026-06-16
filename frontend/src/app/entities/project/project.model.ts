export type ProjectStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export interface Project {
  id?: number;
  organizationId?: number;
  name?: string;
  description?: string | null;
  status?: ProjectStatus;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface CreateProjectRequest {
  name: string;
  description?: string | null;
  status: ProjectStatus;
}

export interface UpdateProjectRequest {
  name: string;
  description?: string | null;
  status: ProjectStatus;
}
