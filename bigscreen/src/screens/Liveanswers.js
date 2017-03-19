import React, {Component, PropTypes} from 'react';
import Knob from 'react-canvas-knob';
import './Liveanswers.css';
import {socket} from '../App';

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        const temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    return array;
}

const COLORS = [
    '#FFC107',
    '#E91E63',
    '#00BCD4',
    '#4CAF50'
];

export default class Liveanswers extends Component {
    state = {
        liveanswers: {},
        order: [],
        total: 0,
    };

    componentDidMount() {
        socket.registerEventHandler('liveAnswersEvent', this._onLiveanswers.bind(this));
    }

    componentWillMount() {
        this.refreshOrder();
    }

    refreshOrder(count = 4) {
        const order = shuffleArray(Array.apply(null, {length: count}).map(Number.call, Number));
        this.setState({order});
    }

    _onLiveanswers(message) {
        this.setState({
            liveanswers: message.answers,
            total: Object.keys(message.answers).map(k => message.answers[k]).reduce((a, b) => a + b, 0),
        });
    }

    renderKnobs() {
        return this.state.order.map((answer, index) => {
            const knobSettings = {
                value: (this.state.liveanswers[answer] / this.state.total) * 100 || 0,
                onChange: Function.prototype,
                bgColor: '#fafafa',
                fgColor: COLORS[index]
            };
            return (
                <div key={answer} className="liveanswer">
                    <Knob {...knobSettings} />
                </div>
            );
        })
    }

    render() {
        return (
            <div className="liveanswers">
                {this.renderKnobs()}
            </div>
        )
    }
}