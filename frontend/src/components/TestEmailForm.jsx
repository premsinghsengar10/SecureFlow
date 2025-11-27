import React, { useState } from 'react';
import { emailService } from '../services/api';

const TestEmailForm = ({ onEmailSent }) => {
    const [formData, setFormData] = useState({
        from: 'test@example.com',
        subject: 'Test Email',
        bodyText: 'This is a test email message.'
    });
    const [status, setStatus] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setStatus(null);

        try {
            const result = await emailService.ingestEmail(formData);
            setStatus({ type: 'success', message: `Email sent! Message ID: ${result.messageId}` });

            // Reset form
            setFormData({
                from: 'test@example.com',
                subject: '',
                bodyText: ''
            });

            if (onEmailSent) {
                onEmailSent();
            }
        } catch (error) {
            setStatus({ type: 'error', message: 'Failed to send email: ' + error.message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="test-section">
            <h2>📧 Send Test Email</h2>
            <form className="test-form" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="from">From:</label>
                    <input
                        type="email"
                        id="from"
                        name="from"
                        value={formData.from}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="subject">Subject:</label>
                    <input
                        type="text"
                        id="subject"
                        name="subject"
                        value={formData.subject}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="bodyText">Message:</label>
                    <textarea
                        id="bodyText"
                        name="bodyText"
                        value={formData.bodyText}
                        onChange={handleChange}
                        required
                    />
                </div>

                <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? 'Sending...' : 'Send Email'}
                </button>

                {status && (
                    <div className={`status-message ${status.type}`}>
                        {status.message}
                    </div>
                )}
            </form>
        </div>
    );
};

export default TestEmailForm;
