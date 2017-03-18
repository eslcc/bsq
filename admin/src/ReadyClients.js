import React, {Component} from 'react';
import { RpcRequest, AdminSetGameStateRequest, GameState, protosLoaded } from './lib/ProtoLoader';
import AdminSocket from './lib/AdminSocket';

export default class ReadyClients extends Component {
    state = {
        tablets: [
            {
                device: 'c0a5fa5f484820fa',
                team: '1',
                ready: false
            },
            {
                device: '2b14548e2af389b2',
                team: '2',
                ready: true
            },
            {
                device: '0d8fcafc01467d8f',
                team: '3',
                ready: false
            },

        ],
        displayList: false,
    };

    async startQuiz() {
        await protosLoaded;
        const message = RpcRequest.create();
        message.adminSetGameStateRequest = AdminSetGameStateRequest.create();
        message.adminSetGameStateRequest.newState = GameState.State.READY;
        AdminSocket.sendMessage(message);
    }

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
                    disabled={!(this.props.dangerZone || (readyClients === totalClients))}
                    onClick={this.startQuiz}
                >
                    Start Quiz
                </button>
                <br />
                {this.state.displayList && (
                    <ul className="devices">
                        {this.state.tablets.map(tablet => (
                            <li key={tablet.device} className={tablet.ready && 'ready'}>
                                <b>Device: </b> {tablet.device}; <b>Team: </b> {tablet.team};
                                <b>Ready: </b> {tablet.ready.toString()}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        )
    }
}