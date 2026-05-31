import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransferApiService } from '../../services/transfer-api.service';
import { filter, finalize, map, startWith, switchMap, tap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { UploadStatus } from '../../models/transfer.models';
import { HttpEventType } from '@angular/common/http';

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
  readonly uploadStatus = signal<UploadStatus>('idle');
  readonly uploadProgress = signal(0);

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
    this.uploadStatus.set('idle');
    this.uploadProgress.set(0);
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
    this.uploadStatus.set('creating');
    this.uploadProgress.set(0);

    this.transferApi
      .startDirectUpload(file.name, expiresAt)
      .pipe(
        switchMap((startResponse) =>
          this.transferApi.uploadToBlob(startResponse.uploadUrl, file).pipe(
            tap((event) => {
              this.uploadStatus.set('uploading');

              if (event.type === HttpEventType.UploadProgress && event.total) {
                const progress = Math.round((event.loaded / event.total) * 100);
                this.uploadProgress.set(progress);
              }
            }),
            filter((event) => event.type === HttpEventType.Response),
            switchMap(() => {
              this.uploadStatus.set('completing');
              return this.transferApi.completeDirectUpload(startResponse.transferId);
            }),
          ),
        ),
        finalize(() => this.isUploading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.downloadToken.set(response.downloadToken);
          this.uploadStatus.set('ready');
          this.uploadProgress.set(100);
        },
        error: () => {
          this.errorMessage.set('Upload failed. Try again.');
          this.uploadStatus.set('error');
        },
      });
  }
}
