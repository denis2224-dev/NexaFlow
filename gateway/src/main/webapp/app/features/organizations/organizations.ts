import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { finalize } from 'rxjs';

import { InvitationService } from 'app/core/nexaflow/invitation.service';
import { Workspace, WorkspaceRole } from 'app/core/nexaflow/nexaflow.model';
import { WorkspaceService } from 'app/core/nexaflow/workspace.service';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

@Component({
  selector: 'jhi-organizations',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './organizations.html',
  styleUrl: './organizations.scss',
  imports: [ReactiveFormsModule, FontAwesomeModule, PageHeader, SectionPanel, StatePanel],
})
export default class Organizations implements OnInit {
  readonly workspaces = signal<Workspace[]>([]);
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly isInviting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly createDialogOpen = signal(false);
  readonly inviteDialogOpen = signal(false);
  readonly selectedWorkspace = signal<Workspace | null>(null);
  readonly roleOptions: WorkspaceRole[] = ['ADMIN', 'MEMBER'];

  readonly createForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2), Validators.maxLength(100)] }),
    slug: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)] }),
    description: new FormControl<string | null>(null, { validators: [Validators.maxLength(500)] }),
  });

  readonly inviteForm = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email, Validators.maxLength(254)] }),
    role: new FormControl<WorkspaceRole>('MEMBER', { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly workspaceService = inject(WorkspaceService);
  private readonly invitationService = inject(InvitationService);

  ngOnInit(): void {
    this.loadWorkspaces();
  }

  loadWorkspaces(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.workspaceService
      .getMyWorkspaces()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: workspaces => this.workspaces.set(workspaces),
        error: () => {
          this.workspaces.set([]);
          this.errorMessage.set(
            'Organizations could not be loaded. Confirm user-service is running, registered in Consul, and reachable through the gateway.',
          );
        },
      });
  }

  openCreateDialog(): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.createForm.reset({ name: '', slug: '', description: null });
    this.createDialogOpen.set(true);
  }

  closeCreateDialog(): void {
    if (!this.isSaving()) {
      this.createDialogOpen.set(false);
    }
  }

  createWorkspace(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.workspaceService
      .create(this.createForm.getRawValue())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: () => {
          this.createDialogOpen.set(false);
          this.successMessage.set('Workspace created.');
          this.loadWorkspaces();
        },
        error: () => this.errorMessage.set('Workspace could not be created. Check the backend validation response and try again.'),
      });
  }

  openInviteDialog(workspace: Workspace): void {
    const workspaceId = this.getWorkspaceId(workspace);
    if (workspaceId === null) {
      this.errorMessage.set('This workspace response does not include an organization identifier for invitations.');
      return;
    }

    this.selectedWorkspace.set(workspace);
    this.inviteForm.reset({ email: '', role: 'MEMBER' });
    this.inviteDialogOpen.set(true);
  }

  closeInviteDialog(): void {
    if (!this.isInviting()) {
      this.inviteDialogOpen.set(false);
      this.selectedWorkspace.set(null);
    }
  }

  sendInvitation(): void {
    const workspace = this.selectedWorkspace();
    const workspaceId = workspace ? this.getWorkspaceId(workspace) : null;

    if (this.inviteForm.invalid || workspaceId === null) {
      this.inviteForm.markAllAsTouched();
      return;
    }

    this.isInviting.set(true);
    this.errorMessage.set(null);
    this.invitationService
      .createForWorkspace(workspaceId, this.inviteForm.getRawValue())
      .pipe(finalize(() => this.isInviting.set(false)))
      .subscribe({
        next: () => {
          this.inviteDialogOpen.set(false);
          this.selectedWorkspace.set(null);
          this.successMessage.set('Invitation created.');
        },
        error: () => this.errorMessage.set('Invitation could not be created. Confirm the invitation endpoint is enabled in user-service.'),
      });
  }

  getWorkspaceId(workspace: Workspace): number | null {
    return workspace.organizationId ?? workspace.id ?? null;
  }

  trackWorkspace(index: number, workspace: Workspace): number | string {
    return this.getWorkspaceId(workspace) ?? workspace.slug ?? index;
  }
}
