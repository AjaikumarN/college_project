// Main application script
// Enhanced with authentication and modern API integration

class CollegeApp {
    constructor() {
        this.currentUser = null;
        this.initializeAuth();
        this.initializeNavigation();
        this.initializeGlobalEventListeners();
    }

    // Initialize authentication
    initializeAuth() {
        // Check if user is authenticated
        if (authService.isAuthenticated()) {
            this.currentUser = authService.getCurrentUser();
            this.setupAuthenticatedState();
        } else {
            this.setupUnauthenticatedState();
        }
    }

    // Setup authenticated state
    setupAuthenticatedState() {
        // Update UI for authenticated user
        this.updateUserHeader();
        this.showUserMenu();
        this.hideLoginButtons();
    }

    // Setup unauthenticated state
    setupUnauthenticatedState() {
        // Redirect to login if on protected pages
        const protectedPages = [
            'admin.html', 'student.html', 'faculty.html',
            'courses.html', 'attendance.html', 'marks.html',
            'notes.html', 'announcement.html'
        ];
        
        const currentPage = window.location.pathname.split("/").pop();
        if (protectedPages.includes(currentPage)) {
            window.location.href = 'login.html';
        }
    }

    // Update user header with current user info
    updateUserHeader() {
        const userNameElements = document.querySelectorAll('.user-name');
        const userEmailElements = document.querySelectorAll('.user-email');
        const userRoleElements = document.querySelectorAll('.user-role');

        if (this.currentUser) {
            userNameElements.forEach(el => el.textContent = this.currentUser.name);
            userEmailElements.forEach(el => el.textContent = this.currentUser.email);
            userRoleElements.forEach(el => el.textContent = this.currentUser.role);
        }
    }

    // Show user menu
    showUserMenu() {
        const userMenus = document.querySelectorAll('.user-menu');
        userMenus.forEach(menu => menu.style.display = 'block');

        const loginButtons = document.querySelectorAll('.login-button');
        loginButtons.forEach(btn => btn.style.display = 'none');
    }

    // Hide login buttons
    hideLoginButtons() {
        const loginButtons = document.querySelectorAll('.login-btn, .register-btn');
        loginButtons.forEach(btn => btn.style.display = 'none');
    }

    // Initialize navigation
    initializeNavigation() {
        this.setActiveNavigation();
        this.setupRoleBasedNavigation();
    }

    // Set active navigation based on current page
    setActiveNavigation() {
        const currentPage = window.location.pathname.split("/").pop();
        const navLinks = document.querySelectorAll(".nav-link");

        navLinks.forEach(link => {
            const linkPage = link.getAttribute("href");
            link.classList.remove("active");
            
            if (linkPage === currentPage || (currentPage === "" && linkPage === "index.html")) {
                link.classList.add("active");
            }
        });
    }

    // Setup role-based navigation
    setupRoleBasedNavigation() {
        if (!this.currentUser) return;

        const role = this.currentUser.role;
        const navItems = document.querySelectorAll('.nav-item[data-role]');

        navItems.forEach(item => {
            const allowedRoles = item.getAttribute('data-role').split(',');
            if (!allowedRoles.includes(role)) {
                item.style.display = 'none';
            }
        });
    }

    // Initialize global event listeners
    initializeGlobalEventListeners() {
        // Logout functionality
        const logoutButtons = document.querySelectorAll('.logout-btn, .logout-button');
        logoutButtons.forEach(btn => {
            btn.addEventListener('click', () => this.handleLogout());
        });

        // Profile button
        const profileButtons = document.querySelectorAll('.profile-btn, .profile-button');
        profileButtons.forEach(btn => {
            btn.addEventListener('click', () => this.handleProfileView());
        });

        // Theme toggle
        const themeToggles = document.querySelectorAll('.theme-toggle');
        themeToggles.forEach(toggle => {
            toggle.addEventListener('click', () => this.toggleTheme());
        });

        // Search functionality
        const searchInputs = document.querySelectorAll('.search-input');
        searchInputs.forEach(input => {
            input.addEventListener('input', (e) => this.handleSearch(e.target.value));
        });

        // Global keyboard shortcuts
        document.addEventListener('keydown', (e) => this.handleKeyboardShortcuts(e));

        // Error handling for unhandled promises
        window.addEventListener('unhandledrejection', (e) => {
            console.error('Unhandled promise rejection:', e.reason);
            this.showErrorMessage('An unexpected error occurred. Please try again.');
        });
    }

    // Handle logout
    handleLogout() {
        if (confirm('Are you sure you want to logout?')) {
            authService.logout();
            window.location.href = 'index.html';
        }
    }

    // Handle profile view
    handleProfileView() {
        if (!this.currentUser) return;

        const role = this.currentUser.role;
        switch (role) {
            case 'ADMIN':
                window.location.href = 'admin-profile.html';
                break;
            case 'FACULTY':
                window.location.href = 'faculty-profile.html';
                break;
            case 'STUDENT':
                window.location.href = 'student-profile.html';
                break;
            default:
                console.error('Unknown role:', role);
        }
    }

    // Toggle theme
    toggleTheme() {
        const body = document.body;
        const isDark = body.classList.contains('dark-theme');
        
        if (isDark) {
            body.classList.remove('dark-theme');
            localStorage.setItem('theme', 'light');
        } else {
            body.classList.add('dark-theme');
            localStorage.setItem('theme', 'dark');
        }
    }

    // Handle search
    handleSearch(query) {
        if (!query.trim()) return;

        // Implement global search functionality
        console.log('Searching for:', query);
        // This would typically make an API call to search across the system
    }

    // Handle keyboard shortcuts
    handleKeyboardShortcuts(e) {
        // Ctrl/Cmd + K for search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.querySelector('.search-input');
            if (searchInput) searchInput.focus();
        }

        // Escape to close modals
        if (e.key === 'Escape') {
            this.closeAllModals();
        }
    }

    // Close all modals
    closeAllModals() {
        const modals = document.querySelectorAll('.modal, .popup, .overlay');
        modals.forEach(modal => {
            modal.style.display = 'none';
            modal.classList.remove('active', 'open');
        });
    }

    // Show error message
    showErrorMessage(message) {
        const errorContainer = document.getElementById('error-container') || this.createErrorContainer();
        errorContainer.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <span>${message}</span>
                <button class="close-btn" onclick="this.parentElement.style.display='none'">×</button>
            </div>
        `;
        errorContainer.style.display = 'block';

        // Auto hide after 5 seconds
        setTimeout(() => {
            errorContainer.style.display = 'none';
        }, 5000);
    }

    // Create error container if it doesn't exist
    createErrorContainer() {
        const container = document.createElement('div');
        container.id = 'error-container';
        container.className = 'error-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            display: none;
        `;
        document.body.appendChild(container);
        return container;
    }

    // Show success message
    showSuccessMessage(message) {
        const successContainer = document.getElementById('success-container') || this.createSuccessContainer();
        successContainer.innerHTML = `
            <div class="success-message">
                <i class="fas fa-check-circle"></i>
                <span>${message}</span>
                <button class="close-btn" onclick="this.parentElement.style.display='none'">×</button>
            </div>
        `;
        successContainer.style.display = 'block';

        // Auto hide after 3 seconds
        setTimeout(() => {
            successContainer.style.display = 'none';
        }, 3000);
    }

    // Create success container if it doesn't exist
    createSuccessContainer() {
        const container = document.createElement('div');
        container.id = 'success-container';
        container.className = 'success-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            display: none;
        `;
        document.body.appendChild(container);
        return container;
    }

    // Initialize theme from localStorage
    initializeTheme() {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-theme');
        }
    }
}

// Utility functions for common operations
const utils = {
    // Format date to readable string
    formatDate: (date) => {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    },

    // Format time to readable string
    formatTime: (date) => {
        return new Date(date).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Capitalize first letter
    capitalize: (str) => {
        return str.charAt(0).toUpperCase() + str.slice(1);
    },

    // Truncate text
    truncate: (text, length) => {
        return text.length > length ? text.substring(0, length) + '...' : text;
    },

    // Validate email
    isValidEmail: (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },

    // Generate random ID
    generateId: () => {
        return Math.random().toString(36).substr(2, 9);
    },

    // Debounce function
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};

// Initialize the application
document.addEventListener("DOMContentLoaded", function() {
    window.collegeApp = new CollegeApp();
    window.collegeApp.initializeTheme();
});

// Export for global access
window.CollegeApp = CollegeApp;
window.utils = utils;

