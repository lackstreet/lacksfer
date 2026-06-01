import { inject, Injectable } from '@angular/core';
import { HttpEventType } from '@angular/common/http';

import {
  concat,
  filter,
  from,
  map,
  mergeMap,
  Observable,
  of,
  retry,
  switchMap,
  tap,
  toArray,
} from 'rxjs';

import { TransferApiService } from './transfer-api.service';
import { UploadSessionStoreService } from './upload-session-store.service';

import { UploadSession } from '../models/upload-session.models';
import { BlockUploadEvent } from '../models/block-upload.models';

import { splitFileIntoBlocks } from '../utils/file-blocks';

const BLOCK_SIZE_BYTES = 10 * 1024 * 1024;
const PARALLEL_UPLOADS = 3;
const BLOCK_UPLOAD_RETRY_COUNT = 3;
const BLOCK_UPLOAD_RETRY_DELAY_MS = 1000;

@Injectable({
  providedIn: 'root',
})
export class BlockUploadService {
  private readonly transferApi = inject(TransferApiService);
  private readonly uploadSessionStore = inject(UploadSessionStoreService);

  upload(
    file: File,
    expiresAt: string,
    existingSession: UploadSession | null,
  ): Observable<BlockUploadEvent> {
    const blocks = splitFileIntoBlocks(file, BLOCK_SIZE_BYTES);
    const blockIndexes = blocks.map((block) => block.index);

    const completedBlockIndexes = existingSession?.completedBlockIndexes ?? [];

    const completedBlockIndexSet = new Set(completedBlockIndexes);

    const blocksToUpload = blocks.filter((block) => !completedBlockIndexSet.has(block.index));

    let completedBlockCount = completedBlockIndexes.length;

    const startUpload$ = existingSession
      ? of({
          transferId: existingSession.transferId,
          uploadUrl: existingSession.uploadUrl,
          downloadToken: existingSession.downloadToken,
          uploadUrlExpiresAt: existingSession.uploadUrlExpiresAt,
        })
      : this.transferApi.startDirectUpload(file.name, expiresAt);

    return concat(
      of({ type: 'creating' } satisfies BlockUploadEvent),

      startUpload$.pipe(
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
            uploadUrlExpiresAt: startResponse.uploadUrlExpiresAt,
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
                          if (event.type === HttpEventType.Response) {
                            completedBlockCount++;

                            session.completedBlockIndexes = [
                              ...session.completedBlockIndexes,
                              block.index,
                            ];

                            session.updatedAt = new Date().toISOString();

                            void this.uploadSessionStore.save(session);
                          }
                        }),

                        filter((event) => event.type === HttpEventType.Response),

                        map(() => {
                          const progress = Math.round((completedBlockCount / blocks.length) * 100);

                          return {
                            type: 'uploading',
                            progress,
                          } satisfies BlockUploadEvent;
                        }),
                      ),
                  PARALLEL_UPLOADS,
                ),

                toArray(),

                switchMap(() =>
                  concat(
                    of({
                      type: 'completing',
                    } satisfies BlockUploadEvent),

                    this.transferApi.commitBlockList(startResponse.uploadUrl, blockIndexes).pipe(
                      switchMap(() =>
                        this.transferApi.completeDirectUpload(startResponse.transferId),
                      ),

                      tap((response) => {
                        void this.uploadSessionStore.remove(response.transferId);
                      }),

                      map(
                        (response) =>
                          ({
                            type: 'ready',
                            downloadToken: response.downloadToken,
                          }) satisfies BlockUploadEvent,
                      ),
                    ),
                  ),
                ),
              ),
            ),
          );
        }),
      ),
    );
  }
}
