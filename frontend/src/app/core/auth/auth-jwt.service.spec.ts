import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

import { StateStorageService } from './state-storage.service';

describe('Auth JWT', () => {
  let service: AuthServerProvider;
  let httpMock: HttpTestingController;
  let mockStorageService: StateStorageService;

  beforeEach(() => {
    mockStorageService = {
      getAuthenticationToken: vitest.fn(),
      storeAuthenticationToken: vitest.fn(),
      clearAuthenticationToken: vitest.fn(),
    } as unknown as StateStorageService;

    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting(), { provide: StateStorageService, useValue: mockStorageService }],
    });

    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AuthServerProvider);
  });

  describe('Get Token', () => {
    it('should return empty token if not found in local storage nor session storage', () => {
      const result = service.getToken();
      expect(result).toEqual('');
    });

    it('should return token from session storage if local storage is empty', () => {
      mockStorageService.getAuthenticationToken = vitest.fn(() => 'sessionStorageToken');
      const result = service.getToken();
      expect(result).toEqual('sessionStorageToken');
    });

    it('should return token from localstorage storage', () => {
      mockStorageService.getAuthenticationToken = vitest.fn(() => 'localStorageToken');
      const result = service.getToken();
      expect(result).toEqual('localStorageToken');
    });
  });

  describe('Login', () => {
    it('should clear session storage and save in local storage when rememberMe is true', () => {
      // GIVEN
      mockStorageService.storeAuthenticationToken = vitest.fn();

      // WHEN
      service.login({ username: 'John', password: '123', rememberMe: true }).subscribe();
      httpMock.expectOne('api/authenticate').flush({ id_token: '1' });

      // THEN
      httpMock.verify();
      expect(mockStorageService.storeAuthenticationToken).toHaveBeenCalledWith('1', true);
    });

    it('should clear local storage and save in session storage when rememberMe is false', () => {
      // GIVEN
      mockStorageService.storeAuthenticationToken = vitest.fn();

      // WHEN
      service.login({ username: 'John', password: '123', rememberMe: false }).subscribe();
      httpMock.expectOne('api/authenticate').flush({ id_token: '1' });

      // THEN
      httpMock.verify();
      expect(mockStorageService.storeAuthenticationToken).toHaveBeenCalledWith('1', false);
    });
  });

  describe('Logout', () => {
    it('should clear storage', () => {
      // GIVEN
      mockStorageService.clearAuthenticationToken = vitest.fn();

      // WHEN
      service.logout().subscribe();

      // THEN
      expect(mockStorageService.clearAuthenticationToken).toHaveBeenCalled();
    });
  });
});
