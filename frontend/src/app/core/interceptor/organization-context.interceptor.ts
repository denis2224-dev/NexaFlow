import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';

const NOTIFICATION_API_PATH = 'services/notificationservice/api/notifications';

export const organizationContextInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes(NOTIFICATION_API_PATH) || req.headers.has('X-Organization-Id')) {
    return next(req);
  }

  const organizationId = inject(ActiveOrganizationService).selectedOrganizationId();
  if (organizationId == null) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-Organization-Id': `${organizationId}`,
      },
    }),
  );
};
