export interface UploadSession {
  transferId: string;
  uploadUrl: string;
  uploadUrlExpiresAt: string;
  downloadToken: string;
  fileName: string;
  fileSize: number;
  fileLastModified: number;
  blockSizeBytes: number;
  completedBlockIndexes: number[];
  createdAt: string;
  updatedAt: string;
}
