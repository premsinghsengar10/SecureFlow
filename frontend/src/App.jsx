import React, { useState, useEffect } from 'react';
import EmailCard from './components/EmailCard';
import TestEmailForm from './components/TestEmailForm';
import { emailService } from './services/api';
import websocketService from './services/websocket';

function App() {
    const [emails, setEmails] = useState([]);
    const [loading, setLoading] = useState(true);
    const [wsConnected, setWsConnected] = useState(false);
    const [notification, setNotification] = useState(null);

    const loadEmails = async () => {
        try {
            setLoading(true);
            const data = await emailService.getAllEmails();
            // Sort by receivedAt descending
            const sorted = data.sort((a, b) =>
                new Date(b.receivedAt) - new Date(a.receivedAt)
            );
            setEmails(sorted);
        } catch (error) {
            console.error('Error loading emails:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadEmails();

        // Connect to WebSocket
        websocketService.connect(
            (email) => {
                console.log('Received notification:', email);
                setNotification(email);

                // Add to emails list
                setEmails(prev => [email, ...prev]);

                // Clear notification after 5 seconds
                setTimeout(() => setNotification(null), 5000);
            },
            () => {
                console.log('WebSocket connected');
                setWsConnected(true);
            },
            (error) => {
                console.error('WebSocket error:', error);
                setWsConnected(false);
            }
        );

        return () => {
            websocketService.disconnect();
        };
    }, []);

    const handleEmailSent = () => {
        // Reload emails after a short delay to allow processing
        setTimeout(() => {
            loadEmails();
        }, 2000);
    };

    return (
        <div>
            <div className="header">
                <div className="container">
                    <h1>📬 Email Classification System</h1>
                    <p>Intelligent email categorization with real-time notifications</p>
                    <div className={`notification-badge ${wsConnected ? 'active' : ''}`}>
                        <span>{wsConnected ? '🟢' : '🔴'}</span>
                        <span>{wsConnected ? 'Connected' : 'Disconnected'}</span>
                    </div>
                    {notification && (
                        <div className="notification-badge active" style={{ marginLeft: '12px' }}>
                            <span>🔔</span>
                            <span>New important email from {notification.from}</span>
                        </div>
                    )}
                </div>
            </div>

            <div className="container">
                <TestEmailForm onEmailSent={handleEmailSent} />

                <div style={{ marginTop: '32px' }}>
                    <h2 style={{ marginBottom: '20px', fontSize: '24px' }}>Inbox ({emails.length})</h2>

                    {loading ? (
                        <div className="loading">Loading emails...</div>
                    ) : emails.length === 0 ? (
                        <div className="empty-state">
                            <h3>No emails yet</h3>
                            <p>Send a test email above to get started</p>
                        </div>
                    ) : (
                        <div className="email-list">
                            {emails.map((email) => (
                                <EmailCard key={email.id || email.messageId} email={email} />
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default App;
