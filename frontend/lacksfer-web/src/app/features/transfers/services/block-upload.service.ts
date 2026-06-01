import { inject, Injectable } from '@angular/core';
import { TransferApiService } from './transfer-api.service';
import { UploadSessionStoreService } from './upload-session-store.service';
import { UploadSession } from '../models/upload-session.models';
import { Observable } from 'rxjs';
import { BlockUploadEvent } from '../models/block-upload.models';

@Injectable({
  providedIn: 'root',
})
export class BlockUploadService {
  private readonly transferApi = inject(TransferApiService);
  private readonly uploadSessionStore = inject(UploadSessionStoreService);

  upload(file: File, expiresAt: string, existingSession: UploadSession | null): Observable<BlockUploadEvent> {
    throw new Error('Method not implemented.');
  }
}
