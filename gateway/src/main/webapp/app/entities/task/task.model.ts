import { Project } from 'app/entities/project/project.model';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'BLOCKED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id?: number;
  organizationId?: number;
  title?: string;
  description?: string | null;
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDate?: string | null;
  assignedUserLogin?: string | null;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string | null;
  project?: Project;
}

export interface CreateTaskRequest {
  projectId: number;
  title: string;
  description?: string | null;
  priority: TaskPriority;
  status: TaskStatus;
  assignedUserLogin?: string | null;
  dueDate?: string | null;
}

export interface UpdateTaskRequest {
  title: string;
  description?: string | null;
  priority: TaskPriority;
  dueDate?: string | null;
}

export interface ChangeTaskStatusRequest {
  status: TaskStatus;
}

export interface AssignTaskRequest {
  assignedUserLogin: string;
}

export interface Comment {
  id?: number;
  organizationId?: number;
  content?: string;
  authorLogin?: string;
  createdAt?: string;
  task?: Task;
}

export interface CreateCommentRequest {
  content: string;
}
