/* ==========================================================================
   CAREERNEST AUTHENTICATION CONTROLLER (auth.js)
   Handles Register and Login with Live Spring Boot JWT Backend
   ========================================================================== */

let selectedRole = 'JOB_SEEKER';

// Tab & Role Switching
const tabLoginBtn = document.getElementById('tabLoginBtn');
const tabRegisterBtn = document.getElementById('tabRegisterBtn');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const authAlert = document.getElementById('authAlert');

tabLoginBtn.addEventListener('click', () => {
    tabLoginBtn.classList.add('active');
    tabRegisterBtn.classList.remove('active');
    loginForm.style.display = 'block';
    registerForm.style.display = 'none';
    hideAlert();
});

tabRegisterBtn.addEventListener('click', () => {
    tabRegisterBtn.classList.add('active');
    tabLoginBtn.classList.remove('active');
    registerForm.style.display = 'block';
    loginForm.style.display = 'none';
    hideAlert();
});

function selectRole(role) {
    selectedRole = role;
    const seekerEl = document.getElementById('roleSeeker');
    const employerEl = document.getElementById('roleEmployer');

    if (role === 'JOB_SEEKER') {
        seekerEl.classList.add('selected');
        employerEl.classList.remove('selected');
    } else {
        employerEl.classList.add('selected');
        seekerEl.classList.remove('selected');
    }
}

function showAlert(message, type) {
    authAlert.innerText = message;
    authAlert.className = `alert-box alert-${type}`;
}

function hideAlert() {
    authAlert.className = 'alert-box';
    authAlert.innerText = '';
}

// 1. Login Handler (POST /api/auth/login)
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();

    const submitBtn = document.getElementById('loginSubmitBtn');
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;

    submitBtn.disabled = true;
    submitBtn.innerText = 'Authenticating...';

    try {
        const data = await API.post('/auth/login', { email, password });

        API.setToken(data.token);
        API.setUser({
            id: data.id,
            fullName: data.fullName,
            email: data.email,
            role: data.role
        });

        showAlert(`Welcome back, ${data.fullName}! Redirecting...`, 'success');

        setTimeout(() => {
            if (data.role === 'EMPLOYER') {
                window.location.href = 'employer-dashboard.html';
            } else {
                window.location.href = 'index.html';
            }
        }, 1000);

    } catch (err) {
        showAlert(err.message || 'Invalid email or password. Please try again.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Sign In';
    }
});

// 2. Register Handler (POST /api/auth/register then Auto-Login)
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();

    const submitBtn = document.getElementById('regSubmitBtn');
    const fullName = document.getElementById('regFullName').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const phoneNumber = document.getElementById('regPhone').value.trim();
    const password = document.getElementById('regPassword').value;

    submitBtn.disabled = true;
    submitBtn.innerText = 'Creating Account...';

    try {
        const payload = {
            fullName,
            email,
            password,
            phoneNumber,
            role: selectedRole
        };

        // Step 1: Save User in MongoDB Atlas
        await API.post('/auth/register', payload);

        // Step 2: Auto-Login to receive JWT token
        submitBtn.innerText = 'Signing in...';
        const loginData = await API.post('/auth/login', { email, password });

        API.setToken(loginData.token);
        API.setUser({
            id: loginData.id,
            fullName: loginData.fullName,
            email: loginData.email,
            role: loginData.role
        });

        showAlert(`🎉 Account created! Welcome, ${loginData.fullName}. Redirecting...`, 'success');

        setTimeout(() => {
            if (loginData.role === 'EMPLOYER') {
                window.location.href = 'employer-dashboard.html';
            } else {
                window.location.href = 'index.html';
            }
        }, 1200);

    } catch (err) {
        showAlert(err.message || 'Registration failed. Please check your details.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Create CareerNest Account';
    }
});