export type PlanCode = 'FREE' | 'PRO' | 'ENTERPRISE';

export type SubscriptionStatus = 'ACTIVE' | 'CANCELLED' | 'EXPIRED';

export interface BillingPlan {
  id?: number;
  code: PlanCode;
  name: string;
  priceMonthly: number | string;
  maxProjects: number;
  maxUsers: number;
  maxTasks: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface BillingSubscription {
  id?: number;
  organizationId: number;
  planCode: PlanCode;
  status: SubscriptionStatus;
  startedAt: string;
  expiresAt?: string | null;
  createdBy?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface UsageMetric {
  used: number;
  limit: number;
  remaining: number;
}

export interface BillingUsage {
  organizationId: number;
  subscription: BillingSubscription;
  projects: UsageMetric;
  users: UsageMetric;
  tasks: UsageMetric;
}

export interface ActivateSubscriptionRequest {
  organizationId: number;
  planCode: PlanCode;
}

export interface CancelSubscriptionRequest {
  organizationId: number;
}
