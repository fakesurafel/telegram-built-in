# LiteChat - Complete Telegram Client Solution

A complete, production-ready Telegram client solution combining an Android app with a cloud-hosted backend service.

## 📦 Project Structure

```
telegram-built-in/
├── app/                          # Android Kotlin/Compose app
│   ├── src/main/java/com/example/
│   │   ├── data/                # Data layer (API, DB, Repository)
│   │   ├── ui/                  # UI layer (Compose screens)
│   │   └── MainActivity.kt       # Entry point
│   └── build.gradle.kts         # Android build config
│
├── backend-server/              # Node.js + Python backend
│   ├── index.ts                 # Express server entry point
│   ├── telegram-bridge.ts       # API routes & session management
│   └── telegram_bridge.py       # FastAPI Telethon bridge
│
├── Dockerfile                   # Multi-stage Docker build
├── requirements.txt             # Python dependencies
├── API_DOCUMENTATION.md         # Backend API reference
├── BACKEND_README.md            # Backend setup guide
└── README.md                    # Android app guide
```

## 🚀 Quick Start

### Option 1: Android App Only (Local Development)

```bash
# Prerequisites: Android Studio

1. Open Android Studio
2. Select "Open" and choose this directory
3. Create `.env` file with `GEMINI_API_KEY`
4. Configure backend URL in app's "Nodes" tab
5. Run on emulator or device
```

### Option 2: Backend Service (Local Development)

```bash
# Prerequisites: Node.js 18+, Python 3.8+

# Install dependencies
pip3 install -r requirements.txt

# Start the backend
cd backend-server
node index.ts

# Backend runs on http://localhost:3000
# FastAPI bridge on http://localhost:8000
```

### Option 3: Full Stack (Docker)

```bash
# Build the Docker image
docker build -t litechat .

# Run the container
docker run -p 3000:3000 litechat

# Backend accessible at http://localhost:3000
```

## 🌐 Deployment on Render

### Step 1: Push to GitHub

```bash
git add .
git commit -m "Combined Android app and backend service"
git push origin main
```

### Step 2: Create Render Service

1. Go to [render.com](https://render.com)
2. Sign in with GitHub
3. Click "New +" → "Web Service"
4. Select your `telegram-built-in` repository
5. Configure:
   - **Name**: `litechat-backend`
   - **Environment**: `Docker`
   - **Branch**: `main`
   - **Build Command**: (leave empty - uses Dockerfile)
   - **Start Command**: (leave empty - uses Dockerfile)

### Step 3: Set Environment Variables

In Render dashboard, add:
- `PORT=3000` (optional, default is 3000)
- `NODE_ENV=production`

### Step 4: Deploy

Click "Create Web Service" and Render will automatically:
1. Build the Docker image
2. Deploy the backend service
3. Provide a public URL (e.g., `https://litechat-backend.onrender.com`)

### Step 5: Update Android App

In the LiteChat app:
1. Go to "Nodes" tab
2. Change "Base URL" to your Render URL: `https://litechat-backend.onrender.com`
3. Enter your Telegram API credentials
4. Start using the app!

## 📱 Android App Features

- **OTP Authentication**: Log in with Telegram OTP codes
- **Session Key Import**: Import existing Telethon session strings
- **Two-Factor Authentication**: Handle 2FA passwords
- **Chat Management**: View up to 20 recent chats with unread counts
- **Session Management**: Save and switch between multiple sessions
- **Live Mode**: Connect to any backend server via custom URL
- **Dark Theme**: Beautiful dark UI with Indigo accent colors

## 🔌 Backend API Endpoints

### Authentication
- `POST /api/send-code` - Initiate Telegram OTP request
- `POST /api/verify-code` - Verify OTP and get session string (supports 2FA)

### Chat Operations
- `POST /api/chats` - Fetch recent chats/dialogs (up to 20)

### Status
- `GET /` - Service status page (HTML)
- `GET /health` - Health check endpoint

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for detailed endpoint documentation.

## 🔐 Getting Telegram API Credentials

1. Go to [my.telegram.org](https://my.telegram.org)
2. Log in with your Telegram account
3. Navigate to "API development tools"
4. Create a new application
5. Copy your `api_id` and `api_hash`

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│         Android App (Kotlin/Compose)        │
│  • OTP Authentication UI                    │
│  • Chat List Display                        │
│  • Session Management                       │
└────────────────┬────────────────────────────┘
                 │ HTTP/HTTPS
                 ▼
┌─────────────────────────────────────────────┐
│    Express Server (Node.js) - Port 3000     │
│  • API Routes                               │
│  • Session Store                            │
│  • Request Validation                       │
└────────────────┬────────────────────────────┘
                 │ HTTP (localhost:8000)
                 ▼
┌─────────────────────────────────────────────┐
│  FastAPI Bridge (Python) - Port 8000        │
│  • Telethon Client Management               │
│  • Telegram OTP Handling                    │
│  • Chat/Dialog Retrieval                    │
└────────────────┬────────────────────────────┘
                 │ Telethon Protocol
                 ▼
            Telegram Servers
```

## 📊 Tech Stack

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **State Management**: ViewModel + StateFlow

### Backend Service
- **Node.js Server**: Express.js
- **Python Bridge**: FastAPI + Telethon
- **Deployment**: Docker + Render

## 🔄 Development Workflow

### Making Changes to Android App

1. Edit files in `app/src/main/java/`
2. Build and run in Android Studio
3. Test on emulator or device

### Making Changes to Backend

1. Edit files in `backend-server/`
2. Restart the backend service
3. Test endpoints with curl or Postman

### Deploying Changes

1. Commit and push to GitHub
2. Render automatically redeploys on push
3. New backend URL is available immediately

## 🚨 Troubleshooting

### Android App Can't Connect to Backend

- Verify backend URL in "Nodes" tab
- Check that backend is running (visit `/health` endpoint)
- Ensure firewall allows outbound connections

### Backend Not Starting

- Verify Python 3.8+ is installed: `python3 --version`
- Install dependencies: `pip3 install -r requirements.txt`
- Check for port conflicts on 3000 and 8000

### Render Deployment Fails

- Check build logs in Render dashboard
- Verify Dockerfile is in repository root
- Ensure GitHub repository is public or connected

## 📚 Additional Resources

- [Android App Guide](./README.md)
- [Backend API Documentation](./API_DOCUMENTATION.md)
- [Backend Setup Guide](./BACKEND_README.md)
- [Telegram Bot API Docs](https://core.telegram.org/bots/api)
- [Telethon Documentation](https://docs.telethon.dev/)

## 🤝 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the documentation files
3. Check GitHub issues in the repository

## 📄 License

MIT

---

**Status**: Production Ready ✅

The complete LiteChat solution is ready for deployment and use. Start with the Quick Start section above!
