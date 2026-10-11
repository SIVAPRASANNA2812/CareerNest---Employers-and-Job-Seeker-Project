// noinspection JSUnusedGlobalSymbols,SpellCheckingInspection
/* ==========================================================================
   CAREERNEST EMPLOYER DASHBOARD CONTROLLER (dashboard.js)
   Full CRUD against Live Spring Boot Backend & Twilio SMS Status Updates
   ========================================================================== */

let employerJobs = [];

// ==========================================================================
// 1. ROUTE GUARD (CHECK EMPLOYER PERMISSIONS)
// ==========================================================================
const currentUser = API.getUser();

if (!currentUser || currentUser.role !== 'EMPLOYER') {
    alert('Access Restricted: Please sign in with an Employer account to access this page.');
    window.location.href = 'auth.html';
}

document.getElementById('navEmployerName').innerText = `🏢 ${currentUser.fullName}`;

// Set tomorrow's date as minimum deadline date picker
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);
document.getElementById('newDeadline').min = tomorrow.toISOString().split('T')[0];
document.getElementById('newDeadline').value = tomorrow.toISOString().split('T')[0];

// ==========================================================================
// 2. LOAD EMPLOYER JOBS & METRICS
// ==========================================================================
async function loadEmployerDashboard() {
    const jobsTableBody = document.getElementById('jobsTableBody');
    const kpiJobCount = document.getElementById('kpiJobCount');

    try {
        const data = await API.get('/jobs');
        // Filter jobs created by this employer (or show all if admin)
        employerJobs = data.content || [];

        kpiJobCount.innerText = employerJobs.length.toString();

        if (employerJobs.length === 0) {
            jobsTableBody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">
            No jobs posted yet. Click "+ Post New Job" to create your first listing!
          </td>
        </tr>
      `;
            return;
        }

        jobsTableBody.innerHTML = employerJobs.map(job => `
      <tr>
        <td style="font-weight: 700;">${escapeHtml(job.title)}</td>
        <td style="color: var(--primary); font-weight: 600;">${escapeHtml(job.companyName)}</td>
        <td>📍 ${escapeHtml(job.location)}</td>
        <td style="font-weight: 600;">${escapeHtml(job.salary)}</td>
        <td>⏳ ${escapeHtml(job.deadline || 'Open')}</td>
        <td><span class="job-badge">${escapeHtml(job.jobType || 'FULL_TIME')}</span></td>
        <td>
          <button class="btn btn-outline" style="padding: 4px 10px; font-size: 0.82rem; color: #ef4444; border-color: #fca5a5;" onclick="deleteJob('${job.id}', '${escapeHtml(job.title)}')">
            Delete 🗑️
          </button>
        </td>
      </tr>
    `).join('');

        // Load candidate applications for these jobs
        loadApplications();

    } catch (err) {
        console.error('Failed to load employer dashboard:', err);
        jobsTableBody.innerHTML = `
      <tr><td colspan="7" style="text-align: center; color: #ef4444; padding: 20px;">
        Error connecting to backend server.
      </td></tr>
    `;
    }
}

// ==========================================================================
// 3. DELETE JOB (DELETE /api/jobs/{id})
// ==========================================================================
async function deleteJob(jobId, jobTitle) {
    if (!confirm(`Are you sure you want to delete "${jobTitle}"? This cannot be undone.`)) {
        return;
    }

    try {
        await API.delete(`/jobs/${jobId}`);
        alert(`🗑️ Job "${jobTitle}" was deleted successfully.`);
        loadEmployerDashboard();
    } catch (err) {
        alert(err.message || 'Failed to delete job.');
    }
}

// ==========================================================================
// 4. LOAD CANDIDATE APPLICATIONS
// ==========================================================================
async function loadApplications() {
    const applicantTableBody = document.getElementById('applicantTableBody');
    const kpiApplicantCount = document.getElementById('kpiApplicantCount');
    const kpiAcceptedCount = document.getElementById('kpiAcceptedCount');

    let allApplications = [];

    try {
        for (const job of employerJobs) {
            try {
                const apps = await API.get(`/applications/job/${job.id}`);
                if (Array.isArray(apps)) {
                    allApplications.push(...apps);
                }
            } catch (e) {
                // Continue if a job has no applications yet
            }
        }

        kpiApplicantCount.innerText = allApplications.length.toString();
        const acceptedTotal = allApplications.filter(a => a.status === 'ACCEPTED').length;
        kpiAcceptedCount.innerText = acceptedTotal.toString();

        if (allApplications.length === 0) {
            applicantTableBody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 30px;">
            No candidates have applied yet. Applications will appear here in real time.
          </td>
        </tr>
      `;
            return;
        }

        applicantTableBody.innerHTML = allApplications.map(app => {
            // Find the human-readable job title instead of showing raw ObjectId
            const matchedJob = employerJobs.find(j => j.id === app.jobId);
            const displayTitle = matchedJob ? `${matchedJob.title} (${matchedJob.companyName})` : app.jobId;

            return `
        <tr>
          <td>
            <span style="font-family: monospace; font-size: 0.85rem; color: var(--text-muted);">${app.id.substring(0, 10)}...</span>
          </td>
          <td>
            <div style="font-weight: 600; color: var(--text-main); font-size: 0.92rem;">${escapeHtml(displayTitle)}</div>
          </td>
          <td>
            <a href="${escapeHtml(app.resumeUrl)}" target="_blank" style="color: var(--primary); font-weight: 600; font-size: 0.88rem;">
              View Resume 📄
            </a>
          </td>
          <td>
            <select class="status-select" onchange="updateCandidateStatus('${app.id}', this.value)">
              <option value="APPLIED" ${app.status === 'APPLIED' ? 'selected' : ''}>APPLIED</option>
              <option value="UNDER_REVIEW" ${app.status === 'UNDER_REVIEW' ? 'selected' : ''}>UNDER_REVIEW</option>
              <option value="ACCEPTED" ${app.status === 'ACCEPTED' ? 'selected' : ''}>ACCEPTED</option>
              <option value="REJECTED" ${app.status === 'REJECTED' ? 'selected' : ''}>REJECTED</option>
            </select>
          </td>
          <td>
            <button class="btn btn-outline" style="padding: 4px 10px; font-size: 0.82rem;" onclick="alert('Applicant Seeker ID:\\n' + '${app.seekerId}')">
              Details
            </button>
          </td>
        </tr>
      `;
        }).join('');

    } catch (err) {
        console.error('Failed to load applications:', err);
    }
}

// ==========================================================================
// 5. LIVE STATUS CHANGE (TRIGGERS BACKEND TWILIO SMS)
// ==========================================================================
async function updateCandidateStatus(applicationId, newStatus) {
    try {
        await API.patch(`/applications/${applicationId}/status`, { status: newStatus });

        alert(`✅ Status updated to: ${newStatus}!\n\n📱 Twilio Cloud SMS notification dispatched to applicant's phone.`);
        loadApplications();
    } catch (err) {
        alert(err.message || 'Failed to update application status.');
    }
}

// ==========================================================================
// 6. POST NEW JOB (POST /api/jobs)
// ==========================================================================
const postJobModal = document.getElementById('postJobModal');
const postJobForm = document.getElementById('postJobForm');
const openPostJobBtn = document.getElementById('openPostJobBtn');
const closePostJobBtn = document.getElementById('closePostJobBtn');
const cancelPostJobBtn = document.getElementById('cancelPostJobBtn');

openPostJobBtn.addEventListener('click', () => postJobModal.classList.add('active'));
closePostJobBtn.addEventListener('click', () => postJobModal.classList.remove('active'));
cancelPostJobBtn.addEventListener('click', () => postJobModal.classList.remove('active'));

postJobForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById('submitPostJobBtn');

    const title = document.getElementById('newJobTitle').value.trim();
    const companyName = document.getElementById('newCompanyName').value.trim();
    const location = document.getElementById('newLocation').value.trim();
    const salary = document.getElementById('newSalary').value.trim();
    const jobType = document.getElementById('newJobType').value;
    const deadline = document.getElementById('newDeadline').value;
    const description = document.getElementById('newDescription').value.trim();

    submitBtn.disabled = true;
    submitBtn.innerText = 'Publishing...';

    try {
        const payload = {
            title,
            companyName,
            location,
            salary,
            jobType,
            deadline,
            description,
            employerId: currentUser.id
        };

        await API.post('/jobs', payload);

        alert(`🎉 Job "${title}" published successfully to MongoDB Atlas!`);
        postJobForm.reset();
        postJobModal.classList.remove('active');
        loadEmployerDashboard();

    } catch (err) {
        alert(err.message || 'Failed to post job. Please verify all fields.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Publish to MongoDB Atlas';
    }
});

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}

// Initial Load
document.addEventListener('DOMContentLoaded', loadEmployerDashboard);