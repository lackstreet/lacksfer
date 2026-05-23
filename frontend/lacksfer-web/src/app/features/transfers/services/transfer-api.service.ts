import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UploadTransferResponse } from '../models/transfer.models';
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
}
