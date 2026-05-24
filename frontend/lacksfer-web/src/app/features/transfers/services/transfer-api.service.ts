import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UploadTransferResponse, CompleteTransferUploadResponse, StartDirectUploadResponse } from '../models/transfer.models';
import { Observable } from 'rxjs';

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

  uploadToBlob(uploadUrl: string, file: File): Observable<void> {
    return this.http.put<void>(uploadUrl, file, {
      headers: {
        'x-ms-blob-type': 'BlockBlob'
      },
    });
  }

  completeDirectUpload(transferId: string): Observable<CompleteTransferUploadResponse> {
    return this.http.post<CompleteTransferUploadResponse>(`/api/transfers/${transferId}/complete`, {});
  }
}
