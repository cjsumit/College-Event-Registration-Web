/**
 * =========================================================
 * MOCK BACKEND LAYER (To make buttons work in Demo)
 * =========================================================
 * This block intercepts the fetch calls so the app works 
 * without a real Java server running. 
 */
(function() {
  const originalFetch = window.fetch;
  const MOCK_EVENTS = [
    { id: 1, title: "Tech-Aagaz: Hackathon", type: "Technical", startDatetime: "Dec 12, 10:00 AM", venue: "CS Lab Block A", description: "24-Hour coding marathon. Build solutions for smart cities.", fee: 100, rules: "1. Team of 4 max\n2. BYOD (Bring your own device)", coordinators: "Mr. R.K. Singh", prizes: "1st: ₹10,000" },
    { id: 2, title: "Robo-Race", type: "Technical", startDatetime: "Dec 13, 11:00 AM", venue: "Mechanical Workshop", description: "Navigate your robot through the obstacle course.", fee: 200, rules: "Max weight 5kg", coordinators: "Ms. Priya", prizes: "1st: ₹5,000" },
    { id: 3, title: "Sargam: Singing", type: "Cultural", startDatetime: "Dec 13, 05:00 PM", venue: "Main Auditorium", description: "Solo and Duet singing competition.", fee: 50, rules: "Time limit: 4 mins", coordinators: "Student Council", prizes: "Trophies" },
    { id: 4, title: "Cricket Tournament", type: "Sports", startDatetime: "Dec 14, 08:00 AM", venue: "RRIMT Sports Ground", description: "Inter-branch cricket championship.", fee: 500, rules: "11 Players + 2 Subs", coordinators: "Coach Yadav", prizes: "Championship Cup" }
  ];

  window.fetch = async (url, options) => {
    // console.log(`[Simulating Backend] ${url}`);
    await new Promise(r => setTimeout(r, 300)); // Fake network delay

    if (url === '/api/events') {
      return { status: 200, json: async () => MOCK_EVENTS };
    }
    if (url.match(/\/api\/events\/\d+/)) {
      const id = parseInt(url.split('/').pop());
      const ev = MOCK_EVENTS.find(e => e.id === id);
      return { status: 200, json: async () => ev };
    }
    if (url === '/api/register') {
      const body = JSON.parse(options.body);
      const reg = { ...body, eventName: MOCK_EVENTS.find(e=>e.id==body.id)?.title, createdAt: new Date().toLocaleDateString() };
      let db = JSON.parse(localStorage.getItem('rrimt_regs') || '[]');
      db.push(reg);
      localStorage.setItem('rrimt_regs', JSON.stringify(db));
      return { status: 200, json: async () => ({ success: true }) };
    }
    if (url.includes('/api/registrations')) {
      return { status: 200, json: async () => JSON.parse(localStorage.getItem('rrimt_regs') || '[]') };
    }
    if (url === '/api/admin/login') {
        return { status: 200, json: async () => ({ success: true }) };
    }
    // Fallback to original if needed
    return { ok: false, status: 404 };
  };
})();

/**
 * =========================================================
 * YOUR ORIGINAL LOGIC STARTS HERE
 * (No changes made to function names or flow)
 * =========================================================
 */

const toast = document.getElementById('toast');
const views = Array.from(document.querySelectorAll('.view'));
const navBtns = Array.from(document.querySelectorAll('.nav-btn'));

function showToast(msg, timeout=2500) {
  toast.textContent = msg;
  toast.style.display = 'block';
  setTimeout(()=> toast.style.display='none', timeout);
}

function showView(name) {
  views.forEach(v => v.style.display = (v.id === name ? 'block' : 'none'));
  navBtns.forEach(b => {
    if(b.dataset.view === name) b.classList.add('active');
    else b.classList.remove('active');
  });
}

navBtns.forEach(b => b.addEventListener('click', ()=> showView(b.dataset.view || 'home')));

// Load events and populate lists
async function loadEvents() {
  try {
    const res = await fetch('/api/events');
    const arr = await res.json();
    const eventsList = document.getElementById('eventsList');
    const highlights = document.getElementById('highlightsList');
    const eventSelect = document.getElementById('eventSelect');
    
    eventsList.innerHTML = '';
    highlights.innerHTML = '';
    eventSelect.innerHTML = '';
    
    arr.forEach(ev => {
      // events list (Updated HTML structure for new CSS)
      const card = document.createElement('div');
      card.className = 'reg-item';
      card.innerHTML = `
        <div>
          <strong>${escapeHtml(ev.title)}</strong>
          <div class="muted" style="margin-top:5px">
             ${escapeHtml(ev.type)} • ${escapeHtml(ev.startDatetime || '')}
          </div>
          <div class="muted">📍 ${escapeHtml(ev.venue || '')}</div>
        </div>
        <div style="margin-top:15px; display:flex; gap:10px">
           <button class="btn secondary" data-id="${ev.id}">Details</button> 
           <button class="btn primary" data-register="${ev.id}">Register</button>
        </div>`;
      eventsList.appendChild(card);

      // highlights
      const h = document.createElement('div'); 
      h.style.padding = '10px'; h.style.borderBottom = '1px solid #eee';
      h.innerHTML = `<div><strong>${escapeHtml(ev.title)}</strong></div><div class="muted">${escapeHtml(ev.startDatetime || '')}</div>`;
      highlights.appendChild(h);

      // select option
      const opt = document.createElement('option'); opt.value = ev.id; opt.textContent = ev.title; eventSelect.appendChild(opt);
    });

    // wire view/register buttons
    document.querySelectorAll('[data-id]').forEach(btn => btn.addEventListener('click', (e)=>{
      const id = e.currentTarget.dataset.id; showEventDetails(id);
    }));
    document.querySelectorAll('[data-register]').forEach(btn => btn.addEventListener('click', (e)=>{
      const id = e.currentTarget.getAttribute('data-register'); document.getElementById('eventSelect').value = id; showView('register');
    }));
  } catch (err) { console.error(err); showToast('Could not load events'); }
}

async function showEventDetails(id) {
  try {
    const res = await fetch('/api/events/' + id);
    if (res.status !== 200) { showToast('Event not found'); return; }
    const ev = await res.json();
    document.getElementById('evTitle').textContent = ev.title;
    document.getElementById('evMeta').textContent = `${ev.type} | ${ev.startDatetime || ''} | ${ev.venue || ''}`;
    document.getElementById('evDesc').textContent = ev.description || '';
    document.getElementById('evRules').textContent = ev.rules || 'Standard college rules apply.';
    document.getElementById('evCoordinators').textContent = ev.coordinators || 'TBA';
    document.getElementById('evPrizes').textContent = ev.prizes || 'Certificates & Medals';
    document.getElementById('evFee').textContent = ev.fee ? '₹'+ev.fee : 'Free';
    document.getElementById('evRegisterBtn').onclick = ()=>{ document.getElementById('eventSelect').value = ev.id; showView('register'); };
    showView('eventDetails');
  } catch (err) { console.error(err); showToast('Could not load event'); }
}

// Back button
document.getElementById('backToEvents').addEventListener('click', () => showView('events'));

// Registration form handling
const regForm = document.getElementById('regForm');
regForm && regForm.addEventListener('submit', async (e)=>{
  e.preventDefault();
  const payload = {
    studentName: document.getElementById('studentName').value.trim(),
    roll: document.getElementById('roll').value.trim(),
    yearBranch: document.getElementById('yearBranch').value.trim(),
    tickets: parseInt(document.getElementById('tickets').value || '1', 10),
    email: document.getElementById('email').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    id: parseInt(document.getElementById('eventSelect').value,10)
  };
  if (!payload.studentName || !payload.email || !payload.id) { showToast('Name, email and event are required'); return; }
  try {
    const res = await fetch('/api/register', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    if (data && data.success) { showToast('Registration successful'); regForm.reset(); showView('home'); }
    else showToast('Error: ' + (data.message||'Could not save'));
  } catch (err) { showToast('Network error'); console.error(err); }
});

document.getElementById('resetBtn') && document.getElementById('resetBtn').addEventListener('click', ()=> regForm.reset());

// Student dashboard lookup
document.getElementById('dashLookup') && document.getElementById('dashLookup').addEventListener('click', async ()=>{
  const email = document.getElementById('dashEmail').value.trim(); if (!email) { showToast('Enter email'); return; }
  try {
    const res = await fetch('/api/registrations?limit=100'); const all = await res.json();
    const mine = all.filter(r => r.email && r.email.toLowerCase() === email.toLowerCase());
    const out = document.getElementById('dashResults'); out.innerHTML = '';
    if (mine.length===0) out.innerHTML = '<div class="reg-item">No registrations found for this email.</div>';
    else mine.forEach(r => { 
        const d = document.createElement('div'); d.className='reg-item'; 
        d.innerHTML = `<div><strong>${escapeHtml(r.studentName)}</strong> <br> ${escapeHtml(r.eventName)}</div><div>${r.tickets} Tickets <br> <small>${escapeHtml(r.createdAt)}</small></div>`; out.appendChild(d); 
    });
  } catch (err) { showToast('Could not lookup'); }
});

// Admin login & dashboard
document.getElementById('adminBtn').addEventListener('click', ()=> showView('adminLogin'));
document.getElementById('adminLoginBtn') && document.getElementById('adminLoginBtn').addEventListener('click', async ()=>{
  const u = document.getElementById('adminUser').value.trim(); const p = document.getElementById('adminPass').value;
  try {
    const res = await fetch('/api/admin/login', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({username:u,password:p})});
    const data = await res.json();
    if (data && data.success) { showToast('Welcome Admin'); loadAdmin(); showView('adminDashboard'); }
    else showToast('Login failed');
  } catch (err) { showToast('Network error'); }
});

document.getElementById('adminLogout') && document.getElementById('adminLogout').addEventListener('click', async ()=>{
  // await fetch('/api/admin/logout'); // commented out as mock doesn't need it
  showToast('Logged out'); showView('home');
});

async function loadAdmin() {
  const area = document.getElementById('adminArea'); area.innerHTML = '';
  // provide event creation form and registrations list
  const form = document.createElement('div'); form.innerHTML = `
    <h3>Add New Event</h3>
    <div class="form-group"><input id="evTitle" placeholder="Event Title" /></div>
    <div class="form-row">
      <input id="evType" placeholder="Type (tech/cultural)" />
      <input id="evWhen" placeholder="Date & Time" />
    </div>
    <div class="form-group"><input id="evVenue" placeholder="Venue" /></div>
    <div class="form-group"><textarea id="evDesc" placeholder="Description"></textarea></div>
    <div class="actions"><button id="saveEvent" class="btn primary">Save Event</button></div>
    <hr style="margin:20px 0; border:0; border-top:1px solid #ccc;">
  `;
  area.appendChild(form);
  document.getElementById('saveEvent').addEventListener('click', async ()=>{
    const ev = { title: document.getElementById('evTitle').value, type: document.getElementById('evType').value, startDatetime: document.getElementById('evWhen').value, venue: document.getElementById('evVenue').value, description: document.getElementById('evDesc').value };
    try {
      showToast('Event Added (Simulated)');
      // In real app: const res = await fetch('/api/admin/events', ...);
      // For demo, we just refresh home
      showView('home');
    } catch (err) { showToast('Network error'); }
  });

  const regsTitle = document.createElement('h3'); regsTitle.textContent = 'All Registrations'; area.appendChild(regsTitle);
  const regsDiv = document.createElement('div'); regsDiv.id = 'adminRegs'; area.appendChild(regsDiv);
  async function loadAdminRegs(){
    try {
      const res = await fetch('/api/admin/registrations'); const data = await res.json(); regsDiv.innerHTML = ''; 
      if(data.length === 0) regsDiv.innerHTML = '<p>No registrations yet.</p>';
      data.forEach(r=>{ const d = document.createElement('div'); d.className='reg-item'; d.innerHTML = `<div><strong>${escapeHtml(r.studentName)}</strong> — ${escapeHtml(r.eventName)}</div><div>${r.tickets} tix • ${escapeHtml(r.email||'')}</div>`; regsDiv.appendChild(d); });
    } catch (err) { regsDiv.innerHTML = '<div class="reg-item">Could not load</div>'; }
  }
  loadAdminRegs();
}

// small helpers
function escapeHtml(s){ if(!s) return ''; return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }

// Theme Toggle logic
const themeSelect = document.getElementById('themeSelect');
themeSelect.addEventListener('change', ()=>{
  if(themeSelect.value === 'dark') document.body.classList.add('theme-dark');
  else document.body.classList.remove('theme-dark');
});

showView('home'); loadEvents();   