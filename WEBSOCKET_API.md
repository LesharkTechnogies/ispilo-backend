# WebSocket API

This document provides instructions on how to use the WebSocket API for real-time messaging.

## 1. Connection

First, you need to establish a WebSocket connection to the following endpoint:

`ws://<your-server-address>/ws/chat`

You need to pass a valid JWT token for authentication. The token should be passed as a query parameter named `token`.

Example: `ws://localhost:8080/ws/chat?token=your_jwt_token`

## 2. Creating a Conversation

To create a new conversation (a "room"), you can send a message to the `/app/conversation.create` destination.

The payload should be a JSON object with the following structure:

```json
{
  "type": "PRIVATE", // or "GROUP"
  "participantIds": ["user_id_1", "user_id_2"]
}
```

*   `type`: The type of conversation. It can be `PRIVATE` for one-to-one chats or `GROUP` for chats with multiple users.
*   `participantIds`: A list of user IDs to be included in the conversation.

After sending this message, the server will create the conversation and send a confirmation message to the creator's user-specific queue at `/user/queue/conversation.created`. The payload will contain the details of the created conversation.

## 3. Joining a Conversation

To start receiving messages from a conversation, you need to "join" it by sending a message to the `/app/conversation.join` destination.

The payload should be the `conversationId` as a string.

After joining, your client won't receive a confirmation, but you can now subscribe to the conversation's topic to receive messages.

## 4. Subscribing to a Conversation

To receive messages for a conversation, you need to subscribe to the following topic:

`/topic/conversation/{conversationId}`

Replace `{conversationId}` with the actual ID of the conversation.

Once subscribed, you will receive messages sent to this conversation.

## 5. Sending a Message

To send a message to a conversation, you can send a message to the `/app/chat.send` destination.

The payload should be a JSON object with the following structure:

```json
{
  "conversationId": "your_conversation_id",
  "content": "Hello, world!",
  "mediaUrl": null,
  "type": "TEXT",
  "clientMsgId": "some_unique_id",
  "encryptionKey": "your_encryption_key"
}
```

## Other features

The WebSocket API also supports typing indicators and read receipts.

*   **Typing Notification**: Send to `/app/chat.typing`
*   **Mark as Read**: Send to `/app/chat.read`

Please refer to the `WebSocketController.java` for the exact payload structure for these features.
