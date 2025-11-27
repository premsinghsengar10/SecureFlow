const API_BASE = '/api';

export const emailService = {
    async getAllEmails(userId = 'user123') {
        const response = await fetch(`${API_BASE}/emails?userId=${userId}`);
        if (!response.ok) throw new Error('Failed to fetch emails');
        return response.json();
    },

    async getEmailsByLabel(userId = 'user123', label) {
        const response = await fetch(`${API_BASE}/emails?userId=${userId}&label=${label}`);
        if (!response.ok) throw new Error('Failed to fetch emails');
        return response.json();
    },

    async ingestEmail(emailData) {
        const response = await fetch(`${API_BASE}/ingest`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(emailData),
        });
        if (!response.ok) throw new Error('Failed to ingest email');
        return response.json();
    },
};
