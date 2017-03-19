import { GameEvent, RpcRequest, RpcResponse } from './ProtoLoader';

window.dnr = () => {
    window.DO_NOT_REOPEN = true;
};

let reopens = 0;

export default class AdminSocket {
    constructor(type = 'admin') {
        this.type = type;
    }

    type = '';
    socket = null;
    _responseCallbacks = {};
    _eventCallbacks = {};
    _opened = false;
    _sendQueue = [];

    open(forceReopen = false) {
        if (!this._opened || forceReopen) {
            let url;
            if (process.env.NODE_ENV === 'development') {
                url = `ws://localhost:8080/socket?${this.type}`;
            } else {
                url = `ws://${window.location.host}/socket?${this.type}`;
            }
            this.socket = new WebSocket(url);
            this.socket.binaryType = 'arraybuffer';
            this.socket.onmessage = this._onMessage.bind(this);
            this.socket.onerror = (error) => console.error(error);
            this.socket.onclose = () => {
                console.error('Socket closed for some reason!');
                if (reopens > 3 || window.DO_NOT_REOPEN) {
                    console.log('Skipping socket reopen');
                    return;
                }
                this.open(true);
                reopens++;
                console.log('Socket reopened');
            };
            this.socket.onopen = this._drainQueue.bind(this);
            this._opened = true;
        }
    }

    _drainQueue() {
        console.debug(`Draining ${this._sendQueue.length} items`);
        while (this._sendQueue.length > 0) {
            const item = this._sendQueue.shift();
            console.debug(`Sending ${item} from drain`);
            this.socket.send(item);
        }
        console.debug('Swamp drained');
    }

    _onMessage(message) {
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
            (this._eventCallbacks[event.event] || []).forEach(
                handler => handler(event[event.event])
            );
        } else {
            const response = RpcResponse.decode(data);
            console.log(`decoded RpcResponse ${response.response}`);
            (this._responseCallbacks[response.response] || []).forEach(
                handler => handler(response[response.response])
            );
        }
    }

    registerResponseHandler(type, handler) {
        this._responseCallbacks[type] = (this._responseCallbacks[type] || []).concat(handler);
    }

    registerEventHandler(type, handler) {
        this._eventCallbacks[type] = (this._responseCallbacks[type] || []).concat(handler);
    }


    sendMessage(message) {
        const data = RpcRequest.encode(message).finish();
        console.log(`Socket ready state ${this.socket && this.socket.readyState}`);
        if (this.socket !== null && this.socket.readyState === this.socket.OPEN) {
            this.socket.send(data);
            console.debug(`Sending ${message.request}`);
        } else {
            this.open(false);
            this._sendQueue.push(data);
            console.debug(`Queueing ${message.request}`);
        }
    }
}