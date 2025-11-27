import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = [];
    }

    connect(onMessageReceived, onConnected, onError) {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect(
            {},
            (frame) => {
                console.log('Connected: ' + frame);
                this.connected = true;

                // Subscribe to notifications
                const subscription = this.stompClient.subscribe('/topic/notifications', (message) => {
                    const email = JSON.parse(message.body);
                    onMessageReceived(email);
                });

                this.subscriptions.push(subscription);

                if (onConnected) {
                    onConnected();
                }
            },
            (error) => {
                console.error('WebSocket error:', error);
                this.connected = false;
                if (onError) {
                    onError(error);
                }

                // Retry connection after 5 seconds
                setTimeout(() => {
                    console.log('Retrying connection...');
                    this.connect(onMessageReceived, onConnected, onError);
                }, 5000);
            }
        );
    }

    disconnect() {
        if (this.stompClient !== null && this.connected) {
            this.subscriptions.forEach(sub => sub.unsubscribe());
            this.stompClient.disconnect();
            this.connected = false;
            console.log('Disconnected');
        }
    }

    isConnected() {
        return this.connected;
    }
}

export default new WebSocketService();
