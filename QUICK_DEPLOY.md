# 🚀 Quick Deploy to Render - 5 Minutes

Fast-track guide to get your LiteChat backend running on Render.

## Prerequisites

- ✅ GitHub account
- ✅ Render.com account (free)
- ✅ This repository cloned and pushed to GitHub

## Deploy in 5 Steps

### 1️⃣ Go to Render Dashboard
Visit [render.com/dashboard](https://render.com/dashboard)

### 2️⃣ Create New Web Service
- Click "New +" → "Web Service"
- Select `telegram-built-in` repository
- Click "Connect"

### 3️⃣ Configure Service
```
Name:              litechat-backend
Environment:       Docker
Region:            us-east-1 (or closest to you)
Branch:            main
Dockerfile Path:   ./Dockerfile
```

### 4️⃣ Add Environment Variables
```
PORT=3000
NODE_ENV=production
```

### 5️⃣ Deploy
- Select "Free" plan
- Click "Create Web Service"
- Wait 3-5 minutes for build

## ✅ Verify Deployment

Once deployed, you'll get a URL like:
```
https://litechat-backend.onrender.com
```

Test it:
```bash
curl https://litechat-backend.onrender.com/health
```

Expected response:
```json
{
  "status": "healthy",
  "fastApiReady": true,
  "timestamp": "2026-07-22T..."
}
```

## 📱 Update Android App

In LiteChat app:
1. Go to "Nodes" tab
2. Change Base URL to: `https://litechat-backend.onrender.com`
3. Enter Telegram API credentials
4. Start using!

## ⚠️ Important Notes

- **Free Tier**: Service spins down after 15 min inactivity (first request takes 30-60s)
- **Upgrade Later**: Switch to paid plan for always-on service ($7/month)
- **Cold Starts**: Normal on free tier, not a problem for testing

## 🔗 Full Documentation

- [Detailed Render Guide](./RENDER_DEPLOYMENT.md)
- [Backend API Docs](./API_DOCUMENTATION.md)
- [Android App Guide](./README.md)

---

**That's it! Your backend is now live.** 🎉
