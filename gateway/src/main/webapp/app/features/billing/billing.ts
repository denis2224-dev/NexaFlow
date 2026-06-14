import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressBarModule } from 'primeng/progressbar';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { Observable, finalize, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { BillingPlan, BillingSubscription, BillingUsage, PlanCode, UsageMetric } from 'app/core/nexaflow/billing.model';
import { BillingService } from 'app/core/nexaflow/billing.service';
import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

type TagSeverity = 'success' | 'info' | 'secondary' | 'warn' | 'danger' | 'contrast';

interface UsageItem {
  key: 'projects' | 'users' | 'tasks';
  label: string;
  metric: UsageMetric;
}

@Component({
  selector: 'jhi-billing',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './billing.html',
  styleUrl: './billing.scss',
  imports: [
    CurrencyPipe,
    DatePipe,
    FontAwesomeModule,
    ButtonModule,
    CardModule,
    ProgressBarModule,
    TagModule,
    ToastModule,
    PageHeader,
    SectionPanel,
    StatePanel,
  ],
})
export default class Billing implements OnInit {
  readonly plans = signal<BillingPlan[]>([]);
  readonly subscription = signal<BillingSubscription | null>(null);
  readonly usage = signal<BillingUsage | null>(null);
  readonly isLoading = signal(true);
  readonly isChangingPlan = signal(false);
  readonly isCancelling = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly authorizationError = signal(false);
  readonly organizationId = signal<number | null>(null);
  readonly workspaceName = signal<string | null>(null);

  readonly activeOrganization = computed(() => this.activeOrganizationService.activeOrganization());
  readonly currentPlanCode = computed<PlanCode>(() => this.subscription()?.planCode ?? 'FREE');
  readonly currentPlan = computed(() => this.plans().find(plan => plan.code === this.currentPlanCode()) ?? null);
  readonly usingDefaultFreePlan = computed(() => this.subscription() === null && this.currentPlanCode() === 'FREE');
  readonly usageItems = computed<UsageItem[]>(() => {
    const usage = this.usage();
    if (!usage) {
      return [];
    }
    return [
      { key: 'projects', label: 'Projects', metric: usage.projects },
      { key: 'users', label: 'Users', metric: usage.users },
      { key: 'tasks', label: 'Tasks', metric: usage.tasks },
    ];
  });

  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly billingService = inject(BillingService);
  private readonly messageService = inject(MessageService);
  private lastOrganizationId: number | null = null;

  constructor() {
    effect(() => {
      const activeOrganization = this.activeOrganization();
      const isOrganizationLoading = this.activeOrganizationService.isLoading();

      if (isOrganizationLoading) {
        this.resetBillingState();
        this.isLoading.set(true);
        return;
      }

      if (!activeOrganization) {
        this.lastOrganizationId = null;
        this.organizationId.set(null);
        this.workspaceName.set(null);
        this.resetBillingState();
        this.isLoading.set(false);
        this.errorMessage.set(
          this.activeOrganizationService.errorMessage() ?? 'No workspace selected. Create or join a workspace to manage billing.',
        );
        return;
      }

      const organizationChanged = this.lastOrganizationId !== activeOrganization.organizationId;
      this.lastOrganizationId = activeOrganization.organizationId;
      this.organizationId.set(activeOrganization.organizationId);
      this.workspaceName.set(activeOrganization.workspace.name ?? activeOrganization.workspace.slug ?? 'Current workspace');

      if (organizationChanged) {
        this.resetBillingState();
        untracked(() => this.loadBilling(activeOrganization.organizationId));
      }
    });
  }

  ngOnInit(): void {
    this.activeOrganizationService.loadOrganizations();
  }

  loadBilling(organizationId = this.organizationId()): void {
    if (organizationId == null) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.authorizationError.set(false);

    forkJoin({
      plans: this.billingService.getPlans(),
      subscription: this.billingService
        .getCurrentSubscription(organizationId)
        .pipe(catchError(error => this.handleOptionalSubscription(error))),
      usage: this.billingService.getUsage(organizationId).pipe(catchError(error => this.handleOptionalUsage(error))),
    })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: result => {
          this.plans.set(result.plans);
          this.subscription.set(result.subscription);
          this.usage.set(result.usage);
        },
        error: error => this.handleLoadError(error),
      });
  }

  switchPlan(plan: BillingPlan): void {
    const organizationId = this.organizationId();
    if (organizationId == null || plan.code === this.currentPlanCode() || this.isChangingPlan()) {
      return;
    }

    this.isChangingPlan.set(true);
    this.billingService
      .activateSubscription(organizationId, plan.code)
      .pipe(finalize(() => this.isChangingPlan.set(false)))
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Plan updated',
            detail: `${plan.name} is now active for ${this.workspaceName() ?? 'this workspace'}.`,
          });
          this.loadBilling(organizationId);
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Plan not updated',
            detail: this.getActionErrorMessage(error, 'The subscription could not be changed.'),
          });
        },
      });
  }

  cancelSubscription(): void {
    const organizationId = this.organizationId();
    const subscription = this.subscription();
    if (organizationId == null || !subscription || this.isCancelling()) {
      return;
    }

    const confirmed = window.confirm('Cancel the current subscription for this workspace? You can activate another plan at any time.');
    if (!confirmed) {
      return;
    }

    this.isCancelling.set(true);
    this.billingService
      .cancelSubscription(organizationId)
      .pipe(finalize(() => this.isCancelling.set(false)))
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Subscription cancelled',
            detail: 'The workspace no longer has an active paid subscription. You can activate the FREE plan from the plans below.',
          });
          this.loadBilling(organizationId);
        },
        error: error => {
          this.messageService.add({
            severity: 'error',
            summary: 'Subscription not cancelled',
            detail: this.getActionErrorMessage(error, 'The subscription could not be cancelled.'),
          });
        },
      });
  }

  getUsagePercent(metric: UsageMetric): number {
    if (metric.limit <= 0) {
      return 100;
    }
    return Math.min(Math.round((metric.used / metric.limit) * 100), 100);
  }

  getUsageSeverity(metric: UsageMetric): TagSeverity {
    const percentage = this.getUsagePercent(metric);
    if (percentage >= 100) {
      return 'danger';
    }
    if (percentage >= 80) {
      return 'warn';
    }
    return 'success';
  }

  getStatusSeverity(status?: string): TagSeverity {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'CANCELLED':
        return 'secondary';
      case 'EXPIRED':
        return 'warn';
      default:
        return 'info';
    }
  }

  getStatusLabel(status?: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Active';
      case 'CANCELLED':
        return 'Cancelled';
      case 'EXPIRED':
        return 'Expired';
      default:
        return 'Default FREE';
    }
  }

  formatLimit(value: number): string {
    return value >= 100000 ? 'Unlimited' : `${value}`;
  }

  isCurrentPlan(plan: BillingPlan): boolean {
    return plan.code === this.currentPlanCode();
  }

  trackPlan(_index: number, plan: BillingPlan): PlanCode {
    return plan.code;
  }

  trackUsage(_index: number, item: UsageItem): string {
    return item.key;
  }

  private resetBillingState(): void {
    this.plans.set([]);
    this.subscription.set(null);
    this.usage.set(null);
    this.errorMessage.set(null);
    this.authorizationError.set(false);
  }

  private handleOptionalSubscription(error: unknown): Observable<null> {
    if (error instanceof HttpErrorResponse && error.status === 404) {
      return of(null);
    }
    throw error;
  }

  private handleOptionalUsage(error: unknown): Observable<null> {
    if (error instanceof HttpErrorResponse && error.status === 404) {
      return of(null);
    }
    throw error;
  }

  private handleLoadError(error: unknown): void {
    this.plans.set([]);
    this.subscription.set(null);
    this.usage.set(null);
    this.authorizationError.set(error instanceof HttpErrorResponse && error.status === 403);
    this.errorMessage.set(
      this.getActionErrorMessage(error, 'Billing information could not be loaded. Billing-service may be unavailable.'),
    );
  }

  private getActionErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse && error.status === 403) {
      return 'You are not allowed to manage billing for this workspace, or you are not a member of it.';
    }
    return extractNexaFlowErrorMessage(error, fallback);
  }
}
