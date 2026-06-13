import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  finalize,
  map,
  startWith,
} from 'rxjs';

import { toSignal } from '@angular/core/rxjs-interop';
import { UploadStatus } from '../../models/transfer.models';
import { UploadSessionStoreService } from '../../services/upload-session-store.service';
import { UploadSession } from '../../models/upload-session.models';
import { BlockUploadService } from '../../services/block-upload.service';
import { TransferApiService } from '../../services/transfer-api.service';

@Component({
  selector: 'app-upload-page',
  imports: [ReactiveFormsModule],
  templateUrl: './upload-page.html',
  styleUrl: './upload-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UploadPage {
  private readonly uploadSessionStore = inject(UploadSessionStoreService);
  private readonly blockUploadService = inject(BlockUploadService);
  private readonly transferApiService = inject(TransferApiService);


  readonly selectedFile = signal<File | null>(null);
  readonly isUploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly downloadToken = signal<string | null>(null);
  readonly uploadStatus = signal<UploadStatus>('idle');
  readonly uploadProgress = signal(0);
  readonly resumableSession = signal<UploadSession | null>(null);

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
    this.resumableSession.set(null);

    if (file) {
      void this.uploadSessionStore.findByFile(file).then((session) => {
        if (!session) {
          this.resumableSession.set(null);
          return;
        }

        const uploadUrlExpired = new Date(session.uploadUrlExpiresAt) <= new Date();

        if (uploadUrlExpired) {
          this.transferApiService.refreshDirectUploadUrl(session.transferId).subscribe({
            next: (response) => {
             const refreshedSession: UploadSession = {
               ...session,
               uploadUrl: response.uploadUrl,
               uploadUrlExpiresAt: response.uploadUrlExpiresAt,
               updatedAt: new Date().toISOString(),
             };

             void this.uploadSessionStore.save(refreshedSession);
             this.resumableSession.set(refreshedSession);
            },
            error: () => {
              void this.uploadSessionStore.remove(session.transferId);
              this.resumableSession.set(null);
              this.errorMessage.set('Resume session expired. Please start a new upload.');
            },
          });

          return;
        }

        this.resumableSession.set(session);
      });
    }
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

    const existingSession = this.resumableSession();

    this.isUploading.set(true);
    this.errorMessage.set(null);
    this.downloadToken.set(null);
    this.uploadProgress.set(0);


    this.blockUploadService
      .upload(file, expiresAt, existingSession)
      .pipe(finalize(() => this.isUploading.set(false)))
      .subscribe({
        next: (event) => {
          if (event.type === 'creating') {
            this.uploadStatus.set('creating');
          }

          if (event.type === 'uploading') {
            this.uploadStatus.set('uploading');
            this.uploadProgress.set(event.progress);
          }

          if (event.type === 'completing') {
            this.uploadStatus.set('completing');
          }

          if (event.type === 'ready') {
            this.downloadToken.set(event.downloadToken);
            this.uploadStatus.set('ready');
            this.uploadProgress.set(100);
          }

          if (event.type === 'error') {
            this.errorMessage.set(event.message);
            this.uploadStatus.set('error');
          }
        },
        error: () => {
          this.errorMessage.set('Upload failed. Try again.');
          this.uploadStatus.set('error');
        },
      });
  }
}
