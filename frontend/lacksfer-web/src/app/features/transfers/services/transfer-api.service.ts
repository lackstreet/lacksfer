import { inject, Injectable } from '@angular/core';
import { HttpEvent, HttpClient } from '@angular/common/http';
import { UploadTransferResponse, CompleteTransferUploadResponse, StartDirectUploadResponse } from '../models/transfer.models';
import { Observable } from 'rxjs';
import { createBlockId } from '../utils/block-id';

@Injectable({
  providedIn: 'root',
})
export class TransferApiService {
  private readonly http = inject(HttpClient);

  upload(file: File, expiresAt: string): Observable<UploadTransferResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('expiresAt', new Date(expiresAt).toISOString());

    return this.http.post<UploadTransferResponse>('/api/transfers/upload', formData);
  }

  startDirectUpload(fileName: string, expiresAt: string) : Observable<StartDirectUploadResponse> {
    return this.http.post<StartDirectUploadResponse>('/api/transfers', {
      fileName,
      expiresAt: new Date(expiresAt).toISOString(),
    });
  }

  uploadToBlob(uploadUrl: string, file: File): Observable<HttpEvent<void>> {
    return this.http.put<void>(uploadUrl, file, {
      headers: {
        'x-ms-blob-type': 'BlockBlob',
      },
      observe: 'events',
      reportProgress: true,
    });
  }

  completeDirectUpload(transferId: string): Observable<CompleteTransferUploadResponse> {
    return this.http.post<CompleteTransferUploadResponse>(`/api/transfers/${transferId}/complete`, {});
  }

  uploadBlock(uploadUrl: string, blockIndex: number, block: Blob): Observable<HttpEvent<void>> {
    const blockId = encodeURIComponent(createBlockId(blockIndex));
    const blockUrl = `${uploadUrl}&comp=block&blockid=${blockId}`;

    return this.http.put<void>(blockUrl, block, {
      observe: 'events',
      reportProgress: true,
    });
  }

  commitBlockList(uploadUrl: string, blockIndexes: number[]): Observable<void> {
    const blockListUrl = `${uploadUrl}&comp=blocklist`;

    const blockListXml = `<?xml version="1.0" encoding="utf-8"?>
    <BlockList>
    ${blockIndexes.map((index) => `  <Latest>${createBlockId(index)}</Latest>`).join('\n')}
    </BlockList>`;

    return this.http.put<void>(blockListUrl, blockListXml, {
      headers: {
        'Content-Type': 'application/xml',
      },
    });
  }
}
