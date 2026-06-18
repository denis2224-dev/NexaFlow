import { beforeEach, describe, expect, it } from 'vitest';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';

import Home from './home';

describe('Home Component', () => {
  let comp: Home;
  let fixture: ComponentFixture<Home>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        {
          provide: AccountService,
          useValue: {
            account: signal(null),
          },
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Home);
    comp = fixture.componentInstance;
  });

  it('should expose the current account', () => {
    expect(comp.account()).toBeNull();
  });
});
