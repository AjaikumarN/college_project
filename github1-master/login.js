document.getElementById("loginForm").addEventListener("submit", function (e) {
  e.preventDefault();
  const email = document.getElementById("login-email").value.trim();
  const password = document.getElementById("login-password").value;
  const errorBox = document.getElementById("login-error");

  errorBox.textContent = "";

  axios.post("http://localhost:8080/api/auth/login", { email, password })
    .then(response => {
      const user = response.data;

      if (!user || !user.email) {
        errorBox.textContent = "Unexpected server response.";
        return;
      }

      localStorage.setItem("userId", user.id);
      localStorage.setItem("userEmail", user.email);
      localStorage.setItem("userName", user.name);
      window.location.href = "student.html";
    })
    .catch(error => {
      errorBox.textContent = "Invalid email or password.";
      console.error("Login error:", error);
    });
});
