import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AccountService } from 'app/core/auth/account.service';
import { Workspace } from 'app/core/nexaflow/nexaflow.model';
import { WorkspaceService } from 'app/core/nexaflow/workspace.service';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

@Component({
  selector: 'jhi-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  imports: [RouterLink, FontAwesomeModule, PageHeader, SectionPanel, StatePanel],
})
export default class Dashboard implements OnInit {
  readonly account = inject(AccountService).account;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);

  readonly hasWorkspaces = computed(() => this.workspaces().length > 0);

  private readonly workspaceService = inject(WorkspaceService);

  ngOnInit(): void {
    this.workspaceService.getMyWorkspaces().subscribe({
      next: workspaces => {
        this.workspaces.set(workspaces);
        this.errorMessage.set(null);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Workspace data could not be loaded. Confirm user-service is running and registered with Consul.');
        this.isLoading.set(false);
      },
    });
  }
}
