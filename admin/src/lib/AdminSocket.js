import { GameEvent, RpcRequest, RpcResponse } from './ProtoLoader';

window.dnr = () => {
    window.DO_NOT_REOPEN = true;
};

let reopens = 0;

export default class AdminSocket {
    static socket = null;
    static _responseCallbacks = {};
    static _eventCallbacks = {};
    static _opened = false;
    static _sendQueue = [];

    static open(forceReopen = false) {
        if (!AdminSocket._opened || forceReopen) {
            let url;
            if (process.env.NODE_ENV === 'development') {
                url = "ws://localhost:8080/socket?admin";
            } else {
                url = `ws://${window.location.host}/socket?admin`;
            }
            AdminSocket.socket = new WebSocket(url);
            AdminSocket.socket.binaryType = 'arraybuffer';
            AdminSocket.socket.onmessage = AdminSocket._onMessage;
            AdminSocket.socket.onerror = (error) => console.error(error);
            AdminSocket.socket.onclose = () => {
                console.error('Socket closed for some reason!');
                if (reopens > 3 || window.DO_NOT_REOPEN) {
                    console.log('Skipping socket reopen');
                    return;
                }
                AdminSocket.open(true);
                reopens++;
                console.log('Socket reopened');
            };
            AdminSocket.socket.onopen = AdminSocket._drainQueue;
            AdminSocket._opened = true;
        }
    }

    static _drainQueue() {
        console.debug(`Draining ${AdminSocket._sendQueue.length} items`);
        while (AdminSocket._sendQueue.length > 0) {
            const item = AdminSocket._sendQueue.shift();
            console.debug(`Sending ${item} from drain`);
            AdminSocket.socket.send(item);
        }
        console.debug('Swamp drained');
    }

    static _onMessage(message) {
        const data = new Uint8Array(message.data);
        console.log(`received ${data}`);
        if (
            data[0] === 0xff &&
            data[1] === 0xff &&
            data[2] === 0xff &&
            data[3] === 0xff
        ) {
            const eventBuffer = data.slice(4);
            const event = GameEvent.decode(eventBuffer);
            console.log(`decoded GameEvent ${event.event}`);
            (AdminSocket._eventCallbacks[event.event] || []).forEach(
                handler => handler(event[event.event])
            );
        } else {
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
        console.log(`Socket ready state ${AdminSocket.socket && AdminSocket.socket.readyState}`);
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