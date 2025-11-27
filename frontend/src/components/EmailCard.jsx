import React from 'react';

const EmailCard = ({ email }) => {
    const getLabelClass = (label) => {
        return `label-badge ${label || 'other'}`;
    };

    const getCardClass = (label) => {
        return `email-card ${label || ''}`;
    };

    const getConfidenceScore = () => {
        if (!email.mlScores) return null;
        const label = email.label || 'other';
        const score = email.mlScores[label];
        if (score) {
            return (score * 100).toFixed(1) + '%';
        }
        return null;
    };

    return (
        <div className={getCardClass(email.label)}>
            <div className="email-header">
                <div>
                    <div className="email-from">{email.from || 'Unknown Sender'}</div>
                    <div className="email-subject">{email.subject || 'No Subject'}</div>
                </div>
            </div>

            <div className="email-body">
                {email.bodyText ?
                    (email.bodyText.length > 200 ? email.bodyText.substring(0, 200) + '...' : email.bodyText)
                    : 'No content'}
            </div>

            <div className="email-meta">
                <span className={getLabelClass(email.label)}>
                    {email.label || 'other'}
                </span>
                {getConfidenceScore() && (
                    <span className="confidence-score">
                        Confidence: {getConfidenceScore()}
                    </span>
                )}
                <span className="confidence-score">
                    {new Date(email.receivedAt).toLocaleString()}
                </span>
            </div>

            {email.explanations && email.explanations.length > 0 && (
                <div className="explanations">
                    <div className="explanations-title">Why this classification?</div>
                    <div>
                        {email.explanations.map((exp, idx) => (
                            <span key={idx} className="explanation-tag">{exp}</span>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default EmailCard;
