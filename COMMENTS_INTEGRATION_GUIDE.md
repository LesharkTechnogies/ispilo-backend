# Nested Comments & Replies Integration Guide

This guide explains how to integrate and wire the newly implemented nested comments (parent-child replies) system from the backend to the frontend (Flutter).

## 1. Comment Data Structure

Comments are now represented as a recursive tree. A top-level comment has `parentCommentId: null`. Every comment can contain a list of `replies`, which are also `Comment` objects.

### JSON Response Details
```json
{
  "id": "comment-1",
  "postId": "post-123",
  "user": { "id": "user-A", "name": "Alice" },
  "content": "This is a root comment on the post.",
  "parentCommentId": null,
  "replies": [
    {
      "id": "reply-2",
      "postId": "post-123",
      "user": { "id": "user-B", "name": "Bob" },
      "content": "This is a reply to Alice's comment.",
      "parentCommentId": "comment-1",
      "replies": []
    }
  ],
  "createdAt": "2026-04-16T12:00:00Z"
}
```

## 2. API Endpoints

### Fetching Comments for a Post
- **Endpoint**: `GET /api/v1/posts/{postId}/comments`
- **Description**: Returns a paginated list of **top-level** comments. Replies are pre-populated within the `replies` array of each comment automatically by the backend.

### Creating a Comment or a Reply
- **Endpoint**: `POST /api/v1/posts/{postId}/comments`
- **Payload**:
  ```json
  {
    "content": "Your comment text here",
    "parentCommentId": "optional-id-of-parent-comment"
  }
  ```
- **Usage**:
    - If replying directly to the post, **omit** `parentCommentId` (or pass `null`).
    - If replying to a specific comment, pass its ID as the `parentCommentId`.

## 3. Flutter (Frontend) Integration

### A. The Data Model
You must parse the `replies` recursively list in your Dart model.

```dart
class Comment {
  final String id;
  final String postId;
  final String content;
  final String? parentCommentId;
  final List<Comment> replies;

  Comment({
    required this.id,
    required this.postId,
    required this.content,
    this.parentCommentId,
    this.replies = const [],
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    return Comment(
      id: json['id'],
      postId: json['postId'],
      content: json['content'],
      parentCommentId: json['parentCommentId'],
      replies: (json['replies'] as List<dynamic>?)
              ?.map((e) => Comment.fromJson(e))
              .toList() ?? [],
    );
  }
}
```

### B. Recursive UI Widget
To render branching comments, use a recursive widget that increases left indentation (`padding`) for each level of replies.

```dart
import 'package:flutter/material.dart';

class CommentTile extends StatelessWidget {
  final Comment comment;
  final double indentPadding;
  final Function(Comment) onReplyTap;

  const CommentTile({
    Key? key,
    required this.comment,
    this.indentPadding = 0.0,
    required this.onReplyTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // The Comment itself
        Padding(
          padding: EdgeInsets.only(left: indentPadding, top: 8, bottom: 8),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(comment.content),
              TextButton(
                onPressed: () => onReplyTap(comment),
                child: const Text("Reply"),
              )
            ],
          ),
        ),

        // Its Replies (Recursion with increased indent)
        if (comment.replies.isNotEmpty)
          ...comment.replies.map((reply) => CommentTile(
                comment: reply,
                // Indent replies by an extra 30 pixels
                indentPadding: indentPadding + 30.0, 
                onReplyTap: onReplyTap,
              )),
      ],
    );
  }
}
```

### C. State Management
When building the comment screen:
1. Fetch root comments from `GET /api/v1/posts/{postId}/comments`.
2. Keep an optional `Comment? replyingTo` state variable.
3. When the user taps "Reply" on a comment tile, set `replyingTo = comment`. Focus the text field, maybe show a banner "Replying to UserX".
4. When they submit, check if `replyingTo` is set. If so, send its ID as the `parentCommentId` in the `POST` request. If not, omit it.
