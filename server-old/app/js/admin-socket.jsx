class AdminSocket {
    static socket = null;
    static _responseCallbacks = {};
    static _eventCallbacks = {};
    static _opened = false;
    static _sendQueue = [];

    static open(forceReopen = false) {
        if (!AdminSocket._opened || forceReopen) {
            AdminSocket.socket = new WebSocket(`ws://${window.location.host}/socket`, 'admin');
            AdminSocket.socket.binaryType = 'arraybuffer';
            AdminSocket.socket.onmessage = AdminSocket._onMessage;
            AdminSocket.socket.onerror = (error) => console.error(error);
            AdminSocket.socket.onclose = () => {
                console.error('Socket closed for some reason!');
                if (window.DO_NOT_REOPEN) {
                    console.log('Skipping socket reopen');
                    return;
                }
                AdminSocket.open(true);
                console.log('Socket reopened');
            };
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
        console.log(`received ${data}`);
        try {
            const event = GameEvent.decode(data);
            GameEvent.verify(event);
            console.log(`decoded GameEvent ${event.event}`);
            (AdminSocket._eventCallbacks[event.event] || []).forEach(
                handler => handler(response[event.event])
            );
        } catch (e) {
            const response = RpcResponse.decode(data);
            console.log(`decoded RpcResponse ${response.response}`);
            (AdminSocket._responseCallbacks[response.response] || []).forEach(
                handler => handler(response[response.response])
            );
        }
    }

    static registerResponseHandler(type, handler) {
        AdminSocket._responseCallbacks[type] = (AdminSocket._responseCallbacks[type] || []).concat(handler);
    }

    static registerEventHandler(type, handler) {
        AdminSocket._eventCallbacks[type] = (AdminSocket._responseCallbacks[type] || []).concat(handler);
    }


    static sendMessage(message) {
        const data = RpcRequest.encode(message).finish();
        if (AdminSocket.socket !== null && AdminSocket.socket.readyState === AdminSocket.socket.OPEN) {
            AdminSocket.socket.send(data);
            console.debug(`Sending ${message.request}`);
        } else {
            AdminSocket.open(false);
            AdminSocket._sendQueue.push(data);
            console.debug(`Queueing ${message.request}`);
        }
    }
}