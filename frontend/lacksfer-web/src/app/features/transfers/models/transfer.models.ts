export interface UploadTransferResponse {
  id: string;
  fileName: string;
  downloadToken: string;
}

export type TransferStatus = 'PENDING_UPLOAD' | 'READY';

export interface StartDirectUploadResponse {
  transferId: string;
  fileName: string;
  downloadToken: string;
  uploadUrl: string;
}

export interface CompleteTransferUploadResponse {
  transferId: string;
  fileName: string;
  downloadToken: string;
  status: TransferStatus;
}
