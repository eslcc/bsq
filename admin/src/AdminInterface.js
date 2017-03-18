import React from 'react';
import AdminSocket from './lib/AdminSocket';
import { protosLoaded, RpcRequest, GetGameStateRequest } from './lib/ProtoLoader';

import GameState from './GameState';
import Questions from './Questions';
import ReadyClients from './ReadyClients';
import Errors from './Errors';

export default class AdminInterface extends React.Component {
    state = {
        rootsLoaded: false,
        dangerZone: false,
        gameState: {},
    };

    dangerZoneCheckbox = null;

    componentDidMount() {
        protosLoaded.then(() => {
            this.setState({rootsLoaded: true});
            AdminSocket.open();
            window.as = AdminSocket;

            AdminSocket.registerResponseHandler('getGameStateResponse', this._onGetGameState.bind(this));
            AdminSocket.registerEventHandler('gameStateChangeEvent', this._onStateChange.bind(this));
            const message = RpcRequest.create();
            message.getGameStateRequest = GetGameStateRequest.create();
            AdminSocket.sendMessage(message);
        });
    }

    checkDangerZone() {
        this.setState({dangerZone: this.dangerZoneCheckbox.checked});
    }

    _onStateChange(message) {
        this.setState({
            state: message.newState,
        });
    }

    _onGetGameState(message) {
        this.setState({
            state: message.state,
        });
    }

    render() {
        if (this.state.rootsLoaded) {
            return (
                <div style={ {backgroundColor: this.state.dangerZone ? 'red' : 'white'} }>
                    <input type="checkbox"
                           onClick={this.checkDangerZone.bind(this)}
                           ref={c => {
                               this.dangerZoneCheckbox = c
                           }}
                           style={ {width: 32, height: 32}}
                    />
                    <span style={ {color: 'red'} }>DANGER ZONE</span>
                    <br />
                    <h2>Ready clients: </h2>
                    <ReadyClients dangerZone={this.state.dangerZone} state={this.state.state} />
                    <br />
                    <h2>State: </h2>
                    <GameState dangerZone={this.state.dangerZone} state={this.state.state} />
                    <br />
                    <h2>Questions: </h2>
                    <Questions state={this.state.state} />
                    <h2>Errors: </h2>
                    <Errors state={this.state.state} />
                </div>
            );
        } else {
            return <div>Loading protos...</div>;
        }
    }
}