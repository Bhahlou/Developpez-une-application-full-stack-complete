import { Component, ChangeDetectionStrategy, effect, input, model, output, viewChild } from '@angular/core';
import { FormValueControl, ValidationError } from '@angular/forms/signals';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { ThemeResponse } from '../../../../core/models';

@Component({
  selector: 'app-theme-select',
  templateUrl: './theme-select.component.html',
  styleUrls: ['./theme-select.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatFormFieldModule, MatSelectModule],
})
export class ThemeSelectComponent implements FormValueControl<string> {
  readonly themes = input.required<ThemeResponse[]>();

  readonly value = model.required<string>();
  readonly touched = input(false);
  readonly invalid = input(false);
  readonly required = input(false);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly touch = output<void>();

  private readonly matSelect = viewChild.required(MatSelect);

  constructor() {
    // MatSelect only recomputes its own error-state highlighting from an NgControl
    // (ngDoCheck → `if (ngControl) this.updateErrorState()`), which we don't have here
    // since this is bound via Signal Forms, not Reactive/Template forms. Set it directly.
    effect(() => {
      this.matSelect().errorState = this.touched() && this.invalid();
    });
  }

  protected onSelectionChange(themeId: string): void {
    this.value.set(themeId);
  }

  protected onOpenedChange(opened: boolean): void {
    if (!opened) {
      this.touch.emit();
    }
  }

  protected markTouched(): void {
    this.touch.emit();
  }
}
