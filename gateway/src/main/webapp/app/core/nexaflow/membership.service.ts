import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Membership } from './nexaflow.model';

@Injectable({ providedIn: 'root' })
export class MembershipService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/memberships', 'user-service');

  query(): Observable<Membership[]> {
    // TODO: Confirm whether the backend returns a plain array or a paged response before binding this to UI.
    return this.http.get<Membership[]>(this.resourceUrl);
  }
}
