# LiteChat Backend Node - API Documentation

## Overview

The LiteChat Backend Node is a Telegram OTP authentication and chat-fetching service. It bridges a Node.js Express server with a Python Telethon runtime via FastAPI, providing a robust API for mobile and desktop clients to authenticate with Telegram and retrieve chat data.

## Architecture

- **Express Server (Node.js)**: Handles HTTP requests, manages session state, and proxies requests to the FastAPI bridge
- **FastAPI Bridge (Python)**: Manages Telethon clients, handles Telegram authentication, and retrieves chat data
- **Session Store**: In-memory storage for pending authentication sessions (10-minute timeout)

## Base URL

```
http://localhost:3000
```

## Endpoints

### 1. Status & Health

#### GET `/`
Returns the service status and available endpoints.

**Response:**
```json
{
  "status": "ok",
  "service": "LiteChat Telegram Backend Node",
  "version": "1.0.0",
  "fastApiReady": true,
  "uptime": 123.456,
  "endpoints": {
    "POST /api/send-code": "Initiate Telegram OTP request",
    "POST /api/verify-code": "Verify OTP and get session string",
    "POST /api/chats": "Fetch recent chats/dialogs",
    "GET /health": "Health check"
  }
}
```

#### GET `/health`
Quick health check endpoint.

**Response:**
```json
{
  "status": "healthy",
  "fastApiReady": true,
  "timestamp": "2026-07-22T17:30:00.000Z"
}
```

---

### 2. Authentication Endpoints

#### POST `/api/send-code`
Initiates a Telegram OTP request. Sends an authentication code to the user's Telegram app.

**Request Body:**
```json
{
  "api_id": "123456",
  "api_hash": "abcdef1234567890abcdef1234567890",
  "phone": "+1234567890"
}
```

**Response (Success - 200):**
```json
{
  "status": "code_sent",
  "phone": "+1234567890",
  "message": "OTP code has been sent to your Telegram app"
}
```

**Response (Error - 400):**
```json
{
  "error": "Missing required fields: api_id, api_hash, phone"
}
```

**Response (Error - 503):**
```json
{
  "error": "FastAPI bridge not ready"
}
```

---

#### POST `/api/verify-code`
Verifies the OTP code and returns a session string. Handles two-factor authentication if enabled.

**Request Body:**
```json
{
  "phone": "+1234567890",
  "code": "12345",
  "password": null
}
```

**Response (Success - 200):**
```json
{
  "status": "success",
  "session_string": "1BPhvYswBu3vYq8Yv...",
  "api_id": "123456",
  "api_hash": "abcdef1234567890abcdef1234567890",
  "message": "Successfully authenticated"
}
```

**Response (2FA Required - 200):**
```json
{
  "status": "requires_password",
  "message": "Two-step verification is enabled. Please provide your cloud password."
}
```

**Retry with 2FA Password:**
```json
{
  "phone": "+1234567890",
  "code": "12345",
  "password": "your_2fa_password"
}
```

**Response (Error - 400):**
```json
{
  "error": "No pending session for this phone number. Call /api/send-code first."
}
```

---

### 3. Chat Endpoints

#### POST `/api/chats`
Fetches the user's recent Telegram chats/dialogs (up to 20).

**Request Body:**
```json
{
  "session_string": "1BPhvYswBu3vYq8Yv...",
  "api_id": "123456",
  "api_hash": "abcdef1234567890abcdef1234567890"
}
```

**Response (Success - 200):**
```json
{
  "chats": [
    {
      "id": 123456789,
      "name": "John Doe",
      "username": "johndoe",
      "phone": "+1234567890",
      "unread_count": 3,
      "last_message": "Hey, how are you?"
    },
    {
      "id": 987654321,
      "name": "Group Chat",
      "username": "No username",
      "phone": "Private",
      "unread_count": 0,
      "last_message": "See you later!"
    }
  ]
}
```

**Response (Error - 400):**
```json
{
  "error": "Invalid session string"
}
```

---

## Authentication Flow

### Step 1: Send OTP Code
```bash
curl -X POST http://localhost:3000/api/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "api_id": "123456",
    "api_hash": "abcdef1234567890abcdef1234567890",
    "phone": "+1234567890"
  }'
```

### Step 2: Verify OTP Code
User receives code in Telegram app and provides it.

```bash
curl -X POST http://localhost:3000/api/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+1234567890",
    "code": "12345"
  }'
```

### Step 3: Handle 2FA (if required)
If the response indicates `requires_password`, retry with the 2FA password:

```bash
curl -X POST http://localhost:3000/api/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+1234567890",
    "code": "12345",
    "password": "your_2fa_password"
  }'
```

### Step 4: Fetch Chats
Use the returned `session_string` to fetch chats:

```bash
curl -X POST http://localhost:3000/api/chats \
  -H "Content-Type: application/json" \
  -d '{
    "session_string": "1BPhvYswBu3vYq8Yv...",
    "api_id": "123456",
    "api_hash": "abcdef1234567890abcdef1234567890"
  }'
```

---

## Error Handling

All errors return appropriate HTTP status codes:

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 400 | Bad Request (missing fields, invalid input, Telegram error) |
| 500 | Internal Server Error |
| 503 | Service Unavailable (FastAPI bridge not ready) |

---

## Session Management

- **Session Timeout**: Pending sessions expire after 10 minutes
- **Automatic Cleanup**: Expired sessions are cleaned up every 5 minutes
- **Session String**: The `session_string` returned from `/api/verify-code` can be reused indefinitely (until revoked on Telegram)

---

## Getting Telegram API Credentials

1. Go to [my.telegram.org](https://my.telegram.org)
2. Log in with your Telegram account
3. Navigate to "API development tools"
4. Create a new application
5. Copy your `api_id` and `api_hash`

---

## Deployment

### Local Development
```bash
pnpm install
pnpm dev
```

### Production Build
```bash
pnpm build
pnpm start
```

### Docker Deployment
```bash
docker build -t litechat-backend-node .
docker run -p 3000:3000 litechat-backend-node
```

---

## Environment Variables

- `PORT`: Server port (default: 3000)
- `NODE_ENV`: Environment mode (`development` or `production`)

---

## Troubleshooting

### FastAPI Bridge Not Ready
- Ensure Python 3.8+ is installed
- Check that `requirements.txt` dependencies are installed
- Review logs for Python errors

### Session Timeout
- Sessions expire after 10 minutes of inactivity
- Call `/api/send-code` again to start a new session

### Invalid Session String
- Session strings can become invalid if revoked on Telegram
- Authenticate again to get a new session string

---

## Support

For issues or questions, please refer to the project repository or contact the development team.
