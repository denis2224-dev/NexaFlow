import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DrawerModule } from 'primeng/drawer';
import { TagModule } from 'primeng/tag';
import { finalize } from 'rxjs';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import StatePanel from 'app/shared/ui/state-panel/state-panel';
import EditTaskDialogComponent from './edit-task-dialog.component';
import { Comment, Task, TaskPriority, TaskStatus } from './task.model';
import { TaskService } from './task.service';

@Component({
  selector: 'jhi-task-detail-drawer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-detail-drawer.component.html',
  styleUrl: './task-detail-drawer.component.scss',
  imports: [DatePipe, ReactiveFormsModule, ButtonModule, DrawerModule, TagModule, StatePanel, EditTaskDialogComponent],
})
export default class TaskDetailDrawerComponent {
  readonly visible = input(false);
  readonly task = input<Task | null>(null);
  readonly projectArchived = input(false);
  readonly closed = output();
  readonly taskUpdated = output<Task>();
  readonly taskDeleted = output<Task>();

  readonly currentTask = signal<Task | null>(null);
  readonly comments = signal<Comment[]>([]);
  readonly isCommentsLoading = signal(false);
  readonly isCommentSaving = signal(false);
  readonly commentsErrorMessage = signal<string | null>(null);
  readonly updatingStatus = signal(false);
  readonly editTaskDialogOpen = signal(false);
  readonly taskStatuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'];

  readonly assignForm = new FormGroup({
    assignedUserLogin: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
  });

  readonly commentForm = new FormGroup({
    content: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(2000)] }),
  });

  private readonly taskService = inject(TaskService);
  private readonly messageService = inject(MessageService);

  constructor() {
    effect(() => {
      const task = this.task();
      if (this.visible() && task) {
        this.currentTask.set(task);
        this.assignForm.reset({ assignedUserLogin: task.assignedUserLogin ?? '' });
        this.commentForm.reset({ content: '' });
        this.loadComments(task);
      }
    });
  }

  close(): void {
    this.closed.emit();
  }

  onVisibleChange(visible: boolean): void {
    if (!visible) {
      this.close();
    }
  }

  openEditTaskDialog(): void {
    if (!this.projectArchived()) {
      this.editTaskDialogOpen.set(true);
    }
  }

  closeEditTaskDialog(): void {
    this.editTaskDialogOpen.set(false);
  }

  onTaskEdited(updatedTask: Task): void {
    this.updateCurrentTask(updatedTask);
    this.taskUpdated.emit(updatedTask);
  }

  changeStatus(status: TaskStatus): void {
    const task = this.currentTask();
    if (task?.id == null || task.status === status || this.projectArchived()) {
      return;
    }

    this.updatingStatus.set(true);
    this.taskService
      .changeStatus(task.id, { status })
      .pipe(finalize(() => this.updatingStatus.set(false)))
      .subscribe({
        next: updatedTask => {
          this.updateCurrentTask(updatedTask);
          this.taskUpdated.emit(updatedTask);
          this.messageService.add({
            severity: 'success',
            summary: 'Status updated',
            detail: `${updatedTask.title ?? 'Task'} moved to ${this.getStatusLabel(updatedTask.status)}.`,
          });
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Status not changed',
            detail: extractNexaFlowErrorMessage(error, 'Task status could not be changed.'),
          });
        },
      });
  }

  assignTask(): void {
    const task = this.currentTask();
    if (task?.id == null || this.assignForm.invalid || this.projectArchived()) {
      this.assignForm.markAllAsTouched();
      return;
    }

    const assignedUserLogin = this.assignForm.controls.assignedUserLogin.value.trim();
    this.taskService.assign(task.id, { assignedUserLogin }).subscribe({
      next: updatedTask => {
        this.updateCurrentTask(updatedTask);
        this.taskUpdated.emit(updatedTask);
        this.messageService.add({ severity: 'success', summary: 'Task assigned', detail: `${updatedTask.title ?? 'Task'} assigned.` });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Task not assigned',
          detail: extractNexaFlowErrorMessage(error, 'Task could not be assigned.'),
        });
      },
    });
  }

  unassignTask(): void {
    const task = this.currentTask();
    if (task?.id == null || this.projectArchived() || !task.assignedUserLogin) {
      return;
    }

    this.taskService.unassign(task.id).subscribe({
      next: updatedTask => {
        this.updateCurrentTask(updatedTask);
        this.taskUpdated.emit(updatedTask);
        this.messageService.add({ severity: 'success', summary: 'Task unassigned', detail: updatedTask.title ?? 'Task unassigned.' });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Task not unassigned',
          detail: extractNexaFlowErrorMessage(error, 'Task could not be unassigned.'),
        });
      },
    });
  }

  deleteTask(): void {
    const task = this.currentTask();
    if (task?.id == null || this.projectArchived()) {
      return;
    }

    const confirmed = window.confirm(`Delete task "${task.title ?? task.id}"?`);
    if (!confirmed) {
      return;
    }

    this.taskService.delete(task.id).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Task deleted', detail: task.title ?? 'Task deleted.' });
        this.taskDeleted.emit(task);
        this.close();
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Task not deleted',
          detail: extractNexaFlowErrorMessage(error, 'Task could not be deleted.'),
        });
      },
    });
  }

  loadComments(task = this.currentTask()): void {
    if (task?.id == null) {
      return;
    }

    this.isCommentsLoading.set(true);
    this.commentsErrorMessage.set(null);
    this.taskService
      .getComments(task.id)
      .pipe(finalize(() => this.isCommentsLoading.set(false)))
      .subscribe({
        next: response => this.comments.set(response.body ?? []),
        error: error => {
          this.comments.set([]);
          this.commentsErrorMessage.set(extractNexaFlowErrorMessage(error, 'Comments could not be loaded.'));
        },
      });
  }

  addComment(): void {
    const task = this.currentTask();
    if (task?.id == null || this.commentForm.invalid || this.projectArchived()) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const content = this.commentForm.controls.content.value.trim();
    this.isCommentSaving.set(true);
    this.taskService
      .addComment(task.id, { content })
      .pipe(finalize(() => this.isCommentSaving.set(false)))
      .subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Comment added', detail: 'Comment was added to the task.' });
          this.commentForm.reset({ content: '' });
          this.loadComments(task);
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Comment not added',
            detail: extractNexaFlowErrorMessage(error, 'Comment could not be added.'),
          });
        },
      });
  }

  getStatusSeverity(status?: TaskStatus): 'success' | 'info' | 'secondary' | 'warn' | 'danger' | 'contrast' | undefined {
    switch (status) {
      case 'DONE':
        return 'success';
      case 'IN_PROGRESS':
        return 'info';
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

  getStatusLabel(status?: TaskStatus): string {
    switch (status) {
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

  private updateCurrentTask(updatedTask: Task): void {
    this.currentTask.set(updatedTask);
    this.assignForm.reset({ assignedUserLogin: updatedTask.assignedUserLogin ?? '' });
  }
}
