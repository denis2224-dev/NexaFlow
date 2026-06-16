import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'jhi-section-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './section-panel.html',
  styleUrl: './section-panel.scss',
})
export default class SectionPanel {
  readonly title = input<string>();
  readonly description = input<string>();
}
