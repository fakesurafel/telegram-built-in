# LiteChat Backend - Render Deployment Guide

This guide walks you through deploying the LiteChat backend service to Render.com.

## Prerequisites

- GitHub account with the `telegram-built-in` repository
- Render.com account (free tier available)
- Telegram API credentials (from [my.telegram.org](https://my.telegram.org))

## Step-by-Step Deployment

### Step 1: Prepare Your GitHub Repository

Ensure your repository has been updated with the combined code:

```bash
# From your local machine
git add .
git commit -m "Add backend service and deployment configuration"
git push origin main
```

The repository should contain:
- ✅ `backend-server/` directory with Node.js and Python code
- ✅ `Dockerfile` for containerization
- ✅ `requirements.txt` for Python dependencies
- ✅ `render.yaml` for Render configuration
- ✅ Android app code in `app/` directory

### Step 2: Create a Render Account

1. Go to [render.com](https://render.com)
2. Click "Sign up"
3. Choose "Sign up with GitHub"
4. Authorize Render to access your GitHub account

### Step 3: Create a New Web Service

1. In Render dashboard, click "New +" button
2. Select "Web Service"
3. Select your `telegram-built-in` repository
4. Click "Connect"

### Step 4: Configure the Web Service

Fill in the following settings:

| Setting | Value |
|---------|-------|
| **Name** | `litechat-backend` |
| **Environment** | `Docker` |
| **Region** | Choose closest to you (e.g., `us-east-1`) |
| **Branch** | `main` |
| **Dockerfile Path** | `./Dockerfile` |
| **Build Command** | (leave empty) |
| **Start Command** | (leave empty) |

### Step 5: Set Environment Variables

In the "Environment" section, add:

```
PORT=3000
NODE_ENV=production
```

### Step 6: Choose a Plan

- **Free Tier**: Good for testing and development
  - Auto-spins down after 15 minutes of inactivity
  - Limited resources
  - Perfect for getting started

- **Starter Plan**: Recommended for production
  - Always-on service
  - Better performance
  - $7/month

For this guide, we'll use the **Free Tier**.

### Step 7: Deploy

1. Click "Create Web Service"
2. Render will start building your Docker image
3. Wait for the build to complete (usually 3-5 minutes)
4. Once deployed, you'll see a URL like: `https://litechat-backend.onrender.com`

### Step 8: Verify Deployment

Test your backend is running:

```bash
# Check health endpoint
curl https://litechat-backend.onrender.com/health

# Expected response:
# {"status":"healthy","fastApiReady":true,"timestamp":"2026-07-22T..."}
```

## Configuring the Android App

Once your backend is deployed on Render:

1. **Install the LiteChat app** on your Android device
2. **Open the app** and go to the **"Nodes"** tab
3. **Update the Base URL** to your Render URL:
   ```
   https://litechat-backend.onrender.com
   ```
4. **Enter your Telegram API credentials**:
   - API ID (from my.telegram.org)
   - API Hash (from my.telegram.org)
5. **Start authenticating** with your Telegram account

## Important Notes

### Cold Starts

On the free tier, your service will spin down after 15 minutes of inactivity. The first request after inactivity will take 30-60 seconds (cold start).

To minimize cold starts:
- Upgrade to a paid plan
- Use a monitoring service to keep the service warm

### Persistent Sessions

The backend uses in-memory session storage. This means:
- Sessions are lost when the service restarts
- Users need to re-authenticate after a restart
- For production, consider adding database-backed sessions

### Logs

To view deployment logs:

1. Go to your Render dashboard
2. Click on `litechat-backend` service
3. Click "Logs" tab
4. View real-time logs

## Troubleshooting

### Build Fails

**Error**: `ModuleNotFoundError: No module named 'telethon'`

**Solution**: Ensure `requirements.txt` is in the repository root and contains:
```
telethon==1.34.0
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
```

### Service Won't Start

**Error**: `Port 3000 already in use`

**Solution**: The Dockerfile should expose port 3000. Check that `Dockerfile` has:
```dockerfile
EXPOSE 3000
```

### Health Check Fails

**Error**: `Health check failed`

**Solution**: 
1. Check logs in Render dashboard
2. Verify the `/health` endpoint is responding
3. Ensure FastAPI bridge started successfully

### App Can't Connect

**Error**: `Connection refused` or `Network error`

**Solution**:
1. Verify the Render URL is correct
2. Check that the service is running (green status in Render)
3. Wait for cold start to complete (first request takes 30-60s)
4. Check firewall/network settings on your phone

## Upgrading to Paid Plan

For production use, upgrade to a paid plan:

1. Go to your service in Render dashboard
2. Click "Settings"
3. Under "Plan", select "Starter" or higher
4. Follow the upgrade process

Benefits of paid plans:
- ✅ Always-on service (no cold starts)
- ✅ Better performance
- ✅ Priority support
- ✅ More resources

## Monitoring and Maintenance

### Monitor Service Health

1. Go to Render dashboard
2. Check service status (should be green)
3. Monitor CPU and memory usage
4. Review logs for errors

### Update the Backend

To deploy updates:

1. Make changes to your code
2. Commit and push to GitHub:
   ```bash
   git add .
   git commit -m "Update backend"
   git push origin main
   ```
3. Render automatically redeploys (if auto-deploy is enabled)
4. New version is live in 3-5 minutes

### Rollback

If something breaks:

1. Go to Render dashboard
2. Click on your service
3. Go to "Deploys" tab
4. Click "Redeploy" on a previous successful deployment

## Cost Estimation

| Plan | Monthly Cost | Best For |
|------|-------------|----------|
| Free | $0 | Testing, development |
| Starter | $7 | Small production use |
| Standard | $25 | Medium production use |
| Pro | $115+ | Large production use |

Free tier is perfect for getting started. Upgrade when you need always-on service.

## Next Steps

1. ✅ Deploy backend to Render
2. ✅ Configure Android app with Render URL
3. ✅ Test authentication flow
4. ✅ Monitor logs and performance
5. ✅ Upgrade to paid plan when ready for production

## Support

- **Render Docs**: https://render.com/docs
- **Backend API Docs**: See `API_DOCUMENTATION.md`
- **Android App Guide**: See `README.md`

---

**Your LiteChat backend is now deployed and ready to use!** 🚀
