export type WorkspaceRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED' | 'REJECTED';

export interface Workspace {
  id?: number;
  organizationId?: number;
  name?: string;
  slug?: string;
  description?: string | null;
  role?: string;
  active?: boolean;
  createdAt?: string;
  createdDate?: string;
  updatedAt?: string | null;
  lastModifiedDate?: string | null;
}

export interface Membership {
  id?: number;
  membershipId?: number;
  userId?: number;
  userLogin?: string;
  userEmail?: string;
  email?: string;
  login?: string;
  role?: string;
  joinedAt?: string;
  createdDate?: string;
  active?: boolean;
  status?: string;
  organization?: Workspace;
  organizationId?: number;
}

export interface Invitation {
  id?: number;
  invitationId?: number;
  organizationId?: number;
  organizationName?: string;
  organizationSlug?: string;
  email?: string;
  userEmail?: string;
  userLogin?: string;
  token?: string;
  role?: string;
  status?: string;
  invitedAt?: string;
  createdDate?: string;
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
