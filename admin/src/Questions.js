import React from 'react';
import AdminSocket from './lib/AdminSocket';
import * as rpc from './lib/ProtoLoader';

export default class Questions extends React.Component {
    state = {
        questions: [],
    };

    componentDidMount() {
        AdminSocket.registerResponseHandler('adminGetQuestionsResponse', this._onGetQuestionsResponse.bind(this));
        const message = rpc.RpcRequest.create();
        message.adminGetQuestionsRequest = rpc.AdminGetQuestionsRequest.create();
        AdminSocket.sendMessage(message);
    }

    _onGetQuestionsResponse(response) {
        this.setState({
            questions: response.questions
        });
    }

    activateQuestion(question) {
        const message = rpc.RpcRequest.create();
        message.adminSetActiveQuestionRequest = rpc.AdminSetActiveQuestionRequest.create();
        message.adminSetActiveQuestionRequest.questionId = question.id;
        AdminSocket.sendMessage(message);
    }

    render() {
        return (
            <div>
                {this.state.questions.sort((a, b) => a.id - b.id).map(question => (
                    <div key={question.id}>
                        {question.id}:
                        <em>{question.category}</em><br />
                        <strong>{question.question}</strong>
                        <button onClick={() => this.activateQuestion(question)}>Activate</button>
                    </div>
                ))}
            </div>
        );
    }

}