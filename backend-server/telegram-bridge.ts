import express, { Request, Response } from 'express';
import { spawn, ChildProcess } from 'child_process';
import path from 'path';

const app = express();
app.use(express.json());

// In-memory session store for pending clients
interface PendingSession {
  apiId: string;
  apiHash: string;
  phone: string;
  phoneCodeHash: string;
  createdAt: number;
}

const pendingSessions = new Map<string, PendingSession>();
const SESSION_TIMEOUT = 10 * 60 * 1000; // 10 minutes

// FastAPI bridge process
let fastApiProcess: ChildProcess | null = null;
let fastApiReady = false;

// Health check for FastAPI bridge
async function healthCheckBridge(): Promise<void> {
  for (let i = 0; i < 5; i++) {
    try {
      const response = await fetch('http://127.0.0.1:8000/', { signal: AbortSignal.timeout(2000) });
      if (response.ok) {
        console.log('[FastAPI] Health check passed');
        return;
      }
    } catch (err) {
      console.log(`[FastAPI] Health check attempt ${i + 1}/5 failed, retrying...`);
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  }
  throw new Error('FastAPI bridge health check failed');
}

// Start FastAPI bridge subprocess
function startFastApiBridge(): Promise<void> {
  return new Promise((resolve, reject) => {
    const pythonScript = path.join(process.cwd(), 'server', 'telegram_bridge.py');
    
    fastApiProcess = spawn('python3', [pythonScript], {
      stdio: ['pipe', 'pipe', 'pipe'],
      env: { ...process.env, PYTHONUNBUFFERED: '1' }
    });

    let resolved = false;
    const timeout = setTimeout(() => {
      if (!resolved) {
        resolved = true;
        console.log('[FastAPI] Startup timeout reached, attempting health check...');
        healthCheckBridge().then(() => {
          fastApiReady = true;
          resolve();
        }).catch((err) => {
          reject(new Error('FastAPI bridge failed to start within timeout'));
        });
      }
    }, 8000);

    const checkReady = (data: string) => {
      if (data.includes('Application startup complete') || 
          data.includes('Uvicorn running on') ||
          data.includes('Started server process')) {
        if (!resolved) {
          resolved = true;
          clearTimeout(timeout);
          fastApiReady = true;
          resolve();
        }
      }
    };

    fastApiProcess.stdout?.on('data', (data) => {
      const output = data.toString().trim();
      console.log('[FastAPI]', output);
      checkReady(output);
    });

    fastApiProcess.stderr?.on('data', (data) => {
      const output = data.toString().trim();
      console.log('[FastAPI]', output);
      checkReady(output);
    });

    fastApiProcess.on('error', (err) => {
      if (!resolved) {
        resolved = true;
        clearTimeout(timeout);
        console.error('[FastAPI Process Error]', err);
        reject(err);
      }
    });

    fastApiProcess.on('exit', (code) => {
      console.log('[FastAPI] Process exited with code', code);
      fastApiReady = false;
      if (!resolved) {
        resolved = true;
        clearTimeout(timeout);
      }
    });
  });
}

// Clean up expired sessions
function cleanupExpiredSessions() {
  const now = Date.now();
  const entries = Array.from(pendingSessions.entries());
  for (const [phone, session] of entries) {
    if (now - session.createdAt > SESSION_TIMEOUT) {
      pendingSessions.delete(phone);
      console.log(`[Cleanup] Removed expired session for ${phone}`);
    }
  }
}

// Run cleanup every 5 minutes
setInterval(cleanupExpiredSessions, 5 * 60 * 1000);

// Status endpoint (HTML page)
app.get('/', (req: Request, res: Response) => {
  const statusHtml = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>LiteChat Backend Node</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f1115; color: #e0e7ff; }
    .container { max-width: 900px; margin: 0 auto; padding: 40px 20px; }
    h1 { font-size: 28px; margin-bottom: 10px; color: #818cf8; }
    .status { display: flex; align-items: center; gap: 10px; margin-bottom: 30px; }
    .status-badge { width: 12px; height: 12px; border-radius: 50%; background: ${fastApiReady ? '#10b981' : '#ef4444'}; }
    .status-text { font-size: 14px; color: #94a3b8; }
    .section { margin-bottom: 40px; }
    .section h2 { font-size: 18px; margin-bottom: 15px; color: #c7d2fe; }
    .endpoint { background: #11131a; border: 1px solid #1e293b; border-radius: 8px; padding: 15px; margin-bottom: 10px; }
    .endpoint-method { display: inline-block; padding: 4px 8px; background: #4f46e5; border-radius: 4px; font-size: 12px; font-weight: bold; margin-right: 10px; }
    .endpoint-path { font-family: 'Courier New', monospace; color: #818cf8; }
    .endpoint-desc { font-size: 13px; color: #94a3b8; margin-top: 5px; }
    .info { background: #11131a; border-left: 3px solid #4f46e5; padding: 15px; border-radius: 4px; margin-bottom: 15px; }
    .info-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
    .info-value { font-size: 14px; color: #e0e7ff; margin-top: 5px; font-family: 'Courier New', monospace; }
    footer { margin-top: 50px; padding-top: 20px; border-top: 1px solid #1e293b; font-size: 12px; color: #64748b; }
  </style>
</head>
<body>
  <div class="container">
    <h1>🚀 LiteChat Backend Node</h1>
    
    <div class="status">
      <div class="status-badge"></div>
      <div class="status-text">
        FastAPI Bridge: <strong>${fastApiReady ? 'Ready' : 'Initializing...'}</strong>
      </div>
    </div>

    <div class="section">
      <h2>Service Information</h2>
      <div class="info">
        <div class="info-label">Service</div>
        <div class="info-value">LiteChat Telegram Backend Node</div>
      </div>
      <div class="info">
        <div class="info-label">Version</div>
        <div class="info-value">1.0.0</div>
      </div>
      <div class="info">
        <div class="info-label">Uptime</div>
        <div class="info-value">${Math.floor(process.uptime())} seconds</div>
      </div>
    </div>

    <div class="section">
      <h2>API Endpoints</h2>
      
      <div class="endpoint">
        <div><span class="endpoint-method">POST</span><span class="endpoint-path">/api/send-code</span></div>
        <div class="endpoint-desc">Initiate Telegram OTP request</div>
      </div>

      <div class="endpoint">
        <div><span class="endpoint-method">POST</span><span class="endpoint-path">/api/verify-code</span></div>
        <div class="endpoint-desc">Verify OTP and get session string (supports 2FA)</div>
      </div>

      <div class="endpoint">
        <div><span class="endpoint-method">POST</span><span class="endpoint-path">/api/chats</span></div>
        <div class="endpoint-desc">Fetch recent chats/dialogs (up to 20)</div>
      </div>

      <div class="endpoint">
        <div><span class="endpoint-method">GET</span><span class="endpoint-path">/health</span></div>
        <div class="endpoint-desc">Health check endpoint</div>
      </div>
    </div>

    <footer>
      <p>📚 For detailed API documentation, see <code>API_DOCUMENTATION.md</code></p>
      <p>🔗 Status: ${fastApiReady ? 'Operational' : 'Initializing'}</p>
    </footer>
  </div>
</body>
</html>
  `;
  res.type('text/html').send(statusHtml);
});

// Health check endpoint
app.get('/health', (req: Request, res: Response) => {
  res.json({
    status: fastApiReady ? 'healthy' : 'initializing',
    fastApiReady,
    timestamp: new Date().toISOString()
  });
});

// Send OTP code endpoint
app.post('/api/send-code', async (req: Request, res: Response) => {
  try {
    const { api_id, api_hash, phone } = req.body;

    if (!api_id || !api_hash || !phone) {
      return res.status(400).json({ error: 'Missing required fields: api_id, api_hash, phone' });
    }

    if (!fastApiReady) {
      return res.status(503).json({ error: 'FastAPI bridge not ready' });
    }

    // Call FastAPI bridge
    const response = await fetch('http://127.0.0.1:8000/api/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ api_id, api_hash, phone })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json(data);
    }

    // Store pending session
    pendingSessions.set(phone, {
      apiId: api_id,
      apiHash: api_hash,
      phone,
      phoneCodeHash: data.phone_code_hash || '',
      createdAt: Date.now()
    });

    res.json({
      status: 'code_sent',
      phone,
      message: 'OTP code has been sent to your Telegram app'
    });
  } catch (error) {
    console.error('[send-code]', error);
    res.status(500).json({ error: 'Internal server error', details: String(error) });
  }
});

// Verify OTP code endpoint
app.post('/api/verify-code', async (req: Request, res: Response) => {
  try {
    const { phone, code, password } = req.body;

    if (!phone || !code) {
      return res.status(400).json({ error: 'Missing required fields: phone, code' });
    }

    if (!fastApiReady) {
      return res.status(503).json({ error: 'FastAPI bridge not ready' });
    }

    const pendingSession = pendingSessions.get(phone);
    if (!pendingSession) {
      return res.status(400).json({ error: 'No pending session for this phone number. Call /api/send-code first.' });
    }

    // Call FastAPI bridge
    const response = await fetch('http://127.0.0.1:8000/api/verify-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        phone,
        code,
        password: password || null,
        api_id: pendingSession.apiId,
        api_hash: pendingSession.apiHash
      })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json(data);
    }

    // Check if 2FA password is required
    if (data.status === 'requires_password') {
      return res.json({
        status: 'requires_password',
        message: 'Two-step verification is enabled. Please provide your cloud password.'
      });
    }

    // Clean up pending session on success
    pendingSessions.delete(phone);

    res.json({
      status: 'success',
      session_string: data.session_string,
      api_id: pendingSession.apiId,
      api_hash: pendingSession.apiHash,
      message: 'Successfully authenticated'
    });
  } catch (error) {
    console.error('[verify-code]', error);
    res.status(500).json({ error: 'Internal server error', details: String(error) });
  }
});

// Fetch chats endpoint
app.post('/api/chats', async (req: Request, res: Response) => {
  try {
    const { session_string, api_id, api_hash } = req.body;

    if (!session_string || !api_id || !api_hash) {
      return res.status(400).json({ error: 'Missing required fields: session_string, api_id, api_hash' });
    }

    if (!fastApiReady) {
      return res.status(503).json({ error: 'FastAPI bridge not ready' });
    }

    // Call FastAPI bridge
    const response = await fetch('http://127.0.0.1:8000/api/chats', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session_string, api_id, api_hash })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json(data);
    }

    res.json({
      chats: data.chats || []
    });
  } catch (error) {
    console.error('[chats]', error);
    res.status(500).json({ error: 'Internal server error', details: String(error) });
  }
});

// Error handling middleware
app.use((err: any, req: Request, res: Response, next: any) => {
  console.error('[Error]', err);
  res.status(500).json({ error: 'Internal server error' });
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('[Server] SIGTERM received, shutting down gracefully');
  if (fastApiProcess) {
    fastApiProcess.kill();
  }
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('[Server] SIGINT received, shutting down gracefully');
  if (fastApiProcess) {
    fastApiProcess.kill();
  }
  process.exit(0);
});

export async function startTelegramBridge(port: number = 3000) {
  try {
    console.log('[Server] Starting FastAPI bridge...');
    await startFastApiBridge();
    
    console.log('[Server] FastAPI bridge started, starting Express server...');
    app.listen(port, () => {
      console.log(`[Server] Express server listening on port ${port}`);
      console.log(`[Server] Status page: http://localhost:${port}/`);
    });
  } catch (error) {
    console.error('[Server] Failed to start:', error);
    process.exit(1);
  }
}

export default app;
