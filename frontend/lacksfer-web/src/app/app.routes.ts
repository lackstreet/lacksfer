import { Routes } from '@angular/router';
import { UploadPage } from './features/transfers/pages/upload-page/upload-page';
import { DownloadPage } from './features/transfers/pages/download-page/download-page';

export const routes: Routes = [
  {
    path: '',
    component: UploadPage,
  },
  {
    path: 'download/:token',
    component: DownloadPage,
  },
];
