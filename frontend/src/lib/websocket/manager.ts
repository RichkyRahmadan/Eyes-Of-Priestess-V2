// src/lib/websocket/manager.ts
import { wsConnectionStatus, wsUnreadCounts } from '$lib/stores/websocket';
import { authStore } from '$lib/stores/auth';
import { addNotification } from '$lib/stores/notifications';

class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private heartbeatInterval: ReturnType<typeof setInterval> | null = null;
  private messageHandlers: Map<string, ((msg: unknown) => void)[]> = new Map();

  connect() {
    let token: string | undefined;
    authStore.subscribe((s) => {
      token = s.tokens?.accessToken;
    })();

    if (!token) return;

    wsConnectionStatus.set('connecting');
    this.ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${token}`);

    this.ws.onopen = () => {
      wsConnectionStatus.set('connected');
      this.reconnectAttempts = 0;
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data as string);
      this.handleMessage(data);
    };

    this.ws.onclose = () => {
      wsConnectionStatus.set('disconnected');
      this.stopHeartbeat();
      this.attemptReconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  private handleMessage(data: Record<string, unknown>) {
    switch (data.type) {
      case 'NEW_MESSAGE':
        this.emit('new_message', data);
        if (data.senderId !== this.getCurrentUserId()) {
          wsUnreadCounts.update((counts) => ({
            ...counts,
            [data.roomId as string]: (counts[data.roomId as string] || 0) + 1
          }));
        }
        break;
      case 'ROOM_STATUS_UPDATE':
        this.emit('room_status', data);
        break;
      case 'NOTIFICATION':
        addNotification({
          type: (data.notificationType as 'success' | 'error' | 'warning' | 'info') || 'info',
          title: data.title as string,
          message: data.message as string
        });
        break;
    }
  }

  send(message: object) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  joinRoom(roomId: string) {
    this.send({ type: 'JOIN_ROOM', roomId });
  }

  leaveRoom(roomId: string) {
    this.send({ type: 'LEAVE_ROOM', roomId });
  }

  sendMessage(roomId: string, content: string, messageType = 'TEXT') {
    this.send({ type: 'SEND_MESSAGE', roomId, content, messageType });
  }

  on(event: string, handler: (msg: unknown) => void) {
    if (!this.messageHandlers.has(event)) {
      this.messageHandlers.set(event, []);
    }
    this.messageHandlers.get(event)!.push(handler);
  }

  off(event: string, handler: (msg: unknown) => void) {
    const handlers = this.messageHandlers.get(event);
    if (handlers) {
      this.messageHandlers.set(
        event,
        handlers.filter((h) => h !== handler)
      );
    }
  }

  private emit(event: string, data: unknown) {
    this.messageHandlers.get(event)?.forEach((handler) => handler(data));
  }

  private startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      this.send({ type: 'PING' });
    }, 30000);
  }

  private stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return;
    setTimeout(() => {
      this.reconnectAttempts++;
      this.connect();
    }, this.reconnectDelay * Math.pow(2, this.reconnectAttempts));
  }

  private getCurrentUserId(): string | undefined {
    let userId: string | undefined;
    authStore.subscribe((s) => {
      userId = s.user?.id;
    })();
    return userId;
  }

  disconnect() {
    this.stopHeartbeat();
    this.ws?.close();
    this.ws = null;
  }
}

export const wsManager = new WebSocketManager();
