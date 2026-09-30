import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { environment } from '../../../environments/environment';

describe('DashboardService contract', () => {
  let service: DashboardService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(DashboardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('uses the canonical API base for dashboards and reports', () => {
    service.obtenerEjecutivo().subscribe();
    expect(http.expectOne(`${base}/dashboard/ejecutivo`).request.method).toBe('GET');
    service.obtenerReporte('eventos').subscribe();
    expect(http.expectOne(`${base}/reportes/eventos`).request.method).toBe('GET');
  });

  it('downloads protected exports as binary through HttpClient', () => {
    service.exportarPdf('eventos').subscribe();
    const request = http.expectOne(`${base}/reportes/exportar/pdf?tipo=eventos`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob(['pdf'], { type: 'application/pdf' }));
  });
});
