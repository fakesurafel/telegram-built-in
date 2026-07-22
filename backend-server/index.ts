/**
 * Main entry point for the LiteChat Backend Node
 * Starts the Telegram bridge service
 */

import { startTelegramBridge } from './telegram-bridge';

const PORT = process.env.PORT ? parseInt(process.env.PORT) : 3000;

startTelegramBridge(PORT).catch((error) => {
  console.error('[Main] Fatal error:', error);
  process.exit(1);
});
