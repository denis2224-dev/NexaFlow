import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SelectChangeEvent, SelectModule } from 'primeng/select';

import { ActiveOrganizationService } from './active-organization.service';

interface WorkspaceOption {
  label: string;
  value: number;
}

@Component({
  selector: 'jhi-active-organization-selector',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './active-organization-selector.component.html',
  styleUrl: './active-organization-selector.component.scss',
  imports: [FormsModule, RouterLink, SelectModule],
})
export default class ActiveOrganizationSelectorComponent {
  readonly selectedOrganizationId = signal<number | null>(null);

  readonly workspaceOptions = computed<WorkspaceOption[]>(() =>
    this.activeOrganizationService
      .workspaces()
      .map(workspace => {
        const organizationId = this.activeOrganizationService.getWorkspaceId(workspace);
        return organizationId == null
          ? null
          : {
              label: workspace.name ?? workspace.slug ?? `Workspace ${organizationId}`,
              value: organizationId,
            };
      })
      .filter((option): option is WorkspaceOption => option !== null),
  );

  readonly activeWorkspaceName = computed(() => {
    const activeOrganization = this.activeOrganizationService.activeOrganization();
    return activeOrganization?.workspace.name ?? activeOrganization?.workspace.slug ?? null;
  });

  readonly isLoading = computed(() => this.activeOrganizationService.isLoading());
  readonly errorMessage = computed(() => this.activeOrganizationService.errorMessage());

  private readonly activeOrganizationService = inject(ActiveOrganizationService);

  constructor() {
    effect(() => {
      this.selectedOrganizationId.set(this.activeOrganizationService.selectedOrganizationId());
    });
  }

  changeWorkspace(event: SelectChangeEvent): void {
    const organizationId = Number(event.value);
    if (Number.isFinite(organizationId)) {
      this.activeOrganizationService.selectOrganization(organizationId);
    }
  }
}
