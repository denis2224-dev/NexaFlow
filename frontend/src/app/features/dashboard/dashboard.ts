import { DatePipe } from "@angular/common";
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from "@angular/core";
import { RouterLink } from "@angular/router";

import { FontAwesomeModule } from "@fortawesome/angular-fontawesome";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ChartModule } from "primeng/chart";
import { TagModule } from "primeng/tag";
import { catchError, forkJoin, of } from "rxjs";
import type { ChartData, ChartOptions } from "chart.js";
import "chart.js/auto";

import { AccountService } from "app/core/auth/account.service";
import { ActiveOrganizationService } from "app/core/nexaflow/active-organization.service";
import {
  BillingSubscription,
  BillingUsage,
} from "app/core/nexaflow/billing.model";
import { BillingService } from "app/core/nexaflow/billing.service";
import {
  INotification,
  NotificationType,
} from "app/shared/notification/notification.model";
import { NotificationStateService } from "app/shared/notification/notification-state.service";
import SectionPanel from "app/shared/ui/section-panel/section-panel";
import StatePanel from "app/shared/ui/state-panel/state-panel";
import { DashboardSummary } from "./dashboard-summary.model";
import { DashboardSummaryService } from "./dashboard-summary.service";

type StatTone = "blue" | "green" | "amber" | "red" | "violet" | "slate";

interface DashboardStat {
  title: string;
  value: string | number;
  detail: string;
  icon: string;
  tone: StatTone;
}

@Component({
  selector: "jhi-dashboard",
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: "./dashboard.html",
  styleUrl: "./dashboard.scss",
  imports: [
    DatePipe,
    RouterLink,
    FontAwesomeModule,
    ButtonModule,
    CardModule,
    ChartModule,
    TagModule,
    SectionPanel,
    StatePanel,
  ],
})
export default class Dashboard implements OnInit {
  readonly account = inject(AccountService).account;
  readonly isSummaryLoading = signal(false);
  readonly summaryErrorMessage = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly subscription = signal<BillingSubscription | null>(null);
  readonly usage = signal<BillingUsage | null>(null);
  readonly billingErrorMessage = signal<string | null>(null);
  readonly isLoading = computed(() =>
    this.activeOrganizationService.isLoading(),
  );
  readonly errorMessage = computed(() =>
    this.activeOrganizationService.errorMessage(),
  );
  readonly workspaces = computed(() =>
    this.activeOrganizationService.workspaces(),
  );
  readonly activeOrganization = computed(() =>
    this.activeOrganizationService.activeOrganization(),
  );
  readonly hasWorkspaces = computed(() => this.workspaces().length > 0);
  readonly latestNotifications = computed(() =>
    this.notificationStateService.latestNotifications(),
  );
  readonly workspaceName = computed(() => {
    const activeOrganization = this.activeOrganization();
    return (
      activeOrganization?.workspace.name ??
      activeOrganization?.workspace.slug ??
      null
    );
  });
  readonly workspaceDescription = computed(
    () => this.activeOrganization()?.workspace.description ?? null,
  );
  readonly workspaceRole = computed(
    () => this.activeOrganization()?.workspace.role ?? null,
  );
  readonly greeting = computed(() => {
    const account = this.account();
    const name =
      [account?.firstName, account?.lastName]
        .filter(Boolean)
        .join(" ")
        .trim() || account?.login;
    return name ? `Welcome back, ${name}` : "Welcome back";
  });
  readonly completionRate = computed(() => {
    const summary = this.summary();
    if (!summary || summary.totalTasks <= 0) {
      return null;
    }
    return Math.round((summary.completedTasks / summary.totalTasks) * 100);
  });
  readonly stats = computed<DashboardStat[]>(() => {
    const summary = this.summary();
    const stats: DashboardStat[] = [];

    if (summary) {
      stats.push(
        {
          title: "Total projects",
          value: summary.totalProjects,
          detail: "Projects in this organization",
          icon: "pi pi-folder",
          tone: "blue",
        },
        {
          title: "Active projects",
          value: summary.activeProjects,
          detail: "Currently active projects",
          icon: "pi pi-play-circle",
          tone: "green",
        },
        {
          title: "Completed tasks",
          value: summary.completedTasks,
          detail: "Tasks marked done",
          icon: "pi pi-check-circle",
          tone: "violet",
        },
        {
          title: "Overdue tasks",
          value: summary.overdueTasks,
          detail:
            summary.overdueTasks > 0
              ? "Needs attention"
              : "No overdue tasks reported",
          icon: "pi pi-exclamation-triangle",
          tone: summary.overdueTasks > 0 ? "red" : "slate",
        },
        {
          title: "Total tasks",
          value: summary.totalTasks,
          detail: "Tasks across loaded projects",
          icon: "pi pi-list-check",
          tone: "blue",
        },
      );
    }

    const usage = this.usage();
    if (usage) {
      stats.push({
        title: "Organization members",
        value: usage.users.used,
        detail: `${this.formatLimit(usage.users.limit)} seats available`,
        icon: "pi pi-users",
        tone: "green",
      });
    }

    if (!this.billingErrorMessage()) {
      stats.push({
        title: "Current plan",
        value:
          this.subscription()?.planCode ??
          this.usage()?.subscription.planCode ??
          "No record",
        detail:
          this.subscription()?.status ??
          "Billing subscription has not been activated",
        icon: "pi pi-credit-card",
        tone: "amber",
      });
    }

    return stats;
  });
  readonly taskCompletionChart = computed<ChartData<
    "doughnut",
    number[],
    string
  > | null>(() => {
    const summary = this.summary();
    if (!summary || summary.totalTasks <= 0) {
      return null;
    }

    const incompleteTasks = Math.max(
      summary.totalTasks - summary.completedTasks,
      0,
    );
    return {
      labels: ["Completed", "Incomplete"],
      datasets: [
        {
          data: [summary.completedTasks, incompleteTasks],
          backgroundColor: ["#16a34a", "#2563eb"],
          borderColor: ["#ffffff", "#ffffff"],
          borderWidth: 3,
          hoverBackgroundColor: ["#15803d", "#1d4ed8"],
        },
      ],
    };
  });
  readonly projectStatusChart = computed<ChartData<
    "bar",
    number[],
    string
  > | null>(() => {
    const summary = this.summary();
    if (!summary || summary.totalProjects <= 0) {
      return null;
    }

    const otherProjects = Math.max(
      summary.totalProjects -
        summary.activeProjects -
        summary.completedProjects,
      0,
    );
    return {
      labels: ["Active", "Completed", "Other"],
      datasets: [
        {
          label: "Projects",
          data: [
            summary.activeProjects,
            summary.completedProjects,
            otherProjects,
          ],
          backgroundColor: ["#2563eb", "#16a34a", "#64748b"],
          borderRadius: 8,
        },
      ],
    };
  });
  readonly doughnutOptions: ChartOptions<"doughnut"> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          boxWidth: 10,
          boxHeight: 10,
          color: "#475569",
          usePointStyle: true,
        },
      },
      tooltip: { enabled: true },
    },
    cutout: "68%",
  };
  readonly barOptions: ChartOptions<"bar"> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { enabled: true },
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: "#64748b" } },
      y: {
        beginAtZero: true,
        ticks: { precision: 0, color: "#64748b" },
        grid: { color: "rgba(148, 163, 184, 0.18)" },
      },
    },
  };

  readonly notificationStateService = inject(NotificationStateService);

  private readonly activeOrganizationService = inject(
    ActiveOrganizationService,
  );
  private readonly dashboardSummaryService = inject(DashboardSummaryService);
  private readonly billingService = inject(BillingService);
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
        this.subscription.set(null);
        this.usage.set(null);
        this.summaryErrorMessage.set(null);
        this.billingErrorMessage.set(null);
        this.isSummaryLoading.set(false);
        return;
      }

      if (this.lastOrganizationId === activeOrganization.organizationId) {
        return;
      }

      this.lastOrganizationId = activeOrganization.organizationId;
      this.summary.set(null);
      this.subscription.set(null);
      this.usage.set(null);
      untracked(() => {
        this.loadDashboardSummary(activeOrganization.organizationId);
        this.loadBillingContext(activeOrganization.organizationId);
      });
    });
  }

  ngOnInit(): void {
    this.activeOrganizationService.loadOrganizations();
    this.notificationStateService.initializeNotifications();
  }

  formatLimit(value: number): string {
    return value >= 100000 ? "Unlimited" : `${value}`;
  }

  getNotificationIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      TASK_ASSIGNED: "pi pi-check-square",
      COMMENT_ADDED: "pi pi-comments",
      PROJECT_UPDATED: "pi pi-folder",
      INVITATION_ACCEPTED: "pi pi-user-plus",
      SUBSCRIPTION_CHANGED: "pi pi-credit-card",
      SYSTEM: "pi pi-info-circle",
    };

    return icons[type];
  }

  getNotificationLabel(notification: INotification): string {
    return notification.title || notification.message || "Workspace activity";
  }

  private loadDashboardSummary(organizationId: number): void {
    this.isSummaryLoading.set(true);
    this.summaryErrorMessage.set(null);
    this.dashboardSummaryService.getSummary(organizationId).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.isSummaryLoading.set(false);
      },
      error: () => {
        this.summary.set(null);
        this.summaryErrorMessage.set(
          "Project dashboard summary could not be loaded.",
        );
        this.isSummaryLoading.set(false);
      },
    });
  }

  private loadBillingContext(organizationId: number): void {
    this.billingErrorMessage.set(null);
    forkJoin({
      subscription: this.billingService
        .getCurrentSubscription(organizationId)
        .pipe(catchError(() => of(null))),
      usage: this.billingService
        .getUsage(organizationId)
        .pipe(catchError(() => of(null))),
    }).subscribe({
      next: (result) => {
        this.subscription.set(result.subscription);
        this.usage.set(result.usage);
      },
      error: () => {
        this.subscription.set(null);
        this.usage.set(null);
        this.billingErrorMessage.set("Billing context could not be loaded.");
      },
    });
  }
}
