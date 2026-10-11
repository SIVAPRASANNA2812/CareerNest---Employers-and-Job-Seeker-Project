// noinspection JSUnusedGlobalSymbols,SpellCheckingInspection
/* ==========================================================================
   CAREERNEST JOB SEEKER PORTAL (app.js)
   Powered by Centralized API Client (api.js) & Live Spring Boot Backend
   ========================================================================== */

let allJobs = [];

// ==========================================================================
// 1. UPDATE NAVBAR WITH LOGGED-IN USER SESSION
// ==========================================================================
function updateNavUser() {
    const currentUser = API.getUser();
    const authContainer = document.getElementById('authNavContainer');
    const roleSpecificLinks = document.getElementById('roleSpecificLinks');

    if (currentUser) {
        if (currentUser.role === 'JOB_SEEKER') {
            roleSpecificLinks.innerHTML = `
        <a href="javascript:void(0)" onclick="openMyApplicationsModal()" style="font-weight: 600;">My Applications</a>
      `;
        } else if (currentUser.role === 'EMPLOYER') {
            roleSpecificLinks.innerHTML = `
        <a href="employer-dashboard.html" style="font-weight: 600;">Employer Dashboard</a>
      `;
        }

        authContainer.innerHTML = `
      <div style="display: flex; align-items: center; gap: 12px;">
        <span style="font-weight: 600; font-size: 0.9rem; color: var(--primary);">
          👤 ${escapeHtml(currentUser.fullName)} (${currentUser.role === 'EMPLOYER' ? 'Employer' : 'Seeker'})
        </span>
        <button onclick="API.logout()" class="btn btn-outline" style="padding: 6px 14px; font-size: 0.85rem;">
          Sign Out
        </button>
      </div>
    `;
    }
}

// ==========================================================================
// 2. FETCH JOBS USING API.get()
// ==========================================================================
async function loadJobs() {
    const container = document.getElementById('jobsContainer');
    const syncStatus = document.getElementById('syncStatus');

    container.innerHTML = `
    <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted);">
      <p style="font-size: 1.2rem; font-weight: 600;">🔄 Loading live jobs from Render...</p>
    </div>
  `;

    try {
        const data = await API.get('/jobs');
        allJobs = data.content || [];

        syncStatus.innerText = '🟢 Live MongoDB Atlas Connected';
        syncStatus.style.color = 'var(--primary)';
        renderJobs(allJobs);
    } catch (error) {
        console.error('Error fetching jobs:', error);
        syncStatus.innerText = '🔴 Connection error. Check server.';
        syncStatus.style.color = '#ef4444';
        container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px;">
        <p style="color: #ef4444; font-weight: 600;">Unable to connect to backend server.</p>
        <button class="btn btn-outline" style="margin-top: 12px;" onclick="loadJobs()">Retry</button>
      </div>
    `;
    }
}

// ==========================================================================
// 3. RENDER JOBS DYNAMICALLY
// ==========================================================================
function renderJobs(jobsList) {
    const container = document.getElementById('jobsContainer');
    const countEl = document.getElementById('jobCount');

    countEl.innerText = jobsList.length.toString();

    if (jobsList.length === 0) {
        container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted);">
        <p style="font-size: 1.1rem; font-weight: 600;">No jobs found matching your criteria.</p>
        <button class="btn btn-outline" style="margin-top: 12px;" onclick="resetSearch()">Clear Filters</button>
      </div>
    `;
        return;
    }

    container.innerHTML = jobsList.map(job => `
    <article class="job-card">
      <div class="job-card-header">
        <div>
          <div class="company-name">${escapeHtml(job.companyName)}</div>
          <h3 class="job-title">${escapeHtml(job.title)}</h3>
        </div>
        <span class="job-badge">${escapeHtml(job.jobType || 'FULL TIME')}</span>
      </div>
      <p class="job-description">${escapeHtml(job.description)}</p>
      <div class="job-meta">
        <div class="job-meta-item">📍 ${escapeHtml(job.location)}</div>
        <div class="job-meta-item">⏳ Deadline: ${escapeHtml(job.deadline || 'Open')}</div>
      </div>
      <div class="job-card-footer">
        <div class="salary-tag">${escapeHtml(job.salary || 'Competitive')}</div>
        <button class="btn btn-primary" onclick="openApplyModal('${job.id}', '${escapeHtml(job.title)}', '${escapeHtml(job.companyName)}')">
          Apply Now
        </button>
      </div>
    </article>
  `).join('');
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}

// ==========================================================================
// 4. INSTANT SEARCH & FILTERS
// ==========================================================================
function filterJobs() {
    const keyword = document.getElementById('searchInput').value.toLowerCase().trim();
    const location = document.getElementById('locationInput').value.toLowerCase().trim();

    const filtered = allJobs.filter(job => {
        const matchesKeyword = !keyword ||
            job.title.toLowerCase().includes(keyword) ||
            job.companyName.toLowerCase().includes(keyword) ||
            (job.description && job.description.toLowerCase().includes(keyword));

        const matchesLocation = !location || job.location.toLowerCase().includes(location);

        return matchesKeyword && matchesLocation;
    });

    renderJobs(filtered);
}

function resetSearch() {
    document.getElementById('searchInput').value = '';
    document.getElementById('locationInput').value = '';
    renderJobs(allJobs);
}

// ==========================================================================
// 5. EASY APPLY MODAL & LIVE SUBMISSION
// ==========================================================================
function openApplyModal(jobId, title, company) {
    const user = API.getUser();
    if (!user) {
        alert('Please sign in or register as a Job Seeker to apply for jobs!');
        window.location.href = 'auth.html';
        return;
    }

    if (user.role === 'EMPLOYER') {
        alert('Employers cannot apply for jobs. Please sign in with a Job Seeker account.');
        return;
    }

    document.getElementById('applyJobId').value = jobId;
    document.getElementById('modalJobTitle').innerText = title;
    document.getElementById('modalCompanyName').innerText = company;

    document.getElementById('applicantName').value = user.fullName || '';
    document.getElementById('applicantEmail').value = user.email || '';

    document.getElementById('applyModal').classList.add('active');
}

function closeApplyModal() {
    document.getElementById('applyModal').classList.remove('active');
}

async function handleApplySubmit(e) {
    e.preventDefault();
    const user = API.getUser();
    const jobId = document.getElementById('applyJobId').value;
    const resumeUrl = document.getElementById('resumeLink').value;
    const submitBtn = document.getElementById('submitApplyBtn');

    submitBtn.disabled = true;
    submitBtn.innerText = 'Submitting...';

    try {
        await API.post(`/applications/apply/${jobId}`, {
            seekerId: user.id,
            resumeUrl: resumeUrl,
            coverLetter: "Application submitted via CareerNest Web Portal"
        });

        alert('🎉 Application submitted successfully! An SMS status alert will be sent upon employer review.');
        closeApplyModal();
    } catch (err) {
        alert(err.message || 'Failed to submit application. Make sure you are signed in as a Job Seeker.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Submit Application';
    }
}

// ==========================================================================
// 6. "MY APPLICATIONS" MODAL (GET /api/applications/my-applications/{seekerId})
// ==========================================================================
async function openMyApplicationsModal() {
    const user = API.getUser();
    if (!user) return;

    const modal = document.getElementById('myAppsModal');
    const content = document.getElementById('myAppsContent');

    modal.classList.add('active');
    content.innerHTML = `<p style="text-align: center; color: var(--text-muted); padding: 20px;">Fetching your applications from MongoDB Atlas...</p>`;

    try {
        const apps = await API.get(`/applications/my-applications/${user.id}`);

        if (!apps || apps.length === 0) {
            content.innerHTML = `
        <div style="text-align: center; padding: 30px; color: var(--text-muted);">
          <p style="font-size: 1.1rem; font-weight: 600;">You haven't applied to any jobs yet.</p>
          <p style="font-size: 0.9rem; margin-top: 6px;">Browse open positions on the home page and click "Apply Now"!</p>
        </div>
      `;
            return;
        }

        content.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        ${apps.map(app => {
            const matchedJob = allJobs.find(j => j.id === app.jobId);
            const jobTitle = matchedJob ? matchedJob.title : `Job #${app.jobId}`;
            const company = matchedJob ? matchedJob.companyName : '';

            let badgeColor = 'var(--status-pending-bg); color: var(--status-pending-text);';
            if (app.status === 'ACCEPTED') badgeColor = 'var(--status-accepted-bg); color: var(--status-accepted-text);';
            if (app.status === 'REJECTED') badgeColor = 'var(--status-rejected-bg); color: var(--status-rejected-text);';
            if (app.status === 'UNDER_REVIEW') badgeColor = '#e0f2fe; color: #0369a1;';

            return `
            <div style="border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 16px; display: flex; justify-content: space-between; align-items: center;">
              <div>
                <h4 style="font-size: 1rem; font-weight: 700; color: var(--text-main);">${escapeHtml(jobTitle)}</h4>
                <p style="color: var(--primary); font-size: 0.85rem; font-weight: 600;">${escapeHtml(company)}</p>
                <a href="${escapeHtml(app.resumeUrl)}" target="_blank" style="font-size: 0.82rem; color: var(--text-muted); text-decoration: underline;">View Submitted Resume</a>
              </div>
              <div style="text-align: right;">
                <span style="display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 0.78rem; font-weight: 700; background: ${badgeColor}">
                  ${app.status}
                </span>
              </div>
            </div>
          `;
        }).join('')}
      </div>
    `;

    } catch (err) {
        content.innerHTML = `<p style="text-align: center; color: #ef4444; padding: 20px;">Failed to load applications.</p>`;
    }
}

function closeMyAppsModal() {
    document.getElementById('myAppsModal').classList.remove('active');
}

// ==========================================================================
// 7. INITIALIZATION
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    updateNavUser();
    loadJobs();

    document.getElementById('searchInput').addEventListener('input', filterJobs);
    document.getElementById('locationInput').addEventListener('input', filterJobs);
    document.getElementById('searchForm').addEventListener('submit', (e) => {
        e.preventDefault();
        filterJobs();
    });

    document.getElementById('closeModalBtn').addEventListener('click', closeApplyModal);
    document.getElementById('cancelModalBtn').addEventListener('click', closeApplyModal);
    document.getElementById('applyForm').addEventListener('submit', handleApplySubmit);

    document.getElementById('closeMyAppsBtn').addEventListener('click', closeMyAppsModal);
    document.getElementById('closeMyAppsBottomBtn').addEventListener('click', closeMyAppsModal);
});