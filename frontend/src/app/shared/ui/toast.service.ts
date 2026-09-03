import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarRef, TextOnlySnackBar } from '@angular/material/snack-bar';

type ToastTone = 'success' | 'info' | 'warning' | 'error';

@Injectable({ providedIn: 'root' })
export class ToastService {
  constructor(private snackBar: MatSnackBar) {}

  show(message: string, tone: ToastTone = 'info', action = 'Cerrar', duration = 3200): MatSnackBarRef<TextOnlySnackBar> {
    return this.snackBar.open(message, action, {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`toast`, `toast--${tone}`],
      politeness: tone === 'error' ? 'assertive' : 'polite',
      announcementMessage: message
    });
  }

  success(message: string, action = 'Cerrar', duration = 3000): MatSnackBarRef<TextOnlySnackBar> {
    return this.show(message, 'success', action, duration);
  }

  info(message: string, action = 'Cerrar', duration = 3200): MatSnackBarRef<TextOnlySnackBar> {
    return this.show(message, 'info', action, duration);
  }

  warning(message: string, action = 'Cerrar', duration = 3600): MatSnackBarRef<TextOnlySnackBar> {
    return this.show(message, 'warning', action, duration);
  }

  error(message: string, action = 'Cerrar', duration = 4200): MatSnackBarRef<TextOnlySnackBar> {
    return this.show(message, 'error', action, duration);
  }
}
