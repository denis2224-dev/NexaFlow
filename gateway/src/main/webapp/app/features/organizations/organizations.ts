import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { finalize } from 'rxjs';

import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { InvitationService } from 'app/core/nexaflow/invitation.service';
import { MembershipService } from 'app/core/nexaflow/membership.service';
import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { Invitation, Membership, Workspace, WorkspaceRole } from 'app/core/nexaflow/nexaflow.model';
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
  readonly isAccepting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly createDialogOpen = signal(false);
  readonly inviteDialogOpen = signal(false);
  readonly acceptDialogOpen = signal(false);
  readonly membersDialogOpen = signal(false);
  readonly invitationsDialogOpen = signal(false);
  readonly selectedWorkspace = signal<Workspace | null>(null);
  readonly members = signal<Membership[]>([]);
  readonly invitations = signal<Invitation[]>([]);
  readonly isMembersLoading = signal(false);
  readonly isInvitationsLoading = signal(false);
  readonly membersError = signal<string | null>(null);
  readonly invitationsError = signal<string | null>(null);
  readonly invitationsSuccess = signal<string | null>(null);
  readonly revokingInvitationId = signal<number | null>(null);
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

  readonly acceptForm = new FormGroup({
    token: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8), Validators.maxLength(500)],
    }),
  });

  private readonly workspaceService = inject(WorkspaceService);
  private readonly invitationService = inject(InvitationService);
  private readonly membershipService = inject(MembershipService);
  private readonly activeOrganizationService = inject(ActiveOrganizationService);

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
        error: error => {
          this.workspaces.set([]);
          this.errorMessage.set(this.extractErrorMessage(error, 'Organizations could not be loaded. Please try again.'));
        },
      });
  }

  openAcceptDialog(): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.acceptForm.reset({ token: '' });
    this.acceptDialogOpen.set(true);
  }

  closeAcceptDialog(): void {
    if (!this.isAccepting()) {
      this.acceptDialogOpen.set(false);
    }
  }

  acceptInvitation(): void {
    if (this.acceptForm.invalid) {
      this.acceptForm.markAllAsTouched();
      return;
    }

    this.isAccepting.set(true);
    this.errorMessage.set(null);
    this.invitationService
      .accept(this.acceptForm.getRawValue())
      .pipe(finalize(() => this.isAccepting.set(false)))
      .subscribe({
        next: () => {
          this.acceptDialogOpen.set(false);
          this.successMessage.set('Invitation accepted.');
          this.activeOrganizationService.refreshOrganizations();
          this.loadWorkspaces();
        },
        error: error => {
          this.errorMessage.set(this.extractErrorMessage(error, 'Invitation could not be accepted.'));
        },
      });
  }

  openMembersDialog(workspace: Workspace): void {
    const workspaceId = this.getWorkspaceId(workspace);
    if (workspaceId === null) {
      this.errorMessage.set('Members cannot be loaded for this workspace yet.');
      return;
    }

    this.selectedWorkspace.set(workspace);
    this.members.set([]);
    this.membersError.set(null);
    this.membersDialogOpen.set(true);
    this.loadMembers(workspaceId);
  }

  closeMembersDialog(): void {
    this.membersDialogOpen.set(false);
    this.members.set([]);
    this.membersError.set(null);
    this.selectedWorkspace.set(null);
  }

  loadMembers(organizationId: number): void {
    this.isMembersLoading.set(true);
    this.membersError.set(null);
    this.membershipService
      .getWorkspaceMembers(organizationId)
      .pipe(finalize(() => this.isMembersLoading.set(false)))
      .subscribe({
        next: members => this.members.set(members),
        error: error => {
          this.members.set([]);
          this.membersError.set(this.extractErrorMessage(error, 'Members could not be loaded.'));
        },
      });
  }

  openInvitationsDialog(workspace: Workspace): void {
    const workspaceId = this.getWorkspaceId(workspace);
    if (workspaceId === null) {
      this.errorMessage.set('Invitations cannot be loaded for this workspace yet.');
      return;
    }

    this.selectedWorkspace.set(workspace);
    this.invitations.set([]);
    this.invitationsError.set(null);
    this.invitationsSuccess.set(null);
    this.invitationsDialogOpen.set(true);
    this.loadInvitations(workspaceId);
  }

  closeInvitationsDialog(): void {
    if (this.revokingInvitationId() === null) {
      this.invitationsDialogOpen.set(false);
      this.invitations.set([]);
      this.invitationsError.set(null);
      this.invitationsSuccess.set(null);
      this.selectedWorkspace.set(null);
    }
  }

  loadInvitations(organizationId: number): void {
    this.isInvitationsLoading.set(true);
    this.invitationsError.set(null);
    this.invitationService
      .getForWorkspace(organizationId)
      .pipe(finalize(() => this.isInvitationsLoading.set(false)))
      .subscribe({
        next: invitations => this.invitations.set(invitations),
        error: error => {
          this.invitations.set([]);
          this.invitationsError.set(this.extractErrorMessage(error, 'Invitations could not be loaded.'));
        },
      });
  }

  revokeInvitation(invitation: Invitation): void {
    const workspace = this.selectedWorkspace();
    const workspaceId = workspace ? this.getWorkspaceId(workspace) : null;
    const invitationId = this.getInvitationId(invitation);

    if (workspaceId === null || invitationId === null) {
      this.invitationsError.set('This invitation cannot be revoked yet.');
      return;
    }

    this.revokingInvitationId.set(invitationId);
    this.invitationsError.set(null);
    this.invitationsSuccess.set(null);
    this.invitationService
      .revokeFromWorkspace(workspaceId, invitationId)
      .pipe(finalize(() => this.revokingInvitationId.set(null)))
      .subscribe({
        next: () => {
          this.invitationsSuccess.set('Invitation revoked.');
          this.loadInvitations(workspaceId);
        },
        error: error => {
          this.invitationsError.set(this.extractErrorMessage(error, 'Invitation could not be revoked.'));
        },
      });
  }

  refreshSelectedInvitations(): void {
    const workspace = this.selectedWorkspace();
    const workspaceId = workspace ? this.getWorkspaceId(workspace) : null;

    if (workspaceId !== null) {
      this.loadInvitations(workspaceId);
    }
  }

  refreshSelectedMembers(): void {
    const workspace = this.selectedWorkspace();
    const workspaceId = workspace ? this.getWorkspaceId(workspace) : null;

    if (workspaceId !== null) {
      this.loadMembers(workspaceId);
    }
  }

  getInvitationId(invitation: Invitation): number | null {
    return invitation.invitationId ?? invitation.id ?? null;
  }

  getMemberId(member: Membership): number | null {
    return member.membershipId ?? member.id ?? null;
  }

  getMemberName(member: Membership): string {
    return member.userLogin ?? member.login ?? member.userEmail ?? member.email ?? (member.userId ? `User ${member.userId}` : '-');
  }

  getMemberEmail(member: Membership): string | null {
    const email = member.userEmail ?? member.email ?? null;
    const login = member.userLogin ?? member.login ?? null;

    return email && email !== login ? email : null;
  }

  getInvitationEmail(invitation: Invitation): string {
    return invitation.email ?? invitation.userEmail ?? invitation.userLogin ?? '-';
  }

  getActiveLabel(active: boolean | null | undefined): string {
    if (active === true) {
      return 'Active';
    }

    if (active === false) {
      return 'Inactive';
    }

    return '-';
  }

  formatDate(value: string | null | undefined): string {
    if (!value) {
      return '-';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  }

  trackMember(index: number, member: Membership): number | string {
    return this.getMemberId(member) ?? member.userId ?? member.userLogin ?? member.userEmail ?? index;
  }

  trackInvitation(index: number, invitation: Invitation): number | string {
    return this.getInvitationId(invitation) ?? invitation.token ?? invitation.email ?? index;
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
          this.activeOrganizationService.refreshOrganizations();
          this.loadWorkspaces();
        },
        error: error => this.errorMessage.set(this.extractErrorMessage(error, 'Workspace could not be created.')),
      });
  }

  openInviteDialog(workspace: Workspace): void {
    const workspaceId = this.getWorkspaceId(workspace);
    if (workspaceId === null) {
      this.errorMessage.set('Invitations cannot be created for this workspace yet.');
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
        error: error => this.errorMessage.set(this.extractErrorMessage(error, 'Invitation could not be created.')),
      });
  }

  getWorkspaceId(workspace: Workspace): number | null {
    return workspace.organizationId ?? workspace.id ?? null;
  }

  trackWorkspace(index: number, workspace: Workspace): number | string {
    return this.getWorkspaceId(workspace) ?? workspace.slug ?? index;
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    return extractNexaFlowErrorMessage(error, fallback);
  }
}
