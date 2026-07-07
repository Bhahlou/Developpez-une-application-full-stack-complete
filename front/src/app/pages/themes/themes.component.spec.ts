import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ThemesComponent } from './themes.component';
import { ThemeService } from '../../core/services/theme.service';
import { SubscriptionService } from '../../core/services/subscription.service';
import { SnackbarService } from '../../core/services/snackbar.service';

describe('ThemesComponent', () => {
  let component: ThemesComponent;
  let fixture: ComponentFixture<ThemesComponent>;
  let themeService: { getAll: ReturnType<typeof vi.fn> };
  let subscriptionService: { subscribe: ReturnType<typeof vi.fn> };
  let snackbar: { success: ReturnType<typeof vi.fn>; showApiError: ReturnType<typeof vi.fn> };
  let dialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    themeService = { getAll: vi.fn().mockReturnValue(of([])) };
    subscriptionService = { subscribe: vi.fn() };
    snackbar = { success: vi.fn(), showApiError: vi.fn() };
    dialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ThemesComponent],
      providers: [
        { provide: ThemeService, useValue: themeService },
        { provide: SubscriptionService, useValue: subscriptionService },
        { provide: SnackbarService, useValue: snackbar },
        { provide: MatDialog, useValue: dialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ThemesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('loads the themes on init', async () => {
    const themes = [{ id: 1, title: 'Backend', description: 'desc', subscribed: false }];
    themeService.getAll.mockReturnValue(of(themes));

    await component['loadThemes']();

    expect(component['themes']()).toEqual(themes);
    expect(component['loading']()).toBe(false);
  });

  it('shows an API error when loading themes fails', async () => {
    const error = new Error('failed');
    themeService.getAll.mockReturnValue(throwError(() => error));

    await component['loadThemes']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(component['loading']()).toBe(false);
  });

  it('opens the create dialog and adds the created theme, sorted by title', () => {
    fixture.detectChanges();
    component['themes'].set([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
    const created = { id: 2, title: 'Angular', description: 'desc2', subscribed: false };
    dialog.open.mockReturnValue({ afterClosed: () => of(created) });

    component['openCreateDialog']();

    expect(component['themes']()).toEqual([
      created,
      { id: 1, title: 'Backend', description: 'desc', subscribed: false },
    ]);
    expect(snackbar.success).toHaveBeenCalledWith('Thème créé.');
  });

  it('does not change the list when the create dialog is closed without a result', () => {
    fixture.detectChanges();
    component['themes'].set([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
    dialog.open.mockReturnValue({ afterClosed: () => of(undefined) });

    component['openCreateDialog']();

    expect(component['themes']()).toEqual([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
    expect(snackbar.success).not.toHaveBeenCalled();
  });

  it('subscribes to a theme and marks only that theme as subscribed', async () => {
    component['themes'].set([
      { id: 1, title: 'Backend', description: 'desc', subscribed: false },
      { id: 2, title: 'Frontend', description: 'desc2', subscribed: false },
    ]);
    subscriptionService.subscribe.mockReturnValue(of(undefined));

    await component['subscribe']({ id: 1, title: 'Backend', description: 'desc', subscribed: false });

    expect(subscriptionService.subscribe).toHaveBeenCalledWith(1);
    expect(component['themes']()).toEqual([
      { id: 1, title: 'Backend', description: 'desc', subscribed: true },
      { id: 2, title: 'Frontend', description: 'desc2', subscribed: false },
    ]);
    expect(snackbar.success).toHaveBeenCalledWith('Abonnement effectué.');
  });

  it('shows an API error when subscribing fails', async () => {
    component['themes'].set([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
    const error = new Error('failed');
    subscriptionService.subscribe.mockReturnValue(throwError(() => error));

    await component['subscribe']({ id: 1, title: 'Backend', description: 'desc', subscribed: false });

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(component['themes']()).toEqual([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
  });
});
