import React from 'react';
import {socket as AdminSocket} from './AdminInterface';
import * as rpc from './common-js/ProtoLoader';

export default class Questions extends React.Component {
    state = {
        questions: [],
        liveanswers: {},
    };

    componentDidMount() {
        AdminSocket.registerResponseHandler('adminGetQuestionsResponse', this._onGetQuestionsResponse.bind(this));
        AdminSocket.registerEventHandler('adminQuestionsChangedEvent', this._onQuestionsChanged.bind(this));
        AdminSocket.registerEventHandler('liveAnswersEvent', this._onLiveanswers.bind(this));
        const message = rpc.RpcRequest.create();
        message.adminGetQuestionsRequest = rpc.AdminGetQuestionsRequest.create();
        AdminSocket.sendMessage(message);
    }

    _onGetQuestionsResponse(response) {
        this.setState({
            questions: response.questions
        });
    }

    _onQuestionsChanged(message) {
        this.setState({
            questions: message.newQuestions,
        });
    }

    _onLiveanswers(message) {
        this.setState({
            liveanswers: message.answers
        });
    }

    activateQuestion(question) {
        const message = rpc.RpcRequest.create();
        message.adminSetActiveQuestionRequest = rpc.AdminSetActiveQuestionRequest.create();
        message.adminSetActiveQuestionRequest.questionId = question.id;
        AdminSocket.sendMessage(message);
    }

    autoActivate() {
        const question = this.state.questions.sort((a, b) => a.id - b.id).find(q => !q.alreadyPlayed);
        if (!question) {
            alert("Couldn't find unplayed question!");
            return;
        }
        this.activateQuestion(question);
    }

    render() {
        return (
            <div>
                <button onClick={() => this.autoActivate()}>Auto-activate next question</button>
                {this.state.questions.sort((a, b) => a.id - b.id).map(question => (
                    <div key={question.id}>
                        {question.alreadyPlayed ? <s>{this.renderQuestion(question)}</s> : this.renderQuestion(question)}
                        <br />
                    </div>
                ))}
                {this.renderLiveanswers()}
            </div>
        );
    }

    renderLiveanswers() {
        const state = !!this.props.state ? this.props.state.state : 0;
        if (
            state === rpc.GameState.State.QUESTION_ANSWERING ||
            state === rpc.GameState.State.QUESTION_LIVEANSWERS ||
            state === rpc.GameState.State.QUESTION_CLOSED ||
            state === rpc.GameState.State.QUESTION_ANSWERS_REVEALED
        ) {
            return (
                <div>
                    <h3>Live answers:</h3>
                    {JSON.stringify(this.state.liveanswers)}
                </div>
            )
        } else {
            return null;
        }
    }

    renderQuestion(question) {
        return (
            <div>
                {question.id}:
                <em>{question.category}</em><br />
                <strong>{question.question}</strong>
                <button onClick={() => this.activateQuestion(question)}>Activate</button>
            </div>
        );
    }
}