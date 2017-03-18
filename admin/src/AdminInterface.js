import React from 'react';
import AdminSocket from './lib/AdminSocket';
import { protosLoaded } from './lib/ProtoLoader';

import GameState from './GameState';
import Questions from './Questions';
import ReadyClients from './ReadyClients';
import Errors from './Errors';

export default class AdminInterface extends React.Component {
    state = {
        rootsLoaded: false,
        dangerZone: false,
    };

    dangerZoneCheckbox = null;

    componentDidMount() {
        protosLoaded.then(() => {
            this.setState({rootsLoaded: true});
            AdminSocket.open();
            window.as = AdminSocket;
        });
    }

    checkDangerZone() {
        this.setState({dangerZone: this.dangerZoneCheckbox.checked});
    }

    _onResponse(message) {

    }

    _onEvent(message) {

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
                    <ReadyClients dangerZone={this.state.dangerZone} />
                    <br />
                    <h2>State: </h2>
                    <GameState dangerZone={this.state.dangerZone} />
                    <br />
                    <h2>Questions: </h2>
                    <Questions />
                    <h2>Errors: </h2>
                    <Errors />
                </div>
            );
        } else {
            return <div>Loading protos...</div>;
        }
    }
}