import { describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ThemesComponent } from './themes.component';

describe('ThemesComponent', () => {
  it('should create', async () => {
    await TestBed.configureTestingModule({
      imports: [ThemesComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(ThemesComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
  });
});
