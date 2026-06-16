import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { finalize } from 'rxjs';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { Project, ProjectStatus } from './project.model';
import { ProjectService } from './project.service';

@Component({
  selector: 'jhi-edit-project-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './edit-project-dialog.component.html',
  styleUrl: './create-project-dialog.component.scss',
  imports: [ReactiveFormsModule, DialogModule, ButtonModule],
})
export default class EditProjectDialogComponent {
  readonly visible = input(false);
  readonly project = input<Project | null>(null);
  readonly closed = output();
  readonly updated = output<Project>();
  readonly isSaving = signal(false);
  readonly statusOptions: ProjectStatus[] = ['ACTIVE', 'COMPLETED', 'ARCHIVED'];

  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(120)] }),
    description: new FormControl<string | null>(null, { validators: [Validators.maxLength(1000)] }),
    status: new FormControl<ProjectStatus>('ACTIVE', { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly projectService = inject(ProjectService);
  private readonly messageService = inject(MessageService);

  constructor() {
    effect(() => {
      const project = this.project();
      if (this.visible() && project) {
        this.form.reset({
          name: project.name ?? '',
          description: project.description ?? null,
          status: project.status ?? 'ACTIVE',
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
    const project = this.project();
    if (project?.id == null || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.projectService
      .update(project.id, this.form.getRawValue())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: updatedProject => {
          this.messageService.add({ severity: 'success', summary: 'Project updated', detail: updatedProject.name ?? 'Project updated.' });
          this.updated.emit(updatedProject);
          this.closed.emit();
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Project not updated',
            detail: extractNexaFlowErrorMessage(error, 'Project could not be updated.'),
          });
        },
      });
  }

  getStatusLabel(status: ProjectStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'Active';
      case 'COMPLETED':
        return 'Completed';
      case 'ARCHIVED':
        return 'Archived';
    }
  }
}
