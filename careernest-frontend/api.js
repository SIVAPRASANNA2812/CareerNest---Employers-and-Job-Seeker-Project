// noinspection JSUnusedGlobalSymbols,SpellCheckingInspection
/* ==========================================================================
   CAREERNEST CENTRALIZED API SERVICE (api.js)
   Industry-standard HTTP Client for Spring Boot Backend on Render
   ========================================================================== */

const API_BASE_URL = 'https://careernest-employers-jobseeker-project.onrender.com/api';

window.API = {
    getToken() {
        return localStorage.getItem('careernest_token');
    },
    setToken(token) {
        localStorage.setItem('careernest_token', token);
    },
    getUser() {
        const user = localStorage.getItem('careernest_user');
        return user ? JSON.parse(user) : null;
    },
    setUser(user) {
        localStorage.setItem('careernest_user', JSON.stringify(user));
    },
    logout() {
        localStorage.removeItem('careernest_token');
        localStorage.removeItem('careernest_refreshToken');
        localStorage.removeItem('careernest_user');
        window.location.href = 'auth.html';
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json, text/plain, */*',
            ...options.headers,
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            ...options,
            headers,
        });

        if (response.status === 401) {
            console.warn('Session expired. Redirecting to login.');
            this.logout();
            return null;
        }

        if (response.status === 204) {
            return true;
        }

        // Safely parse JSON or Plain Text
        const contentType = response.headers.get('content-type') || '';
        let data;

        if (contentType.includes('application/json')) {
            data = await response.json();
        } else {
            const text = await response.text();
            try {
                data = JSON.parse(text);
            } catch {
                data = { message: text };
            }
        }

        if (!response.ok) {
            const errorMessage = data.message || `Error ${response.status}: Request failed`;
            throw new Error(errorMessage);
        }

        return data;
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },
    post(endpoint, body) {
        return this.request(endpoint, { method: 'POST', body: JSON.stringify(body) });
    },
    put(endpoint, body) {
        return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) });
    },
    patch(endpoint, body) {
        return this.request(endpoint, { method: 'PATCH', body: JSON.stringify(body) });
    },
    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};