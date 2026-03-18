const DEFAULT_RECONNECT = {
  enabled: true,
  initialDelayMs: 500,
  maxDelayMs: 8000,
  factor: 1.6,
  jitterMs: 200,
};

function computeDelay(attempt, cfg) {
  const base = Math.min(cfg.maxDelayMs, Math.round(cfg.initialDelayMs * Math.pow(cfg.factor, attempt)));
  const jitter = cfg.jitterMs ? Math.round((Math.random() * 2 - 1) * cfg.jitterMs) : 0;
  return Math.max(0, base + jitter);
}

export class WebsocketService {
  constructor(url, { reconnect = DEFAULT_RECONNECT } = {}) {
    this.url = url;
    this.reconnectCfg = { ...DEFAULT_RECONNECT, ...(reconnect || {}) };
    this.ws = null;
    this.attempt = 0;
    this.manualClose = false;
    this.reconnectTimer = null;

    this.onMessage = null;
    this.onStatus = null;
    this.onError = null;
  }

  setHandlers({ onMessage, onStatus, onError }) {
    this.onMessage = onMessage;
    this.onStatus = onStatus;
    this.onError = onError;
  }

  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN;
  }

  connect() {
    if (this.isConnected() || (this.ws && this.ws.readyState === WebSocket.CONNECTING)) return;

    this.manualClose = false;
    this._clearReconnect();

    try {
      this.ws = new WebSocket(this.url);
    } catch (e) {
      this._emitError(e);
      this._scheduleReconnect();
      return;
    }

    this._emitStatus(false);

    this.ws.onopen = () => {
      this.attempt = 0;
      this._emitStatus(true);
    };

    this.ws.onmessage = (event) => {
      let parsed;
      try {
        parsed = JSON.parse(event.data);
      } catch {
        parsed = event.data;
      }
      if (this.onMessage) this.onMessage(parsed);
    };

    this.ws.onerror = (err) => {
      this._emitError(err);
    };

    this.ws.onclose = () => {
      this._emitStatus(false);
      this.ws = null;
      if (!this.manualClose) this._scheduleReconnect();
    };
  }

  disconnect() {
    this.manualClose = true;
    this._clearReconnect();
    if (this.ws) {
      try {
        this.ws.close();
      } catch {
        // ignore
      }
    }
    this.ws = null;
    this._emitStatus(false);
  }

  sendJson(payload) {
    if (!this.isConnected()) return false;
    try {
      this.ws.send(JSON.stringify(payload));
      return true;
    } catch (e) {
      this._emitError(e);
      return false;
    }
  }

  _emitStatus(connected) {
    if (this.onStatus) this.onStatus(connected);
  }

  _emitError(err) {
    if (this.onError) this.onError(err);
  }

  _clearReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  _scheduleReconnect() {
    const cfg = this.reconnectCfg;
    if (!cfg.enabled) return;

    this._clearReconnect();
    const delay = computeDelay(this.attempt, cfg);
    this.attempt += 1;

    this.reconnectTimer = setTimeout(() => {
      this.connect();
    }, delay);
  }
}

export function buildWsUrl(pathname = "/ws/robot-control") {
  const explicitUrl = import.meta.env.VITE_WS_URL;
  if (explicitUrl) return String(explicitUrl);

  const base = import.meta.env.VITE_WS_BASE_URL;
  if (base) {
    const baseStr = String(base).replace(/\/$/, "");
    const pathStr = String(pathname).startsWith("/") ? String(pathname) : `/${pathname}`;
    return `${baseStr}${pathStr}`;
  }

  const proto = window.location.protocol === "https:" ? "wss" : "ws";
  return `${proto}://${window.location.host}${pathname}`;
}
