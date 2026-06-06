import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type StatePanelTone = 'loading' | 'empty' | 'error';

@Component({
  selector: 'jhi-state-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './state-panel.html',
  styleUrl: './state-panel.scss',
})
export default class StatePanel {
  readonly tone = input<StatePanelTone>('empty');
  readonly title = input.required<string>();
  readonly description = input<string>();
}
