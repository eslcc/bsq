import React from 'react';
import {socket as AdminSocket} from './AdminInterface';
import {getKeyByValue} from './common-js/helpers';
import {
    RpcRequest,
    GetGameStateRequest,
    AdminResetStateRequest,
    AdminSetGameStateRequest,
    GameState
} from './common-js/ProtoLoader';

export default class GameStateComponent extends React.Component {
    _resetState() {
        const message = RpcRequest.create();
        message.adminResetStateRequest = AdminResetStateRequest.create();
        AdminSocket.sendMessage(message);
    }

    _setState(newState) {
        const message = RpcRequest.create();
        message.adminSetGameStateRequest = AdminSetGameStateRequest.create();
        message.adminSetGameStateRequest.newState = newState;
        AdminSocket.sendMessage(message);
    }

    renderAdvanceButton() {
        switch (this.props.state.state) {
            case GameState.State.QUESTION_ANSWERING:
                return (
                    <button
                        onClick={() => this._setState(GameState.State.QUESTION_LIVEANSWERS)}
                    >
                        Show Live Answers
                    </button>
                );
            case GameState.State.QUESTION_LIVEANSWERS:
                return (
                    <button
                        onClick={() => this._setState(GameState.State.QUESTION_CLOSED)}
                    >
                        Close Answers
                    </button>
                );
            case GameState.State.QUESTION_CLOSED:
                return (
                    <button
                        onClick={() => this._setState(GameState.State.QUESTION_ANSWERS_REVEALED)}
                    >
                        Reveal Answers and Calculate Scores
                    </button>
                );
            default:
                return null;
        }
    }

    render() {
        const {state} = this.props;
        if (typeof state === "undefined" || state === null) {
            return <div>loading state</div>;
        }
        // debugger;
        return (
            <div>
                <strong>State:</strong>
                <em>
                    <span>{(state.state || 0)}</span>
                    <span>({getKeyByValue(GameState.State, (state.state || 0))})</span>
                </em>
                <strong>Question:</strong>
                <em>{!!state.currentQuestion ? state.currentQuestion.id : 'NONE'}</em>
                {this.renderAdvanceButton()}
            </div>
        );
    }
}