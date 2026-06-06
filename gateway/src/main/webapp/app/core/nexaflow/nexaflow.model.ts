export type WorkspaceRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED';

export interface Workspace {
  id?: number;
  organizationId?: number;
  name?: string;
  slug?: string;
  description?: string | null;
  role?: WorkspaceRole;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface Membership {
  id?: number;
  userId?: number;
  userLogin?: string;
  userEmail?: string;
  role?: WorkspaceRole;
  joinedAt?: string;
  active?: boolean;
  organization?: Workspace;
  organizationId?: number;
}

export interface Invitation {
  id?: number;
  invitationId?: number;
  organizationId?: number;
  email?: string;
  token?: string;
  role?: WorkspaceRole;
  status?: InvitationStatus;
  invitedAt?: string;
  expiresAt?: string;
  acceptedAt?: string | null;
  invitedByUserId?: number;
  invitedByLogin?: string;
}

export interface WorkspaceCreateRequest {
  name: string;
  slug: string;
  description?: string | null;
}

export interface InvitationCreateRequest {
  email: string;
  role: WorkspaceRole;
}

export interface InvitationAcceptRequest {
  token: string;
}
