import { Component, Input } from '@angular/core';

@Component({
  selector: 'company-grid',
  templateUrl: './company-grid.component.html',
  styleUrls: ['./company-grid.component.css'],
  standalone: true
})
export class CompanyGridComponent {
  @Input() data: any[] = [];
  @Input() columns: string[] = [];
}
