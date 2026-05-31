
export interface UploadSession {
  transferId: string;
  uploadUrl: string;
  downloadToken: string;
  fileName: string;
  fileSize: number;
  fileLastModified: number;
  blockSizeBytes: number;
  completedBlockIndexes: number[];
  createdAt: string;
  updatedAt: string;
}
