import React, {Component} from 'react';
import AdminSocket from './common-js/AdminSocket';
import {protosLoaded, RpcRequest, GetGameStateRequest, GameState, BigscreenGetTeamsRequest} from './common-js/ProtoLoader';
import './App.css';

import Teams from './screens/Teams';

export const socket = new AdminSocket('bigscreen');

class App extends Component {
    state = {
        state: {
            state: -1,
        },
        teams: [],
    };

    async componentDidMount() {
        await protosLoaded;
        socket.registerResponseHandler('getGameStateResponse', this._onGetGameState.bind(this));
        socket.registerEventHandler('gameStateChangeEvent', this._onStateChange.bind(this));
        const message = RpcRequest.create();
        message.getGameStateRequest = GetGameStateRequest.create();
        socket.sendMessage(message);
    }

    loadTeams() {
        socket.registerResponseHandler('bigscreenGetTeamsResponse', (message) => {
            this.setState({
                teams: message.teams
            });
        });
        const message = RpcRequest.create();
        message.bigscreenGetTeamsRequest = BigscreenGetTeamsRequest.create();
        socket.sendMessage(message);
    }

    onStateChange(state) {
        switch (state) {
            case GameState.State.NOTREADY:
                this.loadTeams();
                break;
        }
    }

    _onStateChange(message) {
        this.setState({
            state: message.newState
        });
        this.onStateChange(message.newState.state);
    }

    _onGetGameState(message) {
        this.setState({
            state: message.state
        });
        this.onStateChange(message.state.state);
    }

    render() {
        switch (this.state.state.state) {
            case -1:
                return <div>loading</div>;
            case GameState.State.NOTREADY:
                return <Teams teams={this.state.teams} />;
            case GameState.State.INTRO:
                return <video src="/assets/intro.mp4" width={1920} height={1080} autoPlay className="introVideo" />;
            default:
                return <font color="red">wat</font>;
        }
    }
}

export default App;
