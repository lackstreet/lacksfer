import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-download-page',
  imports: [],
  templateUrl: './download-page.html',
  styleUrl: './download-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DownloadPage {
  private readonly route = inject(ActivatedRoute);

  readonly downloadToken = this.route.snapshot.paramMap.get('token');

  readonly downloadUrl = computed(() => {
    return this.downloadToken ? `/api/transfers/${this.downloadToken}/download` : null;
  });
}
