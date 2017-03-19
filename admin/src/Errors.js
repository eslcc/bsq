import React, {Component} from 'react';
import {ErrorEvent} from './common-js/ProtoLoader';
import {socket as AdminSocket} from './AdminInterface';

export default class Errors extends Component {
    state = {
        errors: [],
    };

    componentDidMount() {
        AdminSocket.registerEventHandler("errorEvent", this._onError.bind(this));
    }

    _onError(message) {
        this.setState({
            errors: this.state.errors.concat(message.description),
        });
    }

    render() {
        return (
            <ul>
                {this.state.errors.reverse().map(err => (
                    <li key={err}><code>{err}</code></li>
                ))}
            </ul>
        )
    }
}
