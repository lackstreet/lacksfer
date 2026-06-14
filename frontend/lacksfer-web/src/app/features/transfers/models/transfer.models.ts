export type TransferStatus = 'PENDING_UPLOAD' | 'READY';

export interface StartDirectUploadResponse {
  transferId: string;
  fileName: string;
  downloadToken: string;
  uploadUrl: string;
  uploadUrlExpiresAt: string;
}

export interface CompleteTransferUploadResponse {
  transferId: string;
  fileName: string;
  downloadToken: string;
  status: TransferStatus;
}

export interface GetUploadedBlocksResponse {
  transferId: string;
  uploadedBlockIndexes: number[];
}

export type UploadStatus =
  | 'idle'
  | 'creating'
  | 'uploading'
  | 'completing'
  | 'ready'
  | 'error';


