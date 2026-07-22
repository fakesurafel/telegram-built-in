#!/usr/bin/env python3
"""
FastAPI Telethon Bridge
Handles Telegram OTP authentication and chat fetching via Telethon
"""

import asyncio
import logging
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn
from telethon import TelegramClient
from telethon.sessions import StringSession

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Telegram Bridge", version="1.0.0")

# Data models
class SendCodeRequest(BaseModel):
    api_id: str
    api_hash: str
    phone: str

class VerifyCodeRequest(BaseModel):
    phone: str
    code: str
    password: Optional[str] = None
    api_id: str
    api_hash: str

class GetChatsRequest(BaseModel):
    session_string: str
    api_id: str
    api_hash: str

class ChatDto(BaseModel):
    id: int
    name: str
    username: str
    phone: str
    unread_count: int
    last_message: str

class ChatsResponse(BaseModel):
    chats: List[ChatDto]

# In-memory store for pending clients
pending_clients: Dict[str, Any] = {}

@app.get("/")
async def root():
    """Status endpoint"""
    return {
        "status": "ok",
        "service": "Telegram FastAPI Bridge",
        "version": "1.0.0"
    }

@app.post("/api/send-code")
async def send_code(request: SendCodeRequest):
    """
    Initiate Telegram OTP request
    """
    try:
        api_id = int(request.api_id)
        api_hash = request.api_hash
        phone = request.phone

        logger.info(f"[send-code] Initiating OTP for {phone}")

        # Create Telethon client
        client = TelegramClient(StringSession(), api_id, api_hash)
        await client.connect()

        # Send code request
        sent_code = await client.send_code_request(phone)
        
        # Store pending client
        pending_clients[phone] = {
            "client": client,
            "phone_code_hash": sent_code.phone_code_hash,
            "api_id": api_id,
            "api_hash": api_hash
        }

        logger.info(f"[send-code] OTP sent to {phone}")

        return {
            "status": "code_sent",
            "phone": phone,
            "phone_code_hash": sent_code.phone_code_hash
        }
    except Exception as e:
        logger.error(f"[send-code] Error: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/verify-code")
async def verify_code(request: VerifyCodeRequest):
    """
    Verify OTP code and return session string
    """
    try:
        phone = request.phone
        code = request.code
        password = request.password

        logger.info(f"[verify-code] Verifying code for {phone}")

        if phone not in pending_clients:
            raise HTTPException(
                status_code=400,
                detail="No pending code request for this phone number"
            )

        data = pending_clients[phone]
        client = data["client"]

        if not client.is_connected():
            await client.connect()

        try:
            # Try to sign in with code
            await client.sign_in(
                phone,
                code,
                phone_code_hash=data["phone_code_hash"]
            )
        except Exception as e:
            error_str = str(e).lower()
            # Check if 2FA password is required and no password was provided
            if ("password" in error_str or "2fa" in error_str) and not password:
                logger.info(f"[verify-code] 2FA password required for {phone}")
                return {"status": "requires_password"}
            
            # If password was provided, try to sign in with it
            if password:
                try:
                    await client.sign_in(password=password)
                except Exception as pwd_error:
                    logger.error(f"[verify-code] Password verification failed: {str(pwd_error)}")
                    raise HTTPException(status_code=400, detail=f"Invalid password: {str(pwd_error)}")
            else:
                raise HTTPException(status_code=400, detail=str(e))

        # Get session string
        session_string = client.session.save()

        # Clean up
        await client.disconnect()
        del pending_clients[phone]

        logger.info(f"[verify-code] Successfully verified {phone}")

        return {
            "status": "success",
            "session_string": session_string,
            "api_id": data["api_id"],
            "api_hash": data["api_hash"]
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[verify-code] Error: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/chats", response_model=ChatsResponse)
async def get_chats(request: GetChatsRequest):
    """
    Fetch recent chats/dialogs (up to 20)
    """
    try:
        session_string = request.session_string
        api_id = int(request.api_id)
        api_hash = request.api_hash

        logger.info("[get-chats] Fetching chats")

        # Create client from session
        client = TelegramClient(StringSession(session_string), api_id, api_hash)
        await client.connect()

        # Get dialogs (limit to 20)
        dialogs = await client.get_dialogs(limit=20)

        chats = []
        for dialog in dialogs:
            try:
                # Extract chat information
                chat_id = dialog.id
                chat_name = dialog.name or "Unknown"
                
                # Get username if available
                username = ""
                if hasattr(dialog.entity, 'username') and dialog.entity.username:
                    username = dialog.entity.username
                else:
                    username = "No username"

                # Get phone if available
                phone = ""
                if hasattr(dialog.entity, 'phone') and dialog.entity.phone:
                    phone = dialog.entity.phone
                else:
                    phone = "Private"

                # Get last message
                last_message = ""
                if dialog.message:
                    if dialog.message.message:
                        last_message = dialog.message.message[:100]  # Truncate to 100 chars
                    else:
                        last_message = "[Media/No text]"
                else:
                    last_message = "[No messages]"

                unread_count = dialog.unread_count or 0

                chats.append(ChatDto(
                    id=chat_id,
                    name=chat_name,
                    username=username,
                    phone=phone,
                    unread_count=unread_count,
                    last_message=last_message
                ))
            except Exception as e:
                logger.warning(f"[get-chats] Error processing dialog: {str(e)}")
                continue

        await client.disconnect()

        logger.info(f"[get-chats] Fetched {len(chats)} chats")

        return ChatsResponse(chats=chats)
    except Exception as e:
        logger.error(f"[get-chats] Error: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))

@app.on_event("startup")
async def startup_event():
    logger.info("[FastAPI] Application starting up")

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("[FastAPI] Application shutting down")
    # Clean up any pending clients
    for phone, data in pending_clients.items():
        try:
            client = data["client"]
            if client.is_connected():
                asyncio.create_task(client.disconnect())
        except Exception as e:
            logger.error(f"[shutdown] Error disconnecting {phone}: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="127.0.0.1",
        port=8000,
        log_level="info"
    )
