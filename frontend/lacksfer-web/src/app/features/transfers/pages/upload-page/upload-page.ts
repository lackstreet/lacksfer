import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransferApiService } from '../../services/transfer-api.service';
import { finalize, map, startWith, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-upload-page',
  imports: [ReactiveFormsModule],
  templateUrl: './upload-page.html',
  styleUrl: './upload-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UploadPage {
  private readonly transferApi = inject(TransferApiService);
  readonly selectedFile = signal<File | null>(null);
  readonly isUploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly downloadToken = signal<string | null>(null);

  readonly uploadForm = new FormGroup({
    expiresAt: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  readonly isFormValid = toSignal(
    this.uploadForm.statusChanges.pipe(
      startWith(this.uploadForm.status),
      map(() => this.uploadForm.valid),
    ),
    { initialValue: this.uploadForm.valid },
  );

  readonly canSubmit = computed(() => {
    return this.selectedFile() !== null && this.isFormValid() && !this.isUploading();
  });

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.selectedFile.set(file);
    this.errorMessage.set(null);
    this.downloadToken.set(null);
  }

  submit(): void {
    if (!this.canSubmit()) {
      this.errorMessage.set('Select a file and expiration date.');
      return;
    }

    const file = this.selectedFile();
    const expiresAt = this.uploadForm.controls.expiresAt.value;

    if (!file) {
      this.errorMessage.set('No file selected.');
      return;
    }

    this.isUploading.set(true);
    this.errorMessage.set(null);
    this.downloadToken.set(null);

    this.transferApi.startDirectUpload(file.name, expiresAt)
      .pipe(
        switchMap((startResponse) =>
          this.transferApi.uploadToBlob(startResponse.uploadUrl, file).pipe(
            switchMap(() => this.transferApi.completeDirectUpload(startResponse.transferId)),
          )
        ),
        finalize(() => this.isUploading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.downloadToken.set(response.downloadToken);
        },
        error: () => {
          this.errorMessage.set('Upload failed. Try again.');
        },
      });
  }
}
