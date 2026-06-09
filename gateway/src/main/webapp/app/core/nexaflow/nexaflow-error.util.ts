import { HttpErrorResponse } from '@angular/common/http';

const ERROR_MESSAGES: Record<string, string> = {
  'error.nopermission': 'You do not have permission to perform this action.',
  'error.notmember': 'You are not a member of this workspace.',
  'error.ownernotallowed': 'Owner invitations are not allowed.',
  'error.alreadymember': 'This user is already a member of the workspace.',
  'error.alreadyinvited': 'This user already has a pending invitation.',
  'error.invitationnotfound': 'This invitation is no longer available.',
  'error.invitationnotpending': 'This invitation is no longer pending.',
  'error.invitationexpired': 'This invitation has expired.',
  'error.invitationemailmismatch': 'This invitation was sent to a different email address.',
  'error.usernotauthenticated': 'Please sign in again to continue.',
};

const GENERIC_TITLES = new Set(['Bad Request', 'Unauthorized', 'Forbidden', 'Internal Server Error']);

export function extractNexaFlowErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const payload = error.error as Record<string, unknown> | string | null;

  if (typeof payload === 'string') {
    return sanitizeMessage(payload, fallback);
  }

  const mappedMessage = mapErrorKey(readString(payload, 'message'));
  if (mappedMessage) {
    return mappedMessage;
  }

  const detail = sanitizeMessage(readString(payload, 'detail'), '');
  if (detail) {
    return detail;
  }

  const title = readString(payload, 'title');
  if (title && !GENERIC_TITLES.has(title)) {
    return title;
  }

  return fallback;
}

function mapErrorKey(value: string | null): string | null {
  if (!value) {
    return null;
  }

  return ERROR_MESSAGES[value] ?? null;
}

function readString(payload: Record<string, unknown> | null, key: string): string | null {
  const value = payload?.[key];
  return typeof value === 'string' ? value.trim() : null;
}

function sanitizeMessage(value: string | null, fallback: string): string {
  if (!value) {
    return fallback;
  }

  const trimmedValue = value.trim();
  if (!trimmedValue || trimmedValue.startsWith('error.') || trimmedValue.includes('BAD_REQUEST')) {
    return fallback;
  }

  return trimmedValue;
}
