import React, {Component} from 'react';
import {
    RpcRequest,
    AdminSetGameStateRequest,
    GameState,
    protosLoaded,
    AdminShutdownDeviceRequest
} from './common-js/ProtoLoader';
import {socket as AdminSocket} from './AdminInterface';

export default class ReadyClients extends Component {
    state = {
        tablets: [],
        displayList: false,
    };

    componentDidMount() {
        AdminSocket.registerEventHandler('adminDevicesChangedEvent', this._onEvent.bind(this));
    }

    _onEvent(event) {
        this.setState({
            tablets: event.devices,
        });
    }

    shutdown(deviceId) {
        const message = RpcRequest.create();
        message.adminShutdownDeviceRequest = AdminShutdownDeviceRequest.create();
        message.adminShutdownDeviceRequest.deviceId = deviceId;
        AdminSocket.sendMessage(message);
    }

    async startQuiz() {
        await protosLoaded;
        const message = RpcRequest.create();
        message.adminSetGameStateRequest = AdminSetGameStateRequest.create();
        message.adminSetGameStateRequest.newState = GameState.State.READY;
        AdminSocket.sendMessage(message);
    }

<<<<<<< HEAD
    renderTablet(tablet) {
        return (
            <div>
                <b>Device: </b> {tablet.deviceId}; <b>Team: </b> {tablet.team || 'NONE'};
                <b>Ready: </b> {!!tablet.ready ? tablet.ready.toString() : 'no'}
                <button onClick={() => this.shutdown(tablet.deviceId)}
                        disabled={!this.props.dangerZone}
                >Close App
                </button>
            </div>
        );
    }

=======
>>>>>>> 96412f380e49bc09ba40b96954f792216a1bb2e4
    render() {
        const readyClients = this.state.tablets.reduce((prev, curr) => prev + (curr.ready ? 1 : 0), 0);
        const totalClients = this.state.tablets.length;

        return (
            <div>
                {readyClients} ready
                out of {totalClients}
                <meter max={totalClients} value={readyClients}/>
                <br />
                <button
                    onClick={() => this.setState({displayList: !this.state.displayList})}
                >
                    {this.state.displayList ? 'Hide List' : 'Display List'}
                </button>
                <button
                    disabled={!(this.props.dangerZone || (readyClients === totalClients && this.props.state === GameState.State.NOTREADY))}
                    onClick={this.startQuiz}
                >
                    Start Quiz
                </button>
                <br />
                {this.state.displayList && (
                    <ul className="devices">
                        {this.state.tablets.map(tablet => (
                            <li key={tablet.deviceId} className={tablet.ready && 'ready'}>
<<<<<<< HEAD
                                {tablet.disconnected ? <s>{this.renderTablet(tablet)}</s> : this.renderTablet(tablet)}
=======
                                <b>Device: </b> {tablet.deviceId}; <b>Team: </b> {tablet.team || 'NONE'};
                                <b>Ready: </b> {!!tablet.ready ? tablet.ready.toString() : 'no'}
                                <button onClick={() => this.shutdown(tablet.deviceId)}
                                        disabled={!this.props.dangerZone}
                                >Close App
                                </button>
>>>>>>> 96412f380e49bc09ba40b96954f792216a1bb2e4
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        )
    }
}