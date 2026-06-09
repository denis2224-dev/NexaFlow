import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { finalize } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { InvitationService } from 'app/core/nexaflow/invitation.service';
import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { Invitation } from 'app/core/nexaflow/nexaflow.model';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

@Component({
  selector: 'jhi-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  imports: [RouterLink, FontAwesomeModule, PageHeader, SectionPanel, StatePanel],
})
export default class Profile implements OnInit {
  readonly account = inject(AccountService).account;
  readonly invitations = signal<Invitation[]>([]);
  readonly invitationsLoading = signal(true);
  readonly invitationsError = signal<string | null>(null);
  readonly invitationsSuccess = signal<string | null>(null);
  readonly acceptingInvitationId = signal<number | null>(null);
  readonly rejectingInvitationId = signal<number | null>(null);

  readonly displayName = computed(() => {
    const account = this.account();
    if (!account) {
      return 'Profile';
    }
    return [account.firstName, account.lastName].filter(Boolean).join(' ').trim() || account.login;
  });

  readonly initial = computed(() => this.displayName().charAt(0).toUpperCase());

  private readonly invitationService = inject(InvitationService);

  ngOnInit(): void {
    this.loadInvitations();
  }

  loadInvitations(): void {
    this.invitationsLoading.set(true);
    this.invitationsError.set(null);
    this.invitationService
      .getMyPending()
      .pipe(finalize(() => this.invitationsLoading.set(false)))
      .subscribe({
        next: invitations => this.invitations.set(invitations),
        error: error => {
          this.invitations.set([]);
          this.invitationsError.set(extractNexaFlowErrorMessage(error, 'Invitations could not be loaded.'));
        },
      });
  }

  acceptInvitation(invitation: Invitation): void {
    const invitationId = this.getInvitationId(invitation);
    if (invitationId === null || !invitation.token) {
      this.invitationsError.set('This invitation cannot be accepted yet.');
      return;
    }

    this.acceptingInvitationId.set(invitationId);
    this.invitationsError.set(null);
    this.invitationsSuccess.set(null);
    this.invitationService
      .accept({ token: invitation.token })
      .pipe(finalize(() => this.acceptingInvitationId.set(null)))
      .subscribe({
        next: () => {
          this.invitationsSuccess.set('Invitation accepted.');
          this.loadInvitations();
        },
        error: error => {
          this.invitationsError.set(extractNexaFlowErrorMessage(error, 'Invitation could not be accepted.'));
        },
      });
  }

  rejectInvitation(invitation: Invitation): void {
    const invitationId = this.getInvitationId(invitation);
    if (invitationId === null) {
      this.invitationsError.set('This invitation cannot be declined yet.');
      return;
    }

    this.rejectingInvitationId.set(invitationId);
    this.invitationsError.set(null);
    this.invitationsSuccess.set(null);
    this.invitationService
      .reject(invitationId)
      .pipe(finalize(() => this.rejectingInvitationId.set(null)))
      .subscribe({
        next: () => {
          this.invitationsSuccess.set('Invitation declined.');
          this.loadInvitations();
        },
        error: error => {
          this.invitationsError.set(extractNexaFlowErrorMessage(error, 'Invitation could not be declined.'));
        },
      });
  }

  getInvitationId(invitation: Invitation): number | null {
    return invitation.invitationId ?? invitation.id ?? null;
  }

  getInvitationWorkspaceName(invitation: Invitation): string {
    return invitation.organizationName ?? invitation.organizationSlug ?? 'Workspace invitation';
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
    }).format(date);
  }

  trackInvitation(index: number, invitation: Invitation): number | string {
    return this.getInvitationId(invitation) ?? invitation.token ?? invitation.email ?? index;
  }
}
