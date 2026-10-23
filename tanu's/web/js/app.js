/**
 * CCRRS Web Application Frontend Controller
 * Connects modern Web UI to Core Java DSA Backend REST APIs
 */

const API_BASE = (typeof window !== 'undefined' && (window.location.protocol === 'http:' || window.location.protocol === 'https:'))
    ? (window.location.origin + '/api')
    : 'http://localhost:8080/api';

// Application State
let activeTab = 'overview';
let complaintsData = [];
let queueData = [];
let graphData = { vertices: [], edges: [] };
let selectedCaseForModal = null;

// Graph Visualization Engine State
let canvas, ctx;
let graphNodes = [];
let isDragging = false;
let draggedNode = null;
let highlightedNodeIds = new Set();
let highlightedPath = [];

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    setupTabNavigation();
    setupLivePriorityCalculator();
    setupComplaintIntakeForm();
    setupLedgerAndQueueControls();
    setupCategoryTreeControls();
    setupGraphVisualization();
    setupSearchAndSortControls();
    setupAlgorithmicDecisionControls();
    setupStatusModalControls();

    // Initial Data Fetch
    fetchDashboardData();
    fetchGraphData();
});

// =============================================================================
// 1. Navigation & Data Loading
// =============================================================================
function setupTabNavigation() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            activeTab = btn.dataset.tab;
            const targetPane = document.getElementById(`tab-${activeTab}`);
            if (targetPane) targetPane.classList.add('active');

            // Tab-specific initializers
            if (activeTab === 'tree') fetchCategoryTree('preorder');
            if (activeTab === 'graph') renderGraph();
            if (activeTab === 'decisions') {
                runGreedyAllocation();
                runKnapsackOptimization();
                runBacktrackingSearch();
            }
        });
    });
}

async function fetchDashboardData() {
    try {
        const [complaintsRes, queueRes] = await Promise.all([
            fetch(`${API_BASE}/complaints`),
            fetch(`${API_BASE}/queue`)
        ]);

        if (complaintsRes.ok) complaintsData = await complaintsRes.json();
        if (queueRes.ok) queueData = await queueRes.json();

        updateHeaderAndOverviewMetrics();
        renderRecentCasesTable();
        renderLedgerTable();
    } catch (err) {
        console.warn('Backend offline or loading:', err);
    }
}

function updateHeaderAndOverviewMetrics() {
    const totalCases = complaintsData.length;
    const queueSize = queueData.length;
    const totalLoss = complaintsData.reduce((acc, c) => acc + (c.financialLoss || 0), 0);

    document.getElementById('hdrTotalCases').textContent = totalCases;
    document.getElementById('hdrQueueSize').textContent = queueSize;
    document.getElementById('hdrTotalLoss').textContent = `₹${totalLoss.toLocaleString('en-IN')}`;

    document.getElementById('metricActiveList').textContent = totalCases;
    document.getElementById('metricQueueDepth').textContent = queueSize;
    document.getElementById('metricBstNodes').textContent = totalCases;
    document.getElementById('queueCounter').textContent = `${queueSize} Cases Waiting`;
}

// =============================================================================
// 2. Citizen Intake & Live Postfix Stack Evaluator
// =============================================================================
function setupLivePriorityCalculator() {
    const sevInput = document.getElementById('severity');
    const vicInput = document.getElementById('victimCount');
    const lossInput = document.getElementById('financialLoss');

    const updateLiveScore = () => {
        const sev = parseInt(sevInput.value) || 1;
        const victims = parseInt(vicInput.value) || 1;
        const loss = parseFloat(lossInput.value) || 0;

        document.getElementById('valSeverity').textContent = sev;
        document.getElementById('valVictims').textContent = victims;
        document.getElementById('valLoss').textContent = loss.toLocaleString('en-IN');

        // Postfix Calculation Formula:
        // (severity * 3) + (min(loss / 10000, 50) * 4) + (victims * 2)
        const normalizedLoss = Math.min(loss / 10000.0, 50.0);
        const score = (sev * 3) + (normalizedLoss * 4) + (victims * 2);

        document.getElementById('livePriority').textContent = score.toFixed(1);

        const badge = document.getElementById('urgencyBadge');
        if (score >= 150) {
            badge.textContent = 'CRITICAL SEVERITY';
            badge.style.borderColor = '#ef4444';
            badge.style.color = '#ef4444';
        } else if (score >= 70) {
            badge.textContent = 'HIGH URGENCY';
            badge.style.borderColor = '#f59e0b';
            badge.style.color = '#f59e0b';
        } else {
            badge.textContent = 'MODERATE';
            badge.style.borderColor = '#10b981';
            badge.style.color = '#10b981';
        }

        // Display Postfix tokens representation
        const tokensBox = document.getElementById('postfixTokenFlow');
        tokensBox.innerHTML = `
            <span class="token">${sev}</span>
            <span class="token">3</span>
            <span class="token op">*</span>
            <span class="token">${normalizedLoss.toFixed(1)}</span>
            <span class="token">4</span>
            <span class="token op">*</span>
            <span class="token op">+</span>
            <span class="token">${victims}</span>
            <span class="token">2</span>
            <span class="token op">*</span>
            <span class="token op">+</span>
            <span class="token op">=</span>
            <span class="token" style="color:#38bdf8;font-weight:700;">${score.toFixed(1)}</span>
        `;
    };

    sevInput.addEventListener('input', updateLiveScore);
    vicInput.addEventListener('input', updateLiveScore);
    lossInput.addEventListener('input', updateLiveScore);
    updateLiveScore();
}

function setupComplaintIntakeForm() {
    const form = document.getElementById('complaintForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const payload = {
            complainantName: document.getElementById('complainantName').value.trim(),
            phone: document.getElementById('phone').value.trim(),
            email: document.getElementById('email').value.trim(),
            city: document.getElementById('city').value.trim(),
            category: document.getElementById('category').value,
            description: document.getElementById('description').value.trim(),
            severity: document.getElementById('severity').value,
            financialLoss: document.getElementById('financialLoss').value,
            victimCount: document.getElementById('victimCount').value
        };

        try {
            const res = await fetch(`${API_BASE}/complaints`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (data.success) {
                alert(`✓ Complaint Registered Successfully!\nCase ID: ${data.caseId}\nPriority Score: ${data.priorityScore}`);
                form.reset();
                document.getElementById('financialLoss').value = 75000;
                document.getElementById('severity').value = 7;
                document.getElementById('victimCount').value = 1;
                document.getElementById('valSeverity').textContent = '7';
                document.getElementById('valVictims').textContent = '1';
                document.getElementById('valLoss').textContent = '75,000';

                await fetchDashboardData();
                document.querySelector('.nav-btn[data-tab="ledger"]').click();
            } else {
                alert(`Error: ${data.error || 'Failed to register'}`);
            }
        } catch (err) {
            alert(`Error connecting to server: ${err.message}`);
        }
    });
}

// =============================================================================
// 3. Ledger, Queue & Status Undo
// =============================================================================
function setupLedgerAndQueueControls() {
    document.getElementById('btnRefreshLedger').addEventListener('click', fetchDashboardData);

    document.getElementById('btnDequeue').addEventListener('click', async () => {
        try {
            const res = await fetch(`${API_BASE}/queue/dequeue`, { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                alert(`⚡ Dequeued: ${data.case.caseId} (${data.case.complainantName})\nStatus updated to UNDER_INVESTIGATION`);
                await fetchDashboardData();
            } else {
                alert(data.error || 'Queue is currently empty.');
            }
        } catch (err) {
            alert(`Network error: ${err.message}`);
        }
    });
}

function renderRecentCasesTable() {
    const tbody = document.querySelector('#recentCasesTable tbody');
    tbody.innerHTML = '';
    const topCases = [...complaintsData].sort((a, b) => b.priorityScore - a.priorityScore).slice(0, 5);

    topCases.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${c.caseId}</strong></td>
            <td>${c.complainantName} (${c.city})</td>
            <td>${c.category}</td>
            <td><span class="tag tag-ll">${c.severity}/10</span></td>
            <td>₹${c.financialLoss.toLocaleString('en-IN')}</td>
            <td><span class="stat-value" style="font-size:12px;">${c.priorityScore.toFixed(1)}</span></td>
            <td><span class="badge-status ${c.status.toLowerCase()}">${c.status}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

function renderLedgerTable() {
    const tbody = document.querySelector('#ledgerTable tbody');
    tbody.innerHTML = '';

    complaintsData.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${c.caseId}</strong></td>
            <td>${c.complainantName}</td>
            <td>${c.city}</td>
            <td>${c.category}</td>
            <td><strong style="color:var(--accent-blue);">${c.priorityScore.toFixed(1)}</strong></td>
            <td>₹${c.financialLoss.toLocaleString('en-IN')}</td>
            <td><span class="badge-status ${c.status.toLowerCase()}">${c.status}</span></td>
            <td><span class="tag tag-stack">Depth: ${c.stackDepth || 1}</span></td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="openStatusModal('${c.caseId}')">⚙️ Manage / Undo</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// =============================================================================
// 4. Case Status & LIFO Undo Modal
// =============================================================================
function setupStatusModalControls() {
    const modal = document.getElementById('statusModal');
    document.getElementById('btnCloseModal').addEventListener('click', () => modal.classList.remove('active'));

    document.getElementById('btnSubmitStatusUpdate').addEventListener('click', async () => {
        if (!selectedCaseForModal) return;
        const newStatus = document.getElementById('modalNewStatus').value;
        const officer = document.getElementById('modalOfficer').value.trim() || 'Officer Assigned';
        const remarks = document.getElementById('modalRemarks').value.trim() || 'Status updated via web dashboard';

        try {
            const res = await fetch(`${API_BASE}/cases/status`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ caseId: selectedCaseForModal.caseId, status: newStatus, officer, remarks })
            });
            const data = await res.json();
            if (data.success) {
                alert(`✓ Status updated to ${newStatus}. Transition pushed onto LIFO Undo Stack (Depth: ${data.stackDepth})`);
                await fetchDashboardData();
                openStatusModal(selectedCaseForModal.caseId);
            }
        } catch (err) {
            alert('Error updating status: ' + err.message);
        }
    });

    document.getElementById('btnModalUndo').addEventListener('click', async () => {
        if (!selectedCaseForModal) return;
        try {
            const res = await fetch(`${API_BASE}/cases/undo`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ caseId: selectedCaseForModal.caseId })
            });
            const data = await res.json();
            if (data.success) {
                const feedback = document.getElementById('undoFeedback');
                feedback.textContent = `↩️ Pop Success: Reverted to '${data.revertedStatus}'. (Stack Depth: ${data.stackDepth})`;
                await fetchDashboardData();
                openStatusModal(selectedCaseForModal.caseId);
            } else {
                document.getElementById('undoFeedback').textContent = `[!] ${data.error}`;
            }
        } catch (err) {
            alert('Error executing undo: ' + err.message);
        }
    });
}

window.openStatusModal = function(caseId) {
    const c = complaintsData.find(x => x.caseId === caseId);
    if (!c) return;
    selectedCaseForModal = c;

    document.getElementById('modalTitle').textContent = `Case Inspector: ${c.caseId}`;
    document.getElementById('modalCaseSummary').innerHTML = `
        <strong>${c.complainantName}</strong> | Category: ${c.category} | Priority: <strong>${c.priorityScore.toFixed(1)}</strong><br>
        Current Status: <span class="badge-status ${c.status.toLowerCase()}">${c.status}</span> | LIFO Stack Depth: <strong>${c.stackDepth || 1}</strong><br>
        Assigned: ${c.assignedOfficer}
    `;
    document.getElementById('modalNewStatus').value = c.status;
    document.getElementById('undoFeedback').textContent = '';
    document.getElementById('statusModal').classList.add('active');
};

// =============================================================================
// 5. Category Binary Tree Visualizer
// =============================================================================
function setupCategoryTreeControls() {
    document.querySelectorAll('[data-trav]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-trav]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            fetchCategoryTree(btn.dataset.trav);
        });
    });
}

async function fetchCategoryTree(traversalType = 'preorder') {
    try {
        const res = await fetch(`${API_BASE}/tree`);
        if (!res.ok) return;
        const data = await res.json();

        // Render Traversal Sequence Badges
        const sequence = data[traversalType] || [];
        const resultBox = document.getElementById('traversalResult');
        resultBox.innerHTML = sequence.map((node, i) => `
            <span class="tag tag-tree" style="margin: 3px; font-size: 11px;">
                #${i+1}: [${node.code}] ${node.name} (₹${(node.loss || 0).toLocaleString('en-IN')})
            </span>
        `).join(' → ');

        // Render Graphical Hierarchy
        renderTreeRecursive(data.tree);
    } catch (err) {
        console.warn('Tree error:', err);
    }
}

function renderTreeRecursive(treeData) {
    const container = document.getElementById('treeVisualContainer');
    container.innerHTML = '';

    function buildNodeHtml(node) {
        if (!node) return '';
        return `
            <div style="display: flex; flex-direction: column; align-items: center; margin: 10px;">
                <div class="tree-node-card">
                    <div class="tree-node-code">${node.code}</div>
                    <div class="tree-node-title">${node.name}</div>
                    <div class="tree-node-meta">Cases: ${node.cases} | Damage: ₹${(node.loss || 0).toLocaleString('en-IN')}</div>
                </div>
                ${(node.left || node.right) ? `
                    <div style="display: flex; justify-content: center; gap: 40px; margin-top: 15px; position: relative;">
                        ${buildNodeHtml(node.left)}
                        ${buildNodeHtml(node.right)}
                    </div>
                ` : ''}
            </div>
        `;
    }

    container.innerHTML = buildNodeHtml(treeData);
}

// =============================================================================
// 6. Suspect Network Graph (HTML5 Canvas + BFS/DFS)
// =============================================================================
function setupGraphVisualization() {
    canvas = document.getElementById('networkCanvas');
    ctx = canvas.getContext('2d');

    // Canvas Mouse Interaction for dragging nodes
    canvas.addEventListener('mousedown', (e) => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        for (let node of graphNodes) {
            const dist = Math.hypot(node.x - x, node.y - y);
            if (dist <= node.radius + 4) {
                isDragging = true;
                draggedNode = node;
                break;
            }
        }
    });

    canvas.addEventListener('mousemove', (e) => {
        if (isDragging && draggedNode) {
            const rect = canvas.getBoundingClientRect();
            draggedNode.x = e.clientX - rect.left;
            draggedNode.y = e.clientY - rect.top;
            renderGraph();
        }
    });

    window.addEventListener('mouseup', () => {
        isDragging = false;
        draggedNode = null;
    });

    // BFS Button
    document.getElementById('btnRunBFS').addEventListener('click', async () => {
        const startId = document.getElementById('bfsStartNode').value;
        const hops = document.getElementById('bfsHops').value;

        const res = await fetch(`${API_BASE}/graph/bfs?start=${startId}&hops=${hops}`);
        const data = await res.json();

        highlightedNodeIds.clear();
        highlightedPath = [];
        data.connected.forEach(item => highlightedNodeIds.add(item.id));

        const consoleBox = document.getElementById('graphOutputConsole');
        consoleBox.innerHTML = `<strong>BFS Exploration from '${startId}' (${hops} Hops):</strong> Found ${data.connected.length} linked entities.<br>` +
            data.connected.map(c => `• [Hop ${c.hop}] ${c.id} (${c.type}): ${c.name}`).join('<br>');

        renderGraph();
    });

    // DFS Button
    document.getElementById('btnRunDFS').addEventListener('click', async () => {
        const fromId = document.getElementById('dfsFromNode').value;
        const toId = document.getElementById('dfsToNode').value;

        const res = await fetch(`${API_BASE}/graph/dfs?from=${fromId}&to=${toId}`);
        const data = await res.json();

        highlightedNodeIds.clear();
        highlightedPath = data.path.map(p => p.id);
        data.path.forEach(p => highlightedNodeIds.add(p.id));

        const consoleBox = document.getElementById('graphOutputConsole');
        if (data.found) {
            consoleBox.innerHTML = `<strong>DFS Path Found (${data.path.length - 1} hops):</strong><br>` +
                data.path.map((p, i) => `Step ${i + 1}: ${p.id} (${p.type} - ${p.name})`).join(' → ');
        } else {
            consoleBox.innerHTML = `[-] No connecting path found between ${fromId} and ${toId}.`;
        }

        renderGraph();
    });

    document.getElementById('btnResetGraph').addEventListener('click', () => {
        highlightedNodeIds.clear();
        highlightedPath = [];
        document.getElementById('graphOutputConsole').textContent = 'Graph reset to default state.';
        renderGraph();
    });
}

async function fetchGraphData() {
    try {
        const res = await fetch(`${API_BASE}/graph`);
        if (!res.ok) return;
        graphData = await res.json();

        document.getElementById('metricGraphNodes').textContent = graphData.vertices.length;

        // Populate Select dropdowns
        const bfsSelect = document.getElementById('bfsStartNode');
        const dfsFrom = document.getElementById('dfsFromNode');
        const dfsTo = document.getElementById('dfsToNode');

        bfsSelect.innerHTML = '';
        dfsFrom.innerHTML = '';
        dfsTo.innerHTML = '';

        graphData.vertices.forEach(v => {
            const opt = `<option value="${v.id}">${v.id} (${v.type})</option>`;
            bfsSelect.innerHTML += opt;
            dfsFrom.innerHTML += opt;
            dfsTo.innerHTML += opt;
        });

        if (dfsTo.options.length > 3) dfsTo.selectedIndex = 3;

        // Position nodes on circle
        const cx = canvas.width / 2;
        const cy = canvas.height / 2;
        const radius = Math.min(cx, cy) - 60;

        graphNodes = graphData.vertices.map((v, i) => {
            const angle = (i / graphData.vertices.length) * 2 * Math.PI;
            return {
                id: v.id,
                name: v.name,
                type: v.type,
                x: cx + radius * Math.cos(angle) + (Math.random() * 20 - 10),
                y: cy + radius * Math.sin(angle) + (Math.random() * 20 - 10),
                radius: v.type === 'SUSPECT' ? 20 : 16
            };
        });

        renderGraph();
    } catch (err) {
        console.warn('Graph load error:', err);
    }
}

function renderGraph() {
    if (!ctx) return;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const nodeMap = new Map();
    graphNodes.forEach(n => nodeMap.set(n.id, n));

    // 1. Draw Edges
    graphData.edges.forEach(edge => {
        const u = nodeMap.get(edge.source);
        const v = nodeMap.get(edge.target);
        if (!u || !v) return;

        // Check if edge is in DFS path
        const isPathEdge = isEdgeInPath(edge.source, edge.target);

        ctx.beginPath();
        ctx.moveTo(u.x, u.y);
        ctx.lineTo(v.x, v.y);
        ctx.strokeStyle = isPathEdge ? '#ef4444' : '#374151';
        ctx.lineWidth = isPathEdge ? 3 : 1;
        ctx.stroke();

        // Edge label
        const midX = (u.x + v.x) / 2;
        const midY = (u.y + v.y) / 2;
        ctx.fillStyle = '#6b7280';
        ctx.font = '9px JetBrains Mono';
        ctx.fillText(edge.relation, midX, midY - 2);
    });

    // 2. Draw Nodes
    graphNodes.forEach(node => {
        const isHighlighted = highlightedNodeIds.has(node.id);

        ctx.beginPath();
        ctx.arc(node.x, node.y, node.radius, 0, 2 * Math.PI);

        // Fill color based on type
        switch (node.type) {
            case 'SUSPECT': ctx.fillStyle = '#ef4444'; break;
            case 'DEVICE_IMEI': ctx.fillStyle = '#f59e0b'; break;
            case 'IP_ADDRESS': ctx.fillStyle = '#38bdf8'; break;
            case 'BANK_ACCOUNT': ctx.fillStyle = '#10b981'; break;
            case 'PHONE_NUMBER': ctx.fillStyle = '#8b5cf6'; break;
            default: ctx.fillStyle = '#9ca3af';
        }

        ctx.fill();

        // Glow or stroke if highlighted
        if (isHighlighted) {
            ctx.lineWidth = 4;
            ctx.strokeStyle = '#ffffff';
            ctx.stroke();
        } else {
            ctx.lineWidth = 1;
            ctx.strokeStyle = '#111827';
            ctx.stroke();
        }

        // Node ID Text
        ctx.fillStyle = '#f9fafb';
        ctx.font = '10px JetBrains Mono';
        ctx.textAlign = 'center';
        ctx.fillText(node.id, node.x, node.y + node.radius + 12);
    });
}

function isEdgeInPath(u, v) {
    if (highlightedPath.length < 2) return false;
    for (let i = 0; i < highlightedPath.length - 1; i++) {
        if ((highlightedPath[i] === u && highlightedPath[i+1] === v) ||
            (highlightedPath[i] === v && highlightedPath[i+1] === u)) {
            return true;
        }
    }
    return false;
}

// =============================================================================
// 7. Search & QuickSort Triage
// =============================================================================
function setupSearchAndSortControls() {
    // Search
    document.getElementById('btnExecuteSearch').addEventListener('click', async () => {
        const query = document.getElementById('searchInput').value.trim();
        const type = document.querySelector('input[name="searchType"]:checked').value;
        if (!query) return;

        const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}&type=${type}`);
        const data = await res.json();

        const benchBox = document.getElementById('searchBenchmarkBox');
        benchBox.style.display = 'block';
        benchBox.innerHTML = `<strong>Search Execution:</strong> Retrieved in <code>${data.timeNanos.toLocaleString()} ns</code> using <code>${data.type.toUpperCase()}</code>. Found ${data.matches.length} result(s).`;

        const resultsArea = document.getElementById('searchResultsArea');
        if (data.matches.length === 0) {
            resultsArea.innerHTML = '<p class="text-muted">No complaints matched your search query.</p>';
        } else {
            resultsArea.innerHTML = data.matches.map(c => `
                <div class="card mt-1" style="background:#161b22;">
                    <strong>[${c.caseId}] ${c.complainantName}</strong> (${c.city}) - Priority: <strong>${c.priorityScore.toFixed(1)}</strong><br>
                    <span class="text-muted">${c.category} | ${c.description} | ₹${c.financialLoss.toLocaleString('en-IN')}</span>
                </div>
            `).join('');
        }
    });

    // QuickSort
    document.getElementById('btnExecuteQuickSort').addEventListener('click', async () => {
        const res = await fetch(`${API_BASE}/sort`);
        const data = await res.json();

        document.getElementById('quickSortBenchmark').innerHTML =
            `⚡ QuickSort Partitioning executed in <code>${data.executionTimeNanos.toLocaleString()} ns</code>. Reordered by priority descending:`;

        const tbody = document.querySelector('#sortedCasesTable tbody');
        tbody.innerHTML = '';

        data.sortedCases.forEach((c, idx) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${idx + 1}</strong></td>
                <td>${c.caseId}</td>
                <td>${c.complainantName}</td>
                <td>${c.category}</td>
                <td><strong style="color:var(--accent-blue);">${c.priorityScore.toFixed(1)}</strong></td>
                <td>₹${c.financialLoss.toLocaleString('en-IN')}</td>
            `;
            tbody.appendChild(tr);
        });
    });
}

// =============================================================================
// 8. Algorithmic Decisions (Greedy, DP Knapsack, Backtracking)
// =============================================================================
function setupAlgorithmicDecisionControls() {
    document.getElementById('btnRunGreedy').addEventListener('click', runGreedyAllocation);

    const budgetSlider = document.getElementById('knapsackBudgetInput');
    budgetSlider.addEventListener('input', () => {
        document.getElementById('knapsackBudgetVal').textContent = budgetSlider.value;
    });
    document.getElementById('btnRunKnapsack').addEventListener('click', runKnapsackOptimization);

    document.getElementById('btnRunBacktracking').addEventListener('click', runBacktrackingSearch);
}

async function runGreedyAllocation() {
    const res = await fetch(`${API_BASE}/algorithms/greedy?days=7`);
    const data = await res.json();

    const container = document.getElementById('greedyScheduleResult');
    container.innerHTML = `
        <div class="mb-2"><strong>Total Cases Scheduled On-Time:</strong> ${data.totalScheduled} | <strong>Total Priority Payoff:</strong> ${data.totalPriority.toFixed(1)}</div>
        <table class="data-table">
            <thead><tr><th>Day Slot</th><th>Case ID</th><th>Complainant</th><th>Priority</th><th>Assigned Officer</th></tr></thead>
            <tbody>
                ${data.slots.map(s => `
                    <tr>
                        <td>Day ${s.day}</td>
                        <td>${s.case ? s.case.caseId : '-'}</td>
                        <td>${s.case ? s.case.complainantName : 'FREE'}</td>
                        <td>${s.case ? s.case.priorityScore.toFixed(1) : '-'}</td>
                        <td>${s.case ? s.case.assignedOfficer : '-'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function runKnapsackOptimization() {
    const budget = document.getElementById('knapsackBudgetInput').value;
    const res = await fetch(`${API_BASE}/algorithms/knapsack?budget=${budget}`);
    const data = await res.json();

    const container = document.getElementById('knapsackResult');
    container.innerHTML = `
        <div class="mb-2">
            <strong>Allocated:</strong> ₹${data.totalCost},000 / ₹${data.budgetLimit},000 |
            <strong>Surplus:</strong> ₹${data.remainingBudget},000 |
            <strong>Resolution Payoff:</strong> ${data.totalResolutionValue} pts
        </div>
        <table class="data-table">
            <thead><tr><th>Case ID</th><th>Category</th><th>Forensic Cost</th><th>Resolution Value</th></tr></thead>
            <tbody>
                ${data.fundedCases.map(c => `
                    <tr>
                        <td>${c.caseId}</td>
                        <td>${c.category}</td>
                        <td>₹${c.investigationCost},000</td>
                        <td><strong>${c.expectedResolutionValue} pts</strong></td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function runBacktrackingSearch() {
    const subnet = document.getElementById('btSubnet').value;
    const device = document.getElementById('btDevice').value;
    const pattern = document.getElementById('btPattern').value;
    const minRisk = document.getElementById('btMinRisk').value;
    const groupSize = document.getElementById('btSize').value;

    const res = await fetch(`${API_BASE}/algorithms/backtracking?subnet=${subnet}&device=${device}&pattern=${pattern}&minRisk=${minRisk}&groupSize=${groupSize}`);
    const data = await res.json();

    const container = document.getElementById('backtrackingResult');
    container.innerHTML = `
        <div class="mb-2">
            <span class="tag tag-bst">Search Metrics</span>
            Explored: <strong>${data.metrics.explored}</strong> nodes |
            Early Branches Pruned: <strong>${data.metrics.pruned}</strong>
        </div>
        <h4>Shortlisted Matching Suspects (${data.shortlisted.length}):</h4>
        ${data.shortlisted.map(s => `
            <div class="card mt-1" style="background:#161b22; font-size:11px;">
                <strong>[${s.id}] ${s.name}</strong> (${s.city}) | Subnet: ${s.ip} | Device: ${s.device} | Pattern: ${s.pattern} | Risk: <strong>${s.risk}</strong>
            </div>
        `).join('')}

        <h4 class="mt-2">Syndicate Groups Found (${data.syndicates.length}):</h4>
        ${data.syndicates.map((grp, i) => `
            <div class="tag tag-graph" style="display:inline-block; margin:4px; font-size:11px;">
                Group #${i+1}: ${grp.map(s => s.name).join(' & ')}
            </div>
        `).join('')}
    `;
}
