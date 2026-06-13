import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { finalize } from 'rxjs';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { Task, TaskPriority } from './task.model';
import { TaskService } from './task.service';

@Component({
  selector: 'jhi-edit-task-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './edit-task-dialog.component.html',
  styleUrl: './create-task-dialog.component.scss',
  imports: [ReactiveFormsModule, DialogModule, ButtonModule],
})
export default class EditTaskDialogComponent {
  readonly visible = input(false);
  readonly task = input<Task | null>(null);
  readonly closed = output();
  readonly updated = output<Task>();
  readonly isSaving = signal(false);
  readonly priorityOptions: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(180)] }),
    description: new FormControl<string | null>(null, { validators: [Validators.maxLength(2000)] }),
    priority: new FormControl<TaskPriority>('MEDIUM', { nonNullable: true, validators: [Validators.required] }),
    dueDate: new FormControl<string | null>(null),
  });

  private readonly taskService = inject(TaskService);
  private readonly messageService = inject(MessageService);

  constructor() {
    effect(() => {
      const task = this.task();
      if (this.visible() && task) {
        this.form.reset({
          title: task.title ?? '',
          description: task.description ?? null,
          priority: task.priority ?? 'MEDIUM',
          dueDate: task.dueDate ?? null,
        });
      }
    });
  }

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
    const task = this.task();
    if (task?.id == null || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.isSaving.set(true);
    this.taskService
      .update(task.id, {
        title: raw.title,
        description: raw.description,
        priority: raw.priority,
        dueDate: raw.dueDate === '' ? null : (raw.dueDate ?? null),
      })
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: updatedTask => {
          this.messageService.add({ severity: 'success', summary: 'Task updated', detail: updatedTask.title ?? 'Task updated.' });
          this.updated.emit(updatedTask);
          this.closed.emit();
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Task not updated',
            detail: extractNexaFlowErrorMessage(error, 'Task could not be updated.'),
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
}
