import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ThemeSelectComponent } from './theme-select.component';

describe('ThemeSelectComponent', () => {
  let component: ThemeSelectComponent;
  let fixture: ComponentFixture<ThemeSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ThemeSelectComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ThemeSelectComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('themes', [
      { id: 1, title: 'Backend', description: 'desc', subscribed: false },
    ]);
    fixture.componentRef.setInput('value', '');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('updates the value when a theme is selected', () => {
    component['onSelectionChange']('1');

    expect(component.value()).toBe('1');
  });

  it('emits touch when the select closes', () => {
    let touched = false;
    component.touch.subscribe(() => (touched = true));

    component['onOpenedChange'](false);

    expect(touched).toBe(true);
  });

  it('does not emit touch when the select opens', () => {
    let touched = false;
    component.touch.subscribe(() => (touched = true));

    component['onOpenedChange'](true);

    expect(touched).toBe(false);
  });

  it('emits touch on blur, even if the select was never opened', () => {
    let touched = false;
    component.touch.subscribe(() => (touched = true));

    component['markTouched']();

    expect(touched).toBe(true);
  });

  it('forces the mat-select error state only once touched and invalid', () => {
    fixture.componentRef.setInput('touched', false);
    fixture.componentRef.setInput('invalid', true);
    fixture.detectChanges();
    expect(component['matSelect']().errorState).toBe(false);

    fixture.componentRef.setInput('touched', true);
    fixture.detectChanges();
    expect(component['matSelect']().errorState).toBe(true);
  });
});
