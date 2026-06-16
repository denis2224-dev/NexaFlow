import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { finalize } from 'rxjs';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { CreateTaskRequest, Task, TaskPriority, TaskStatus } from './task.model';
import { TaskService } from './task.service';

@Component({
  selector: 'jhi-create-task-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './create-task-dialog.component.html',
  styleUrl: './create-task-dialog.component.scss',
  imports: [ReactiveFormsModule, DialogModule, ButtonModule],
})
export default class CreateTaskDialogComponent {
  readonly visible = input(false);
  readonly projectId = input.required<number>();
  readonly closed = output();
  readonly created = output<Task>();
  readonly isSaving = signal(false);
  readonly priorityOptions: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
  readonly statusOptions: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'];

  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(180)] }),
    description: new FormControl<string | null>(null, { validators: [Validators.maxLength(2000)] }),
    priority: new FormControl<TaskPriority>('MEDIUM', { nonNullable: true, validators: [Validators.required] }),
    status: new FormControl<TaskStatus>('TODO', { nonNullable: true, validators: [Validators.required] }),
    assignedUserLogin: new FormControl<string | null>(null, { validators: [Validators.maxLength(100)] }),
    dueDate: new FormControl<string | null>(null),
  });

  private readonly taskService = inject(TaskService);
  private readonly messageService = inject(MessageService);

  close(): void {
    if (!this.isSaving()) {
      this.closed.emit();
    }
  }

  onVisibleChange(visible: boolean): void {
    if (!visible) {
      this.close();
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const assignedUserLogin = raw.assignedUserLogin?.trim();
    const request: CreateTaskRequest = {
      projectId: this.projectId(),
      title: raw.title,
      description: raw.description,
      priority: raw.priority,
      status: raw.status,
      assignedUserLogin: assignedUserLogin === '' ? null : (assignedUserLogin ?? null),
      dueDate: raw.dueDate === '' ? null : (raw.dueDate ?? null),
    };

    this.isSaving.set(true);
    this.taskService
      .create(request)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: task => {
          this.messageService.add({ severity: 'success', summary: 'Task created', detail: task.title ?? 'Task was created.' });
          this.form.reset({ title: '', description: null, priority: 'MEDIUM', status: 'TODO', assignedUserLogin: null, dueDate: null });
          this.created.emit(task);
          this.closed.emit();
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Task not created',
            detail: extractNexaFlowErrorMessage(error, 'Task could not be created.'),
          });
        },
      });
  }

  getPriorityLabel(priority: TaskPriority): string {
    switch (priority) {
      case 'LOW':
        return 'Low';
      case 'MEDIUM':
        return 'Medium';
      case 'HIGH':
        return 'High';
      case 'URGENT':
        return 'Urgent';
    }
  }

  getStatusLabel(status: TaskStatus): string {
    switch (status) {
      case 'TODO':
        return 'To do';
      case 'IN_PROGRESS':
        return 'In progress';
      case 'DONE':
        return 'Done';
      case 'BLOCKED':
        return 'Blocked';
    }
  }
}
