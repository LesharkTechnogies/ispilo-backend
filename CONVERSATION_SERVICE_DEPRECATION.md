# ⚠️ CONVERSATION SERVICE - DEPRECATION NOTICE

## Status: ❌ DEPRECATED - USE MessageService INSTEAD

---

## Why ConversationService is Redundant

### Problem
- **ConversationService**: Uses mock in-memory storage (no API integration)
- **MessageService**: Already provides all conversation + messaging functionality with full API integration

### Solution
**REMOVE ConversationService and USE MessageService**

---

## Migration Guide

### ConversationService Methods → MessageService Equivalents

| ConversationService | MessageService | Status |
|---------------------|----------------|--------|
| `getOrCreateConversation()` | `createConversation()` or `getConversationById()` | ✅ REPLACED |
| `fetchMessages()` | `getMessages()` | ✅ REPLACED |
| `sendMessage()` | `sendMessage()` | ✅ REPLACED |
| `markAsRead()` | `markConversationAsRead()` | ✅ REPLACED |
| `trackMessageRead()` | `markMessageAsRead()` | ✅ REPLACED |
| `getMessageCount()` | `getUnreadMessageCount()` | ✅ REPLACED |

---

## Migration Code Examples

### BEFORE (ConversationService - MOCK)
```dart
import 'conversation_service.dart';

final conv = await ConversationService.instance.getOrCreateConversation(
  sellerId: 'seller-123',
  sellerName: 'John Store',
  sellerAvatar: 'https://...',
);

final messages = await ConversationService.instance.fetchMessages(conv['id']);
```

### AFTER (MessageService - API)
```dart
import 'message_service.dart';

// Get existing conversation or create new
final conversation = await MessageService.getConversationById('conv-123') ??
    await MessageService.createConversation(
      name: 'John Store',
      participantIds: ['user-123', 'seller-123'],
    );

// Fetch messages
final messages = await MessageService.getMessages(conversation!.id);

// Send message
await MessageService.sendMessage(
  conversationId: conversation.id,
  content: 'Hello!',
);

// Mark as read
await MessageService.markConversationAsRead(conversation.id);
```

---

## Benefits of Using MessageService

✅ **API Integrated**: All calls go to backend  
✅ **Real Data**: No mock data  
✅ **Scalable**: Handles any number of conversations  
✅ **Reliable**: Error handling & offline caching  
✅ **Streams**: Real-time updates via streams  
✅ **Encryption**: Supports encrypted messages  
✅ **Maintained**: Single source of truth  

---

## ACTION REQUIRED

### Step 1: Delete ConversationService ❌
```bash
rm lib/core/services/conversation_service.dart
```

### Step 2: Replace All Imports
```
Find: import 'conversation_service.dart'
Replace with: import 'message_service.dart'
```

### Step 3: Update All References
```
Find: ConversationService.instance
Replace with: MessageService
```

### Step 4: Test All Messaging Features
- ✅ Create conversation
- ✅ Fetch messages
- ✅ Send message
- ✅ Mark as read
- ✅ Delete conversation

---

## Complete MessageService API Reference

```dart
// Conversations
getConversations()                      // Get all conversations
getConversationById(id)                 // Get specific conversation
createConversation(name, participants)  // Create new conversation
getUnreadConversations()                // Unread only
getUnreadMessageCount()                 // Count of unread

// Messages
getMessages(conversationId)             // Get messages in conversation
sendMessage(conversationId, content)    // Send message
editMessage(conversationId, msgId)      // Edit message
deleteMessage(conversationId, msgId)    // Delete message
searchMessages(conversationId, query)   // Search in conversation

// Status
markMessageAsRead(conversationId, msgId)        // Mark as read
markConversationAsRead(conversationId)          // Mark all as read

// Participants
addParticipant(conversationId, userId)          // Add user
removeParticipant(conversationId, userId)       // Remove user
leaveConversation(conversationId)               // Leave conversation

// Management
deleteConversation(conversationId)              // Delete conversation
listenForMessages(conversationId)               // Setup listener

// Streams
conversationStream                      // Listen to conversations
messageStream                          // Listen to messages
```

---

## Files to Update

Search for `ConversationService` usage in:

```
grep -r "ConversationService" lib/
```

Expected files with ConversationService imports:
- lib/presentation/chat/chat_page.dart
- lib/presentation/marketplace/product_detail.dart
- Any other chat-related pages

---

## Testing Checklist

- [ ] All imports updated to MessageService
- [ ] No ConversationService imports remain
- [ ] All tests use MessageService
- [ ] ConversationService file deleted
- [ ] Chat page works with MessageService
- [ ] Messages send/receive/display correctly
- [ ] Conversations list updates
- [ ] Unread badges work

---

## Timeline

- **Immediate**: Delete ConversationService.dart
- **Next PR**: Update all imports across codebase
- **Testing**: Verify all messaging features work
- **Complete**: Remove UnreadMessageService (if it only served ConversationService)

---

## Summary

| Item | Action | Status |
|------|--------|--------|
| ConversationService | DELETE | ⏳ PENDING |
| Update Imports | FIND & REPLACE | ⏳ PENDING |
| MessageService | USE | ✅ READY |
| Tests | RUN | ⏳ PENDING |

---

**Date**: January 16, 2026  
**Impact**: Medium (internal refactoring)  
**Risk**: Low (MessageService fully tested)  
**Benefit**: Eliminate redundancy, single source of truth  

Delete `ConversationService` and migrate to `MessageService` NOW!
