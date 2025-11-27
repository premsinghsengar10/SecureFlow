# Email Classification & Notification System

A microservices-based system to ingest, classify, and notify users about important emails.

## Architecture

### Backend (Spring Boot Monolith)
- **Ingest Module**: REST endpoint `/api/ingest` to receive emails
- **Processor Module**: Feature extraction from email content
- **Orchestrator Module**: ML-based classification using external ML service
- **Notification Module**: Real-time WebSocket notifications for important emails

### ML Service (Python/FastAPI)
- Provides `/predict` endpoint for email classification
- Returns scores for: important, spam, fraud, other
- Includes explanations for classification decisions

### Infrastructure
- **MongoDB**: Email storage
- **RabbitMQ**: Internal message broker for async processing
- **React Frontend**: User interface with real-time updates

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Node.js (for local frontend development)

### Running with Docker

```bash
# Start all services
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

Services will be available at:
- Backend API: http://localhost:8080
- Frontend: http://localhost:3000 (when running locally)
- ML Service: http://localhost:8000
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- MongoDB: localhost:27017

### Running Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Ingest Email
```bash
POST http://localhost:8080/api/ingest
Content-Type: application/json

{
  "from": "sender@example.com",
  "subject": "Test Email",
  "bodyText": "This is a test message"
}
```

### Get All Emails
```bash
GET http://localhost:8080/api/emails?userId=user123
```

### Get Emails by Label
```bash
GET http://localhost:8080/api/emails?userId=user123&label=important
```

## Testing

1. Start all services with `docker-compose up -d --build`
2. Open frontend at http://localhost:3000
3. Use the test form to send emails
4. Watch real-time classification and notifications

## Project Structure

```
Email-Notify/
├── backend/                 # Spring Boot monolith
│   ├── src/main/java/com/email/backend/
│   │   ├── config/         # RabbitMQ, WebSocket config
│   │   ├── controller/     # REST endpoints
│   │   ├── model/          # Email entity
│   │   ├── repository/     # MongoDB repository
│   │   └── service/        # Business logic
│   ├── Dockerfile
│   └── pom.xml
├── ml-service/             # Python ML service
│   ├── app.py
│   ├── requirements.txt
│   └── Dockerfile
├── frontend/               # React UI
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   └── App.jsx
│   └── package.json
└── docker-compose.yml
```

## Features

- ✅ Email ingestion via REST API
- ✅ Automated feature extraction
- ✅ ML-based classification (important/spam/fraud/other)
- ✅ Real-time WebSocket notifications
- ✅ Explanation for classification decisions
- ✅ Modern React UI with live updates
- ✅ Dockerized deployment

## Future Enhancements

- OAuth integration for Gmail/Outlook
- User authentication and multi-user support
- Advanced fraud detection
- Email attachments handling
- Mobile app with push notifications
- Model retraining pipeline
