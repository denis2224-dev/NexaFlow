import { Injectable, computed, inject, signal } from '@angular/core';

import { BehaviorSubject, Observable, filter } from 'rxjs';

import { Workspace } from './nexaflow.model';
import { WorkspaceService } from './workspace.service';

export interface ActiveOrganization {
  organizationId: number;
  workspace: Workspace;
}

@Injectable({ providedIn: 'root' })
export class ActiveOrganizationService {
  static readonly storageKey = 'nexaflow.activeOrganizationId';

  readonly workspaces = signal<Workspace[]>([]);
  readonly activeOrganization = signal<ActiveOrganization | null>(null);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly selectedOrganizationId = computed(() => this.activeOrganization()?.organizationId ?? null);

  get activeOrganization$(): Observable<ActiveOrganization> {
    return this.activeOrganizationSubject
      .asObservable()
      .pipe(filter((activeOrganization): activeOrganization is ActiveOrganization => activeOrganization !== null));
  }

  private readonly activeOrganizationSubject = new BehaviorSubject<ActiveOrganization | null>(null);
  private readonly workspaceService = inject(WorkspaceService);
  private hasLoaded = false;

  getActiveOrganization(): Observable<ActiveOrganization> {
    this.loadOrganizations();
    return this.activeOrganization$;
  }

  loadOrganizations(force = false): void {
    if ((this.isLoading() || this.hasLoaded) && !force) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.workspaceService.getMyWorkspaces().subscribe({
      next: workspaces => {
        this.hasLoaded = true;
        this.workspaces.set(workspaces);
        this.syncActiveOrganization(workspaces);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasLoaded = false;
        this.workspaces.set([]);
        this.setActiveOrganization(null);
        this.errorMessage.set('Workspace context could not be loaded.');
        this.isLoading.set(false);
      },
    });
  }

  refreshOrganizations(): void {
    this.loadOrganizations(true);
  }

  selectOrganization(organizationId: number): void {
    const workspace = this.workspaces().find(candidate => this.getWorkspaceId(candidate) === organizationId);

    if (!workspace) {
      this.clearStoredOrganizationId();
      this.syncActiveOrganization(this.workspaces());
      return;
    }

    this.storeOrganizationId(organizationId);
    this.setActiveOrganization({ organizationId, workspace });
  }

  getWorkspaceId(workspace?: Workspace): number | null {
    return workspace?.organizationId ?? workspace?.id ?? null;
  }

  private syncActiveOrganization(workspaces: Workspace[]): void {
    if (workspaces.length === 0) {
      this.clearStoredOrganizationId();
      this.setActiveOrganization(null);
      this.errorMessage.set('No workspace selected. Create or join a workspace to manage projects.');
      return;
    }

    const currentOrganizationId = this.activeOrganization()?.organizationId ?? null;
    const storedOrganizationId = this.readStoredOrganizationId();
    const selectedWorkspace =
      this.findWorkspace(workspaces, currentOrganizationId) ?? this.findWorkspace(workspaces, storedOrganizationId) ?? workspaces[0];
    const selectedOrganizationId = this.getWorkspaceId(selectedWorkspace);

    if (selectedOrganizationId == null) {
      this.clearStoredOrganizationId();
      this.setActiveOrganization(null);
      this.errorMessage.set('No workspace selected. Create or join a workspace to manage projects.');
      return;
    }

    this.storeOrganizationId(selectedOrganizationId);
    this.setActiveOrganization({ organizationId: selectedOrganizationId, workspace: selectedWorkspace });
  }

  private findWorkspace(workspaces: Workspace[], organizationId: number | null): Workspace | undefined {
    if (organizationId == null) {
      return undefined;
    }

    return workspaces.find(workspace => this.getWorkspaceId(workspace) === organizationId);
  }

  private setActiveOrganization(activeOrganization: ActiveOrganization | null): void {
    this.activeOrganization.set(activeOrganization);
    this.activeOrganizationSubject.next(activeOrganization);
  }

  private readStoredOrganizationId(): number | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const rawValue = localStorage.getItem(ActiveOrganizationService.storageKey);
    if (!rawValue) {
      return null;
    }

    const organizationId = Number(rawValue);
    return Number.isFinite(organizationId) ? organizationId : null;
  }

  private storeOrganizationId(organizationId: number): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(ActiveOrganizationService.storageKey, `${organizationId}`);
    }
  }

  private clearStoredOrganizationId(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(ActiveOrganizationService.storageKey);
    }
  }
}
