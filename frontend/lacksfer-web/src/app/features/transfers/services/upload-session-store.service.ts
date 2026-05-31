import { Injectable } from '@angular/core';
import { UploadSession } from '../models/upload-session.models';

const DB_NAME = 'lacksfer';
const DB_VERSION = 1;
const STORE_NAME = 'upload_sessions';

@Injectable({
  providedIn: 'root',
})
export class UploadSessionStoreService {
  private openDatabase(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {

      const request = indexedDB.open(DB_NAME, DB_VERSION);

      request.onupgradeneeded = () => {
        const database = request.result;

        if(!database.objectStoreNames.contains(STORE_NAME)) {
          database.createObjectStore(STORE_NAME, { keyPath: 'transferId'});
        }
      };

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  async save(session: UploadSession): Promise<void> {
    const database = await this.openDatabase();

    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORE_NAME, 'readwrite');
      const store = transaction.objectStore(STORE_NAME);

      store.put(session);

      transaction.oncomplete = () => resolve();
      transaction.onerror = () => reject(transaction.error);
    });
  }

  async get(transferId: string): Promise<UploadSession | undefined> {
    const database = await this.openDatabase();

    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORE_NAME, 'readonly');
      const store = transaction.objectStore(STORE_NAME);
      const request = store.get(transferId);

      request.onsuccess = () => resolve(request.result as UploadSession | undefined);
      request.onerror = () => reject(request.error);
    });
  }

  async remove(transferId: string): Promise<void> {
    const database = await this.openDatabase();

    return new Promise((resolve, reject) => {
      const transaction = database.transaction(STORE_NAME, 'readwrite');
      const store = transaction.objectStore(STORE_NAME);

      store.delete(transferId);

      transaction.oncomplete = () => resolve();
      transaction.onerror = () => reject(transaction.error);
    });
  }

}
