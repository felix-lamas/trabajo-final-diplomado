import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';

export type AppTheme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly platformId = inject(PLATFORM_ID);
  readonly theme = signal<AppTheme>('light');

  initialize(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const saved = window.localStorage.getItem('app-theme');
    this.apply(saved === 'dark' ? 'dark' : 'light', false);
  }

  toggle(): void {
    this.setTheme(this.theme() === 'dark' ? 'light' : 'dark');
  }

  setTheme(theme: AppTheme): void {
    this.apply(theme, true);
  }

  private apply(theme: AppTheme, persist: boolean): void {
    this.theme.set(theme);
    this.document.documentElement.dataset['theme'] = theme;

    if (!isPlatformBrowser(this.platformId)) return;
    if (persist) window.localStorage.setItem('app-theme', theme);

    const themeColor = this.document.querySelector<HTMLMetaElement>('meta[name="theme-color"]');
    const primary = this.document.defaultView
      ?.getComputedStyle(this.document.documentElement)
      .getPropertyValue('--color-primary')
      .trim();
    if (primary) themeColor?.setAttribute('content', primary);
  }
}
