document.getElementById("loginForm").addEventListener("submit", async function (e) {
  e.preventDefault();
  const email = document.getElementById("login-email").value.trim();
  const password = document.getElementById("login-password").value;
  const errorBox = document.getElementById("login-error");

  errorBox.textContent = "";

  try {
    // Use authService for login
    const result = await authService.login(email, password);
    
    if (result.success) {
      // authService handles storing token and user data
      // Redirect based on role
      authService.redirectToRoleDashboard();
    } else {
      errorBox.textContent = "Login failed. Please try again.";
    }
  } catch (error) {
    errorBox.textContent = error.message || "Invalid email or password.";
    console.error("Login error:", error);
  }
});
