import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuarantineService, QuarantinedTest } from './quarantine.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="stats-grid">
      <div class="stat-card card fade-in">
        <h3 class="stat-title">TOTAL QUARANTINED TESTS</h3>
        <div class="stat-value">{{ totalElements }}</div>
      </div>
    </div>

    <div class="fade-in" style="margin-bottom: 1rem; display: flex; gap: 1rem;">
      <input type="text" [(ngModel)]="searchQuery" (keyup.enter)="onSearch()" placeholder="Search test name or suite..." class="form-input" style="flex: 1; padding: 0.75rem; border-radius: 6px; border: 1px solid var(--card-border); background: var(--bg-color); color: var(--text-primary);" />
      <button (click)="onSearch()" class="btn">Search</button>
    </div>

    <div class="table-container fade-in" style="animation-delay: 0.1s;">
      <table *ngIf="quarantinedTests.length > 0; else emptyState">
        <thead>
          <tr>
            <th>Test Case</th>
            <th>Suite</th>
            <th>Flake Score</th>
            <th>Quarantined At</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let test of quarantinedTests">
            <td style="color: var(--danger-color); font-weight: 500;">
              <span class="status-indicator"></span>
              {{ test.testIdentifier }}
            </td>
            <td style="color: var(--text-secondary);">{{ test.suiteName }}</td>
            <td style="font-weight: 600;">{{ test.flakeScore }}</td>
            <td style="color: var(--text-secondary);">{{ formatDate(test.quarantinedAt) }}</td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="quarantinedTests.length > 0" style="display: flex; justify-content: space-between; padding: 1rem; border-top: 1px solid var(--card-border); align-items: center;">
        <span style="color: var(--text-secondary)">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <div style="display: flex; gap: 0.5rem;">
          <button [disabled]="currentPage === 0" (click)="changePage(currentPage - 1)" class="btn">Previous</button>
          <button [disabled]="currentPage >= totalPages - 1" (click)="changePage(currentPage + 1)" class="btn">Next</button>
        </div>
      </div>

      <ng-template #emptyState>
        <div class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="color: var(--text-secondary); margin-bottom: 16px;">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3>All Clear!</h3>
          <p>No tests are currently quarantined for this search.</p>
        </div>
      </ng-template>
    </div>
  `
})
export class DashboardComponent implements OnInit, OnDestroy {
  quarantineService = inject(QuarantineService);
  quarantinedTests: QuarantinedTest[] = [];
  
  currentPage: number = 0;
  pageSize: number = 20;
  totalElements: number = 0;
  totalPages: number = 0;
  searchQuery: string = '';

  sseSubscription: any;

  ngOnInit() {
    this.loadData();
    
    // Subscribe to real-time quarantine events via SSE
    this.sseSubscription = this.quarantineService.listenForQuarantineEvents().subscribe({
      next: (newTest) => {
        // Only prepend if we are on the first page and not searching, or if it matches search
        if (this.currentPage === 0 && (!this.searchQuery || newTest.testIdentifier.includes(this.searchQuery) || newTest.suiteName.includes(this.searchQuery))) {
           this.quarantinedTests.unshift(newTest);
           if (this.quarantinedTests.length > this.pageSize) {
             this.quarantinedTests.pop();
           }
           this.totalElements++;
        }
      }
    });
  }

  ngOnDestroy() {
    if (this.sseSubscription) {
      this.sseSubscription.unsubscribe();
    }
  }

  loadData() {
    this.quarantineService.getQuarantinedTests(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: data => {
        this.quarantinedTests = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
      },
      error: err => {
        console.error('Failed to load data:', err);
      }
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadData();
  }

  changePage(newPage: number) {
    this.currentPage = newPage;
    this.loadData();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleString();
  }
}
