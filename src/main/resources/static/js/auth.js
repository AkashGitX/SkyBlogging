const TOKEN_KEY = 'jwt_token';
const USER_KEY = 'user_info';

function setAuth(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    // Also set cookie for SSR if needed
    document.cookie = `jwt=${token}; path=/; max-age=86400`; // 1 day
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    document.cookie = 'jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

function isAuthenticated() {
    return !!localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401) {
        clearAuth();
        window.location.href = '/login';
    }

    return response;
}

// Update UI based on auth state
document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.getElementById('nav-links');
    if (navLinks) {
        if (isAuthenticated()) {
            const user = getUser();
            navLinks.innerHTML = `
                <span>Welcome, ${user.username}</span>
                <a href="/post/create" class="btn btn-outline">Write</a>
                <button id="logout-btn" class="btn btn-outline">Logout</button>
            `;
            
            document.getElementById('logout-btn').addEventListener('click', () => {
                clearAuth();
                window.location.href = '/';
            });
        } else {
            navLinks.innerHTML = `
                <a href="/login">Sign In</a>
                <a href="/register" class="btn btn-primary">Get Started</a>
            `;
        }
    }

    // Handle Login
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error-message');

            try {
                const res = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (res.ok) {
                    const data = await res.json();
                    setAuth(data.token, data);
                    window.location.href = '/';
                } else {
                    errorDiv.textContent = 'Invalid email or password';
                }
            } catch (err) {
                errorDiv.textContent = 'Network error occurred';
            }
        });
    }

    // Handle Register
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error-message');

            try {
                const res = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password })
                });

                if (res.ok) {
                    // Auto login after register
                    const loginRes = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password })
                    });
                    
                    if(loginRes.ok) {
                        const data = await loginRes.json();
                        setAuth(data.token, data);
                        window.location.href = '/';
                    }
                } else {
                    const text = await res.text();
                    errorDiv.textContent = text || 'Registration failed';
                }
            } catch (err) {
                errorDiv.textContent = 'Network error occurred';
            }
        });
    }
});