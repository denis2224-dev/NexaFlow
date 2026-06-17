import { DatePipe } from "@angular/common";
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  effect,
  inject,
  signal,
  untracked,
} from "@angular/core";
import { RouterLink } from "@angular/router";

import { MenuItem, MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { MenuModule } from "primeng/menu";
import { PaginatorModule } from "primeng/paginator";
import { SkeletonModule } from "primeng/skeleton";
import { TagModule } from "primeng/tag";
import { TableModule } from "primeng/table";
import { ToastModule } from "primeng/toast";
import type { TableLazyLoadEvent } from "primeng/types/table";
import { finalize } from "rxjs";

import { ActiveOrganizationService } from "app/core/nexaflow/active-organization.service";
import { extractNexaFlowErrorMessage } from "app/core/nexaflow/nexaflow-error.util";
import PageHeader from "app/shared/ui/page-header/page-header";
import SectionPanel from "app/shared/ui/section-panel/section-panel";
import StatePanel from "app/shared/ui/state-panel/state-panel";
import CreateProjectDialogComponent from "./create-project-dialog.component";
import EditProjectDialogComponent from "./edit-project-dialog.component";
import { Project, ProjectStatus } from "./project.model";
import { ProjectService } from "./project.service";

@Component({
  selector: "jhi-projects",
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: "./projects.component.html",
  styleUrl: "./projects.component.scss",
  imports: [
    RouterLink,
    DatePipe,
    ButtonModule,
    MenuModule,
    PaginatorModule,
    SkeletonModule,
    TagModule,
    TableModule,
    ToastModule,
    PageHeader,
    SectionPanel,
    StatePanel,
    CreateProjectDialogComponent,
    EditProjectDialogComponent,
  ],
})
export default class ProjectsComponent implements OnInit {
  readonly projects = signal<Project[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly organizationId = signal<number | null>(null);
  readonly workspaceName = signal<string | null>(null);
  readonly createDialogOpen = signal(false);
  readonly editDialogOpen = signal(false);
  readonly selectedProject = signal<Project | null>(null);
  readonly totalItems = signal(0);
  readonly rows = signal(20);
  readonly first = signal(0);
  readonly viewMode = signal<"cards" | "table">("cards");

  private readonly activeOrganizationService = inject(
    ActiveOrganizationService,
  );
  private readonly projectService = inject(ProjectService);
  private readonly messageService = inject(MessageService);
  private lastOrganizationId: number | null = null;

  constructor() {
    effect(() => {
      const activeOrganization =
        this.activeOrganizationService.activeOrganization();
      const isOrganizationLoading = this.activeOrganizationService.isLoading();
      const organizationErrorMessage =
        this.activeOrganizationService.errorMessage();

      if (isOrganizationLoading) {
        this.clearProjects();
        this.isLoading.set(true);
        this.errorMessage.set(null);
        return;
      }

      if (!activeOrganization) {
        this.lastOrganizationId = null;
        this.organizationId.set(null);
        this.workspaceName.set(null);
        this.clearProjects();
        this.isLoading.set(false);
        this.errorMessage.set(
          organizationErrorMessage ??
            "No workspace selected. Create or join a workspace to manage projects.",
        );
        return;
      }

      const organizationChanged =
        this.lastOrganizationId !== activeOrganization.organizationId;
      this.lastOrganizationId = activeOrganization.organizationId;
      this.organizationId.set(activeOrganization.organizationId);
      this.workspaceName.set(
        activeOrganization.workspace.name ??
          activeOrganization.workspace.slug ??
          "Current workspace",
      );

      if (organizationChanged) {
        this.closeCreateDialog();
        this.closeEditDialog();
        this.clearProjects();
        this.first.set(0);
        untracked(() => this.loadProjects());
      }
    });
  }

  ngOnInit(): void {
    this.activeOrganizationService.loadOrganizations();
  }

  loadProjects(
    event?: TableLazyLoadEvent | { first?: number; rows?: number },
  ): void {
    const organizationId = this.organizationId();
    if (organizationId == null) {
      return;
    }

    const rows = event?.rows ?? this.rows();
    const first = event?.first ?? this.first();
    const page = Math.floor(first / rows);
    const tableEvent = event && "sortField" in event ? event : undefined;
    const sortFieldValue = tableEvent?.sortField;
    const sortField = Array.isArray(sortFieldValue)
      ? sortFieldValue[0]
      : sortFieldValue;
    const sortDirection = tableEvent?.sortOrder === 1 ? "asc" : "desc";
    const sort = `${sortField ?? "id"},${sortDirection}`;

    this.rows.set(rows);
    this.first.set(first);
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.projectService
      .query(organizationId, page, rows, sort)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.projects.set(response.body ?? []);
          this.totalItems.set(
            Number(
              response.headers.get("X-Total-Count") ??
                response.body?.length ??
                0,
            ),
          );
        },
        error: (error) => {
          this.projects.set([]);
          this.errorMessage.set(
            extractNexaFlowErrorMessage(error, "Projects could not be loaded."),
          );
        },
      });
  }

  openCreateDialog(): void {
    this.createDialogOpen.set(true);
  }

  closeCreateDialog(): void {
    this.createDialogOpen.set(false);
  }

  onProjectCreated(): void {
    this.loadProjects();
  }

  openEditDialog(project: Project): void {
    this.selectedProject.set(project);
    this.editDialogOpen.set(true);
  }

  closeEditDialog(): void {
    this.editDialogOpen.set(false);
    this.selectedProject.set(null);
  }

  onProjectUpdated(): void {
    this.loadProjects();
  }

  setViewMode(viewMode: "cards" | "table"): void {
    this.viewMode.set(viewMode);
  }

  getProjectInitials(project: Project): string {
    const label = project.name ?? "Project";
    return label
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join("");
  }

  getProjectMenuItems(project: Project): MenuItem[] {
    const archived = project.status === "ARCHIVED";
    return [
      {
        label: "Edit",
        icon: "pi pi-pencil",
        disabled: archived,
        command: () => this.openEditDialog(project),
      },
      {
        label: archived ? "Restore" : "Archive",
        icon: archived ? "pi pi-refresh" : "pi pi-box",
        command: () => this.toggleArchive(project),
      },
      {
        label: "Delete",
        icon: "pi pi-trash",
        command: () => this.deleteProject(project),
      },
    ];
  }

  archiveProject(project: Project): void {
    if (project.id == null || project.status === "ARCHIVED") {
      return;
    }

    this.projectService.archive(project.id).subscribe({
      next: (archivedProject) => {
        this.messageService.add({
          severity: "success",
          summary: "Project archived",
          detail: archivedProject.name ?? project.name ?? "Project archived.",
        });
        this.loadProjects();
      },
      error: (error) => {
        this.messageService.add({
          severity: "error",
          summary: "Project not archived",
          detail: extractNexaFlowErrorMessage(
            error,
            "Project could not be archived.",
          ),
        });
      },
    });
  }

  unarchiveProject(project: Project): void {
    if (project.id == null || project.status !== "ARCHIVED") {
      return;
    }

    this.projectService.unarchive(project.id).subscribe({
      next: (restoredProject) => {
        this.messageService.add({
          severity: "success",
          summary: "Project restored",
          detail: restoredProject.name ?? project.name ?? "Project restored.",
        });
        this.loadProjects();
      },
      error: (error) => {
        this.messageService.add({
          severity: "error",
          summary: "Project not restored",
          detail: extractNexaFlowErrorMessage(
            error,
            "Project could not be restored.",
          ),
        });
      },
    });
  }

  toggleArchive(project: Project): void {
    if (project.status === "ARCHIVED") {
      this.unarchiveProject(project);
    } else {
      this.archiveProject(project);
    }
  }

  deleteProject(project: Project): void {
    if (project.id == null) {
      return;
    }

    const confirmed = window.confirm(
      `Delete project "${project.name ?? project.id}"? This also deletes its tasks and comments.`,
    );
    if (!confirmed) {
      return;
    }

    this.projectService.delete(project.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: "success",
          summary: "Project deleted",
          detail: project.name ?? "Project deleted.",
        });
        this.loadProjects();
      },
      error: (error) => {
        this.messageService.add({
          severity: "error",
          summary: "Project not deleted",
          detail: extractNexaFlowErrorMessage(
            error,
            "Project could not be deleted.",
          ),
        });
      },
    });
  }

  getStatusSeverity(
    status?: ProjectStatus,
  ):
    | "success"
    | "info"
    | "secondary"
    | "warn"
    | "danger"
    | "contrast"
    | undefined {
    switch (status) {
      case "ACTIVE":
        return "success";
      case "COMPLETED":
        return "info";
      case "ARCHIVED":
        return "secondary";
      default:
        return undefined;
    }
  }

  getStatusLabel(status?: ProjectStatus): string {
    switch (status) {
      case "ACTIVE":
        return "Active";
      case "COMPLETED":
        return "Completed";
      case "ARCHIVED":
        return "Archived";
      default:
        return "Unknown";
    }
  }

  trackProject(_index: number, project: Project): number | undefined {
    return project.id;
  }

  private clearProjects(): void {
    this.projects.set([]);
    this.totalItems.set(0);
  }
}
