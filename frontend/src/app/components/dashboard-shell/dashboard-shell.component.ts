import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-shell',
  templateUrl: './dashboard-shell.component.html',
  styleUrl: './dashboard-shell.component.css'
})
export class DashboardShellComponent {
  isSidebarExpanded = true;
  currentUser = {
    name: 'System Admin',
    role: 'ADMIN',
    email: 'admin@hospital.com'
  };

  // Mock Data matching backend concepts
  upcomingSessions = [
    { id: 101, patientName: 'Aarav Sharma', staffName: 'Dr. Aditi', room: 'Room-101', machine: 'DM-001', time: '10:00 AM - 02:00 PM', status: 'IN_PROGRESS' },
    { id: 102, patientName: 'Sita Kumari', staffName: 'Nurse Ramesh', room: 'Room-102', machine: 'DM-002', time: '11:30 AM - 03:30 PM', status: 'SCHEDULED' },
    { id: 103, patientName: 'John Doe', staffName: 'Dr. Aditi', room: 'Room-103', machine: 'DM-003', time: '01:00 PM - 05:00 PM', status: 'SCHEDULED' }
  ];

  recentReassignments = [
    { appointmentId: 2089, outcome: 'REASSIGNED', previousStaff: 'Dr. Vikram', newStaff: 'Dr. Aditi', note: 'Dr. Vikram reported sick; auto-reassigned.' }
  ];

  constructor(private router: Router) {}

  toggleSidebar() {
    this.isSidebarExpanded = !this.isSidebarExpanded;
  }

  logout() {
    // Clear mock state if any
    console.log('Logging out...');
    this.router.navigate(['/']);
  }
}
