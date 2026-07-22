# LiteChat Backend Node

A production-ready Telegram OTP authentication and chat-fetching backend service. Bridges Node.js Express with Python Telethon via FastAPI to provide secure, reliable Telegram integration for mobile and desktop clients.

## Features

✅ **OTP Authentication**: Initiate and verify Telegram OTP codes  
✅ **Two-Factor Authentication**: Handle 2FA passwords gracefully  
✅ **Chat Retrieval**: Fetch up to 20 recent Telegram dialogs with unread counts  
✅ **Session Management**: In-memory session store with automatic cleanup  
✅ **Python Telethon Bridge**: FastAPI subprocess for Telegram operations  
✅ **Production Ready**: Custom Dockerfile, error handling, graceful shutdown  
✅ **Health Checks**: Status endpoints and service monitoring  

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Mobile/Desktop Client                     │
│                   (LiteChat Android App)                     │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP Requests
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Express Server (Node.js) - Port 3000            │
│  • API Routes (/api/send-code, /api/verify-code, /api/chats)│
│  • Session Management (in-memory store)                      │
│  • Request Validation & Error Handling                       │
│  • FastAPI Bridge Subprocess Management                      │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP (localhost:8000)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           FastAPI Bridge (Python) - Port 8000                │
│  • Telethon Client Management                                │
│  • Telegram OTP Handling                                     │
│  • Chat/Dialog Retrieval                                     │
│  • Session String Generation                                 │
└────────────────────────┬────────────────────────────────────┘
                         │ Telethon Protocol
                         ▼
                    Telegram Servers
```

## Quick Start

### Prerequisites

- Node.js 18+
- Python 3.8+
- pnpm

### Installation

```bash
# Clone the repository
git clone <repo-url>
cd litechat-backend-node

# Install dependencies
pnpm install
pip3 install -r requirements.txt
```

### Development

```bash
# Start the server (includes FastAPI bridge)
pnpm dev
```

The server will start on `http://localhost:3000` and the FastAPI bridge on `http://localhost:8000`.

### Production

```bash
# Build
pnpm build

# Start
pnpm start
```

### Docker

```bash
# Build image
docker build -t litechat-backend-node .

# Run container
docker run -p 3000:3000 litechat-backend-node
```

## API Endpoints

### Status
- `GET /` - Service status and available endpoints
- `GET /health` - Health check

### Authentication
- `POST /api/send-code` - Initiate OTP request
- `POST /api/verify-code` - Verify OTP and get session string

### Chats
- `POST /api/chats` - Fetch recent chats/dialogs

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for detailed endpoint documentation.

## Usage Example

### 1. Send OTP Code

```bash
curl -X POST http://localhost:3000/api/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "api_id": "123456",
    "api_hash": "abcdef1234567890abcdef1234567890",
    "phone": "+1234567890"
  }'
```

### 2. Verify OTP Code

```bash
curl -X POST http://localhost:3000/api/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+1234567890",
    "code": "12345"
  }'
```

### 3. Fetch Chats

```bash
curl -X POST http://localhost:3000/api/chats \
  -H "Content-Type: application/json" \
  -d '{
    "session_string": "1BPhvYswBu3vYq8Yv...",
    "api_id": "123456",
    "api_hash": "abcdef1234567890abcdef1234567890"
  }'
```

## Project Structure

```
litechat-backend-node/
├── server/
│   ├── index.ts                 # Main entry point
│   ├── telegram-bridge.ts       # Express server & session management
│   ├── telegram_bridge.py       # FastAPI Telethon bridge
│   └── ...
├── requirements.txt             # Python dependencies
├── Dockerfile                   # Production Docker image
├── API_DOCUMENTATION.md         # Detailed API docs
├── BACKEND_README.md            # This file
├── package.json                 # Node.js dependencies
└── ...
```

## Key Components

### Express Server (`telegram-bridge.ts`)

Handles HTTP requests, manages the in-memory session store, and proxies requests to the FastAPI bridge.

**Features:**
- Session timeout: 10 minutes
- Automatic cleanup every 5 minutes
- Graceful shutdown handling
- Comprehensive error handling

### FastAPI Bridge (`telegram_bridge.py`)

Manages Telethon clients and handles all Telegram operations.

**Features:**
- OTP code sending and verification
- 2FA password handling
- Chat/dialog retrieval (up to 20)
- Session string generation
- Automatic client cleanup on shutdown

## Configuration

### Environment Variables

```bash
PORT=3000              # Server port (default: 3000)
NODE_ENV=development   # Environment (development or production)
```

### Session Management

- **Timeout**: 10 minutes (configurable in `telegram-bridge.ts`)
- **Cleanup Interval**: 5 minutes
- **Max Chats**: 20 dialogs per request

## Error Handling

All errors return appropriate HTTP status codes and descriptive messages:

```json
{
  "error": "Error description",
  "details": "Additional details if available"
}
```

## Logging

The server logs all significant events:

```
[Server] Starting FastAPI bridge...
[FastAPI] Application starting up
[Server] FastAPI bridge started, starting Express server...
[Server] Express server listening on port 3000
[send-code] Initiating OTP for +1234567890
[verify-code] Successfully verified +1234567890
[get-chats] Fetched 15 chats
```

## Deployment Considerations

### Autoscale (Serverless)
- The service is designed for Autoscale deployment
- FastAPI bridge starts on each request
- Session store is in-memory (not persisted across instances)

### Reserved Hosting
- For persistent sessions, consider Reserved hosting
- Database-backed session store can be added if needed

### Custom Domain
- Configure custom domain in Manus Management UI
- Update client app with the new backend URL

## Troubleshooting

### FastAPI Bridge Not Starting
1. Ensure Python 3.8+ is installed: `python3 --version`
2. Install dependencies: `pip3 install -r requirements.txt`
3. Check for port conflicts on 8000

### Session Timeout Errors
- Sessions expire after 10 minutes
- Call `/api/send-code` again to start a new session

### Invalid Session String
- Session strings can be revoked on Telegram
- Authenticate again to get a new session string

### Port Already in Use
- Change `PORT` environment variable: `PORT=3001 pnpm dev`

## Performance

- **Send OTP**: ~2-3 seconds (Telegram API call)
- **Verify OTP**: ~2-3 seconds (Telegram API call)
- **Fetch Chats**: ~1-2 seconds (depends on number of dialogs)
- **Concurrent Requests**: Handles multiple simultaneous requests

## Security

- All requests are validated before processing
- Session store is in-memory (not exposed to clients)
- Session strings are generated by Telethon (secure)
- Graceful error handling (no sensitive data in error messages)
- HTTPS recommended for production (handled by deployment platform)

## Future Enhancements

- [ ] Database-backed session store for persistent deployments
- [ ] Message sending capability
- [ ] Real-time updates via WebSocket
- [ ] Rate limiting and API key authentication
- [ ] Caching layer for chat data
- [ ] Support for additional Telegram operations

## Support

For issues, questions, or contributions, please refer to the project repository.

## License

MIT
