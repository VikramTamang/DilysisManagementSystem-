export type NotificationType =
  | 'APPOINTMENT_CONFIRMED'
  | 'APPOINTMENT_RESCHEDULED'
  | 'APPOINTMENT_CANCELLED'
  | 'APPOINTMENT_DELAYED'
  | 'GENERAL';

export interface NotificationItem {
  id: number;
  appointmentId: number | null;
  type: NotificationType;
  message: string;
  read: boolean; // note: backend serializes the boolean field `isRead` as JSON key "read"
  createdAt: string;
}
