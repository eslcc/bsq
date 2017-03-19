import React, { Component, PropTypes } from 'react';
import './Question.css';

const Question = ({ question, revealAnswers }) => (
    <div className="question">
        <h2 className="question-category">{question.category}</h2>
        <h1 className="question-title">{question.question}</h1>
        <div className="question-answers">
            {question.answers.map(answer => (
                <div key={answer.id} className={`question-answer ${(revealAnswers && answer.correct) ? 'correct' : ''}`}>
                    {answer.text}
                </div>
            ))}
        </div>
    </div>
);

Question.propTypes = {
    question: PropTypes.shape({
        category: PropTypes.string,
        question: PropTypes.string,
        answers: PropTypes.arrayOf(PropTypes.shape({
            id: PropTypes.number,
            text: PropTypes.string,
            correct: PropTypes.bool,
        })),
    }).isRequired,
    revealAnswers: PropTypes.bool,
};

export default Question;
