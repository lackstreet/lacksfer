export type BlockUploadEvent =
  | { type: 'creating' }
  | { type: 'uploading'; progress: number }
  | { type: 'completing' }
  | { type: 'ready'; downloadToken: string }
  | { type: 'error'; message: string };
