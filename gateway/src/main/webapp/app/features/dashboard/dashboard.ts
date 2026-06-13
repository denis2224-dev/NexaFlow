import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { CardModule } from 'primeng/card';

import { AccountService } from 'app/core/auth/account.service';
import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';
import { DashboardSummary } from './dashboard-summary.model';
import { DashboardSummaryService } from './dashboard-summary.service';

@Component({
  selector: 'jhi-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  imports: [RouterLink, FontAwesomeModule, CardModule, PageHeader, SectionPanel, StatePanel],
})
export default class Dashboard implements OnInit {
  readonly account = inject(AccountService).account;
  readonly isSummaryLoading = signal(false);
  readonly summaryErrorMessage = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly isLoading = computed(() => this.activeOrganizationService.isLoading());
  readonly errorMessage = computed(() => this.activeOrganizationService.errorMessage());
  readonly workspaces = computed(() => this.activeOrganizationService.workspaces());
  readonly activeOrganization = computed(() => this.activeOrganizationService.activeOrganization());
  readonly hasWorkspaces = computed(() => this.workspaces().length > 0);
  readonly workspaceName = computed(() => {
    const activeOrganization = this.activeOrganization();
    return activeOrganization?.workspace.name ?? activeOrganization?.workspace.slug ?? null;
  });

  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly dashboardSummaryService = inject(DashboardSummaryService);
  private lastOrganizationId: number | null = null;

  constructor() {
    effect(() => {
      const activeOrganization = this.activeOrganization();
      const isOrganizationLoading = this.isLoading();

      if (isOrganizationLoading) {
        this.summary.set(null);
        this.summaryErrorMessage.set(null);
        return;
      }

      if (!activeOrganization) {
        this.lastOrganizationId = null;
        this.summary.set(null);
        this.summaryErrorMessage.set(null);
        this.isSummaryLoading.set(false);
        return;
      }

      if (this.lastOrganizationId === activeOrganization.organizationId) {
        return;
      }

      this.lastOrganizationId = activeOrganization.organizationId;
      this.summary.set(null);
      untracked(() => this.loadDashboardSummary(activeOrganization.organizationId));
    });
  }

  ngOnInit(): void {
    this.activeOrganizationService.loadOrganizations();
  }

  private loadDashboardSummary(organizationId: number): void {
    this.isSummaryLoading.set(true);
    this.summaryErrorMessage.set(null);
    this.dashboardSummaryService.getSummary(organizationId).subscribe({
      next: summary => {
        this.summary.set(summary);
        this.isSummaryLoading.set(false);
      },
      error: () => {
        this.summary.set(null);
        this.summaryErrorMessage.set('Project dashboard summary could not be loaded.');
        this.isSummaryLoading.set(false);
      },
    });
  }
}
