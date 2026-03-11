import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <h1>{{projectName | pascal-case}}</h1>
    <p>Welcome to {{projectName | pascal-case}}.</p>
  `,
  styles: [`
    :host {
      display: block;
      max-width: 1280px;
      margin: 0 auto;
      padding: 2rem;
      text-align: center;
    }
  `]
})
export class AppComponent {
  title = '{{projectName | kebab-case}}';
}
