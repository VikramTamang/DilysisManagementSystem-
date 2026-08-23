import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  hospitalName = 'Hospital Management System';
  userName = 'Admin';

  isLoggedIn = true;
  isProcessing = false;

  logout(): void {
    this.isProcessing = true;

    setTimeout(() => {
      this.isLoggedIn = false;
      this.isProcessing = false;
    }, 500);
  }

  login(): void {
    this.isProcessing = true;

    setTimeout(() => {
      this.isLoggedIn = true;
      this.isProcessing = false;
    }, 5);
  }
}
