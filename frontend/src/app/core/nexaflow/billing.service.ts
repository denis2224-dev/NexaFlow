import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import {
  ActivateSubscriptionRequest,
  BillingPlan,
  BillingSubscription,
  BillingUsage,
  CancelSubscriptionRequest,
  PlanCode,
} from './billing.model';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/billing', 'billing-service');

  getPlans(): Observable<BillingPlan[]> {
    return this.http.get<BillingPlan[]>(`${this.resourceUrl}/plans`);
  }

  getCurrentSubscription(organizationId: number): Observable<BillingSubscription> {
    return this.http.get<BillingSubscription>(`${this.resourceUrl}/subscription/my`, { params: { organizationId } });
  }

  activateSubscription(organizationId: number, planCode: PlanCode): Observable<BillingSubscription> {
    const request: ActivateSubscriptionRequest = { organizationId, planCode };
    return this.http.post<BillingSubscription>(`${this.resourceUrl}/subscription/activate`, request);
  }

  cancelSubscription(organizationId: number): Observable<BillingSubscription> {
    const request: CancelSubscriptionRequest = { organizationId };
    return this.http.post<BillingSubscription>(`${this.resourceUrl}/subscription/cancel`, request);
  }

  getUsage(organizationId: number): Observable<BillingUsage> {
    return this.http.get<BillingUsage>(`${this.resourceUrl}/usage`, { params: { organizationId } });
  }
}
