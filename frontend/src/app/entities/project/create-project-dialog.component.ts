import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { finalize } from 'rxjs';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { ProjectService } from './project.service';
import { Project, ProjectStatus } from './project.model';

@Component({
  selector: 'jhi-create-project-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './create-project-dialog.component.html',
  styleUrl: './create-project-dialog.component.scss',
  imports: [ReactiveFormsModule, DialogModule, ButtonModule],
})
export default class CreateProjectDialogComponent {
  readonly visible = input(false);
  readonly organizationId = input.required<number>();
  readonly closed = output();
  readonly created = output<Project>();
  readonly isSaving = signal(false);
  readonly statusOptions: ProjectStatus[] = ['ACTIVE', 'COMPLETED', 'ARCHIVED'];

  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(120)] }),
    description: new FormControl<string | null>(null, { validators: [Validators.maxLength(1000)] }),
    status: new FormControl<ProjectStatus>('ACTIVE', { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly projectService = inject(ProjectService);
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

    this.isSaving.set(true);
    this.projectService
      .create(this.organizationId(), this.form.getRawValue())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: project => {
          this.messageService.add({ severity: 'success', summary: 'Project created', detail: project.name ?? 'Project was created.' });
          this.form.reset({ name: '', description: null, status: 'ACTIVE' });
          this.created.emit(project);
          this.closed.emit();
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Project not created',
            detail: extractNexaFlowErrorMessage(error, 'Project could not be created.'),
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
