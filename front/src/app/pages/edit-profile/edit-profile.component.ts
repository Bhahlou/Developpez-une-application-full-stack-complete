import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { Location } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule],
})
export class EditProfileComponent {
  private readonly location = inject(Location);

  protected goBack(): void {
    this.location.back();
  }
}
