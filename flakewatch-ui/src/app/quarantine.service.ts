import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QuarantinedTest {
  testIdentifier: string;
  suiteName: string;
  flakeScore: number;
  quarantinedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuarantineService {
  private http = inject(HttpClient);

  getQuarantinedTests(page: number = 0, size: number = 20, search: string = ''): Observable<Page<QuarantinedTest>> {
    let url = `/api/v1/quarantine?page=${page}&size=${size}`;
    if (search) {
      url += `&search=${encodeURIComponent(search)}`;
    }
    return this.http.get<Page<QuarantinedTest>>(url);
  }

  listenForQuarantineEvents(): Observable<QuarantinedTest> {
    return new Observable(observer => {
      const eventSource = new EventSource('/api/v1/quarantine/stream');
      
      eventSource.addEventListener('QUARANTINE_ADDED', (event: any) => {
        const test = JSON.parse(event.data);
        observer.next(test);
      });
      
      eventSource.onerror = error => {
        console.error('SSE Error:', error);
        // EventSource will auto-reconnect on error
      };

      return () => eventSource.close();
    });
  }
}
