import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransferApiService } from '../../services/transfer-api.service';
import {
  toArray,
  filter,
  finalize,
  from,
  map,
  mergeMap,
  of,
  retry,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { toSignal } from '@angular/core/rxjs-interop';
import { UploadStatus } from '../../models/transfer.models';
import { HttpEventType } from '@angular/common/http';
import { splitFileIntoBlocks } from '../../utils/file-blocks';
import { UploadSessionStoreService } from '../../services/upload-session-store.service';
import { UploadSession } from '../../models/upload-session.models';

const BLOCK_SIZE_BYTES = 10 * 1024 * 1024;
const PARALLEL_UPLOADS = 3;
const BLOCK_UPLOAD_RETRY_COUNT = 3;
const BLOCK_UPLOAD_RETRY_DELAY_MS = 1000;

@Component({
  selector: 'app-upload-page',
  imports: [ReactiveFormsModule],
  templateUrl: './upload-page.html',
  styleUrl: './upload-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UploadPage {
  private readonly transferApi = inject(TransferApiService);
  private readonly uploadSessionStore = inject(UploadSessionStoreService);

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
        this.resumableSession.set(session ?? null);
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

    const blocks = splitFileIntoBlocks(file, BLOCK_SIZE_BYTES);
    const blockIndexes = blocks.map((block) => block.index);
    const existingSession = this.resumableSession();
    const completedBlockIndexes = existingSession?.completedBlockIndexes ?? [];
    const completedBlockIndexSet = new Set(completedBlockIndexes);
    const blocksToUpload = blocks.filter((block) => !completedBlockIndexSet.has(block.index));
    let completedBlockCount = completedBlockIndexes.length;

    this.isUploading.set(true);
    this.errorMessage.set(null);
    this.downloadToken.set(null);
    this.uploadStatus.set('creating');
    this.uploadProgress.set(0);

    const startUpload$ = existingSession
      ? of({
          transferId: existingSession.transferId,
          uploadUrl: existingSession.uploadUrl,
          downloadToken: existingSession.downloadToken,
        })
      : this.transferApi.startDirectUpload(file.name, expiresAt);

    startUpload$
      .pipe(
        switchMap((startResponse) => {
          const now = new Date().toISOString();

          const session: UploadSession = existingSession ?? {
            transferId: startResponse.transferId,
            uploadUrl: startResponse.uploadUrl,
            downloadToken: startResponse.downloadToken,
            fileName: file.name,
            fileSize: file.size,
            fileLastModified: file.lastModified,
            blockSizeBytes: BLOCK_SIZE_BYTES,
            completedBlockIndexes: [],
            createdAt: now,
            updatedAt: now,
          };


          return from(this.uploadSessionStore.save(session)).pipe(
            switchMap(() =>
              from(blocksToUpload).pipe(
                mergeMap(
                  (block) =>
                    this.transferApi
                      .uploadBlock(startResponse.uploadUrl, block.index, block.blob)
                      .pipe(
                        retry({
                          count: BLOCK_UPLOAD_RETRY_COUNT,
                          delay: BLOCK_UPLOAD_RETRY_DELAY_MS,
                        }),
                        tap((event) => {
                          this.uploadStatus.set('uploading');

                          if (event.type === HttpEventType.Response) {
                            completedBlockCount++;

                            session.completedBlockIndexes = [
                              ...session.completedBlockIndexes,
                              block.index,
                            ];
                            session.updatedAt = new Date().toISOString();

                            void this.uploadSessionStore.save(session);

                            const progress = Math.round(
                              (completedBlockCount / blocks.length) * 100,
                            );

                            this.uploadProgress.set(progress);
                          }
                        }),
                        filter((event) => event.type === HttpEventType.Response),
                      ),
                  PARALLEL_UPLOADS,
                ),
                toArray(),
                switchMap(() =>
                  this.transferApi.commitBlockList(startResponse.uploadUrl, blockIndexes),
                ),

                switchMap(() => {
                  this.uploadStatus.set('completing');

                  return this.transferApi.completeDirectUpload(startResponse.transferId);
                }),
              ),
            ),
          );
        }),

        finalize(() => this.isUploading.set(false)),
      )
      .subscribe({
        next: (response) => {
          this.downloadToken.set(response.downloadToken);
          this.uploadStatus.set('ready');
          this.uploadProgress.set(100);

          void this.uploadSessionStore.remove(response.transferId);
        },

        error: () => {
          this.errorMessage.set('Upload failed. Try again.');
          this.uploadStatus.set('error');
        },
      });
  }
}
