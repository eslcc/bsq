import React from 'react';
import AdminSocket from './lib/AdminSocket';
import {getKeyByValue} from './lib/helpers';
import {RpcRequest, GetGameStateRequest, AdminResetStateRequest} from './lib/ProtoLoader';

export default class GameState extends React.Component {
    state = {
        state: null,
    };

    componentDidMount() {
        AdminSocket.registerResponseHandler('getGameStateResponse', this._onGetGameState.bind(this));
        AdminSocket.registerEventHandler('gameStateChangeEvent', this._onStateChange.bind(this));
        const message = RpcRequest.create();
        message.getGameStateRequest = GetGameStateRequest.create();
        AdminSocket.sendMessage(message);
    }

    _onGetGameState(message) {
        this.setState({
            state: message.state,
        });
    }

    _onStateChange(event) {
        this.setState({
            state: event.newState,
        });
    }

    _resetState() {
        const message = RpcRequest.create();
        message.adminResetStateRequest = AdminResetStateRequest.create();
        AdminSocket.sendMessage(message);
    }

    render() {
        if (this.state.state === null) {
            return <div>loading state</div>;
        }
        // debugger;
        return (
            <div>
                <strong>State:</strong>
                <em>
                    <span>{(this.state.state.state || 0)}</span>
                    <span>({getKeyByValue(GameState.State, (this.state.state.state || 0))})</span>
                </em>
                <strong>Question:</strong>
                <em>{this.state.state.currentQuestion.id}</em>
                <button disabled={!this.props.dangerZone}>RESET STATE ENTIRELY</button>
            </div>
        );
    }
}