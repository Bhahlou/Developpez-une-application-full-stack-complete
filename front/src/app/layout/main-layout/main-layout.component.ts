import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [RouterOutlet, HeaderComponent],
})
export class MainLayoutComponent {}
