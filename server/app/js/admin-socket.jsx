class AdminSocket {
    static socket = null;
    static _responseCallbacks = {};
    static _eventCallbacks = [];
    static _opened = false;
    static _sendQueue = [];

    static open(forceReopen = false) {
        if (!AdminSocket._opened || forceReopen) {
            AdminSocket.socket = new WebSocket(`ws://${window.location.host}/socket`, 'admin');
            AdminSocket.socket.binaryType = 'arraybuffer';
            AdminSocket.socket.onmessage = AdminSocket._onMessage;
            AdminSocket.socket.onerror = (error) => console.error(error);
            AdminSocket.socket.onclose = () => console.error('Socket closed for some reason!');
            AdminSocket.socket.onopen = AdminSocket._drainQueue;
            AdminSocket._opened = true;
        }
    }

    static _drainQueue() {
        while (AdminSocket._sendQueue.length > 0) {
            const item = AdminSocket._sendQueue.shift();
            AdminSocket.socket.send(item);
        }
    }

    static _onMessage(message) {
        const data = new Uint8Array(message.data);
        try {
            const event = GameEvent.decode(data);
            GameEvent.verify(event);
            AdminSocket._eventCallbacks.forEach(handler => handler(event));
        } catch (e) {
            const response = RpcResponse.decode(data);
            (AdminSocket._responseCallbacks[response.response] || []).forEach(
                handler => handler(response[response.response])
            );
        }
    }

    static registerResponseHandler(type, handler) {
        AdminSocket._responseCallbacks[type] = (AdminSocket._responseCallbacks[type] || []).concat(handler);
    }

    static registerEventHandler(handler) {
        AdminSocket._eventCallbacks.push(handler);
    }


    static sendMessage(message) {
        const data = RpcRequest.encode(message).finish();
        if (AdminSocket.socket !== null && AdminSocket.socket.readyState === AdminSocket.socket.OPEN) {
            AdminSocket.socket.send(data);
            console.debug(`Sending ${data}`);
        } else {
            AdminSocket.open(false);
            AdminSocket._sendQueue.push(data);
            console.debug(`Queueing ${data}`);
        }
    }
}