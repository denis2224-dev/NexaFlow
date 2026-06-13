import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, effect, inject, signal, untracked } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import type { TableLazyLoadEvent } from 'primeng/types/table';
import { finalize } from 'rxjs';

import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import CreateTaskDialogComponent from 'app/entities/task/create-task-dialog.component';
import TaskDetailDrawerComponent from 'app/entities/task/task-detail-drawer.component';
import { Task, TaskPriority, TaskStatus } from 'app/entities/task/task.model';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';
import EditProjectDialogComponent from './edit-project-dialog.component';
import { Project, ProjectStatus } from './project.model';
import { ProjectService } from './project.service';

@Component({
  selector: 'jhi-project-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './project-detail.component.html',
  styleUrl: './project-detail.component.scss',
  imports: [
    RouterLink,
    DatePipe,
    ButtonModule,
    TagModule,
    TableModule,
    ToastModule,
    PageHeader,
    SectionPanel,
    StatePanel,
    CreateTaskDialogComponent,
    TaskDetailDrawerComponent,
    EditProjectDialogComponent,
  ],
})
export default class ProjectDetailComponent implements OnInit {
  readonly project = signal<Project | null>(null);
  readonly tasks = signal<Task[]>([]);
  readonly isProjectLoading = signal(true);
  readonly isTasksLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly tasksErrorMessage = signal<string | null>(null);
  readonly taskDialogOpen = signal(false);
  readonly editProjectDialogOpen = signal(false);
  readonly taskDetailDrawerOpen = signal(false);
  readonly selectedTask = signal<Task | null>(null);
  readonly organizationId = signal<number | null>(null);
  readonly workspaceName = signal<string | null>(null);
  readonly projectId = signal<number | null>(null);
  readonly totalTasks = signal(0);
  readonly rows = signal(20);
  readonly first = signal(0);

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly projectService = inject(ProjectService);
  private readonly messageService = inject(MessageService);
  private loadedOrganizationId: number | null = null;

  constructor() {
    effect(() => {
      const projectId = this.projectId();
      const activeOrganization = this.activeOrganizationService.activeOrganization();
      const isOrganizationLoading = this.activeOrganizationService.isLoading();
      const organizationErrorMessage = this.activeOrganizationService.errorMessage();

      if (projectId == null) {
        return;
      }

      if (isOrganizationLoading) {
        this.clearProjectData();
        this.isProjectLoading.set(true);
        this.errorMessage.set(null);
        return;
      }

      if (!activeOrganization) {
        this.loadedOrganizationId = null;
        this.organizationId.set(null);
        this.workspaceName.set(null);
        this.clearProjectData();
        this.isProjectLoading.set(false);
        this.errorMessage.set(organizationErrorMessage ?? 'No workspace selected. Create or join a workspace to manage projects.');
        return;
      }

      if (this.loadedOrganizationId !== null && this.loadedOrganizationId !== activeOrganization.organizationId) {
        this.closeTaskDialog();
        this.closeEditProjectDialog();
        this.closeTaskDetails();
        this.router.navigate(['/projects']);
        return;
      }

      if (this.loadedOrganizationId === activeOrganization.organizationId) {
        return;
      }

      this.loadedOrganizationId = activeOrganization.organizationId;
      this.organizationId.set(activeOrganization.organizationId);
      this.workspaceName.set(activeOrganization.workspace.name ?? activeOrganization.workspace.slug ?? 'Current workspace');
      this.first.set(0);
      this.clearProjectData();
      untracked(() => {
        this.loadProject();
        this.loadTasks();
      });
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.errorMessage.set('Invalid project id.');
      this.isProjectLoading.set(false);
      return;
    }

    this.projectId.set(id);
    this.activeOrganizationService.loadOrganizations();
  }

  loadProject(): void {
    const projectId = this.projectId();
    if (projectId == null) {
      return;
    }

    this.isProjectLoading.set(true);
    this.errorMessage.set(null);
    this.projectService
      .find(projectId)
      .pipe(finalize(() => this.isProjectLoading.set(false)))
      .subscribe({
        next: project => {
          if (project.organizationId != null && this.organizationId() != null && project.organizationId !== this.organizationId()) {
            this.clearProjectData();
            this.errorMessage.set('This project is not available in the selected workspace.');
            return;
          }

          this.project.set(project);
        },
        error: error => {
          this.project.set(null);
          this.errorMessage.set(extractNexaFlowErrorMessage(error, 'This project is not available in the selected workspace.'));
        },
      });
  }

  loadTasks(event?: TableLazyLoadEvent): void {
    const projectId = this.projectId();
    if (projectId == null || this.organizationId() == null) {
      return;
    }

    const rows = event?.rows ?? this.rows();
    const first = event?.first ?? this.first();
    const page = Math.floor(first / rows);
    const sortFieldValue = event?.sortField;
    const sortField = Array.isArray(sortFieldValue) ? sortFieldValue[0] : sortFieldValue;
    const sortDirection = event?.sortOrder === 1 ? 'asc' : 'desc';
    const sort = `${sortField ?? 'id'},${sortDirection}`;

    this.rows.set(rows);
    this.first.set(first);
    this.isTasksLoading.set(true);
    this.tasksErrorMessage.set(null);

    this.projectService
      .findTasks(projectId, page, rows, sort)
      .pipe(finalize(() => this.isTasksLoading.set(false)))
      .subscribe({
        next: response => {
          this.tasks.set(response.body ?? []);
          this.totalTasks.set(Number(response.headers.get('X-Total-Count') ?? response.body?.length ?? 0));
        },
        error: error => {
          this.tasks.set([]);
          this.tasksErrorMessage.set(extractNexaFlowErrorMessage(error, 'Tasks could not be loaded.'));
        },
      });
  }

  openTaskDialog(): void {
    this.taskDialogOpen.set(true);
  }

  closeTaskDialog(): void {
    this.taskDialogOpen.set(false);
  }

  onTaskCreated(): void {
    this.loadTasks();
  }

  openEditProjectDialog(): void {
    this.editProjectDialogOpen.set(true);
  }

  closeEditProjectDialog(): void {
    this.editProjectDialogOpen.set(false);
  }

  onProjectUpdated(project: Project): void {
    this.project.set(project);
    this.loadProject();
  }

  openTaskDetails(task: Task): void {
    this.selectedTask.set(task);
    this.taskDetailDrawerOpen.set(true);
  }

  closeTaskDetails(): void {
    this.taskDetailDrawerOpen.set(false);
    this.selectedTask.set(null);
  }

  onDrawerTaskUpdated(updatedTask: Task): void {
    this.selectedTask.set(updatedTask);
    this.tasks.update(tasks => tasks.map(task => (task.id === updatedTask.id ? updatedTask : task)));
    this.loadTasks();
  }

  onDrawerTaskDeleted(): void {
    this.closeTaskDetails();
    this.loadTasks();
  }

  unarchiveProject(): void {
    const project = this.project();
    if (project?.id == null || project.status !== 'ARCHIVED') {
      return;
    }

    this.projectService.unarchive(project.id).subscribe({
      next: restoredProject => {
        this.project.set(restoredProject);
        this.messageService.add({
          severity: 'success',
          summary: 'Project restored',
          detail: restoredProject.name ?? 'Project restored.',
        });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Project not restored',
          detail: extractNexaFlowErrorMessage(error, 'Project could not be restored.'),
        });
      },
    });
  }

  deleteProject(): void {
    const project = this.project();
    if (project?.id == null) {
      return;
    }

    const confirmed = window.confirm(`Delete project "${project.name ?? project.id}"? This also deletes its tasks and comments.`);
    if (!confirmed) {
      return;
    }

    this.projectService.delete(project.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Project deleted',
          detail: project.name ?? 'Project deleted.',
        });
        this.router.navigate(['/projects']);
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Project not deleted',
          detail: extractNexaFlowErrorMessage(error, 'Project could not be deleted.'),
        });
      },
    });
  }

  getProjectId(): number | null {
    return this.projectId();
  }

  getStatusSeverity(status?: ProjectStatus | TaskStatus): 'success' | 'info' | 'secondary' | 'warn' | 'danger' | 'contrast' | undefined {
    switch (status) {
      case 'ACTIVE':
      case 'DONE':
        return 'success';
      case 'COMPLETED':
      case 'IN_PROGRESS':
        return 'info';
      case 'ARCHIVED':
      case 'TODO':
        return 'secondary';
      case 'BLOCKED':
        return 'danger';
      default:
        return undefined;
    }
  }

  getPrioritySeverity(priority?: TaskPriority): 'success' | 'info' | 'secondary' | 'warn' | 'danger' | 'contrast' | undefined {
    switch (priority) {
      case 'LOW':
        return 'secondary';
      case 'MEDIUM':
        return 'info';
      case 'HIGH':
        return 'warn';
      case 'URGENT':
        return 'danger';
      default:
        return undefined;
    }
  }

  getStatusLabel(status?: ProjectStatus | TaskStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'Active';
      case 'COMPLETED':
        return 'Completed';
      case 'ARCHIVED':
        return 'Archived';
      case 'TODO':
        return 'To do';
      case 'IN_PROGRESS':
        return 'In progress';
      case 'DONE':
        return 'Done';
      case 'BLOCKED':
        return 'Blocked';
      default:
        return 'Unknown';
    }
  }

  getTaskStatusLabel(status?: TaskStatus): string {
    return this.getStatusLabel(status);
  }

  getPriorityLabel(priority?: TaskPriority): string {
    switch (priority) {
      case 'LOW':
        return 'Low';
      case 'MEDIUM':
        return 'Medium';
      case 'HIGH':
        return 'High';
      case 'URGENT':
        return 'Urgent';
      default:
        return 'Unknown';
    }
  }

  trackTask(_index: number, task: Task): number | undefined {
    return task.id;
  }

  private clearProjectData(): void {
    this.project.set(null);
    this.tasks.set([]);
    this.totalTasks.set(0);
    this.tasksErrorMessage.set(null);
  }
}
