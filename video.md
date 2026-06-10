# VIDEO_MODULE.md

# Video Posting Module Architecture

## Purpose

Add TikTok-style short video posting functionality to the existing application.

The module should allow users to:

* Upload videos
* Publish videos
* View videos in feeds
* Like videos
* Comment on videos
* Follow creators
* Browse hashtags
* Receive engagement notifications

The solution should be optimized for:

* Low storage costs
* Low bandwidth consumption
* Fast feed loading
* Smooth scrolling experience
* Scalability

---

# Technology Stack

## Existing Stack

### Backend

* Spring Boot
* PostgreSQL
* Redis

### Frontend

* Flutter

### Infrastructure

* Cloudflare R2
* Cloudflare CDN
* Cloudflare WAF

---

# Architecture Overview

```text
Flutter App
      |
      |
      v
Spring Boot API
      |
      |
      v
Cloudflare R2
      |
      |
      v
Video Processing Worker
      |
      |
      v
Optimized Video Assets
```

---

# Core Principle

The mobile application should never be responsible for final video optimization.

Flutter uploads the original file.

The backend processing pipeline becomes responsible for:

* Trimming
* Compression
* Resolution optimization
* Thumbnail generation
* Publishing

This ensures:

* Consistent quality
* Lower storage costs
* Better scalability
* Reduced client complexity

---

# Upload Flow

## Step 1

User selects a video.

The application displays:

* Duration
* File size
* Preview

---

## Step 2

User enters:

* Caption
* Hashtags

Example:

```text
Amazing sunset 🌅

#travel
#nature
#kenya
```

---

## Step 3

User presses Publish.

Video uploads directly to Cloudflare R2.

Benefits:

* Backend avoids handling large files.
* Faster uploads.
* Reduced API server load.

---

## Step 4

Create Video Record

Status:

```text
UPLOADING
```

After upload:

```text
PROCESSING
```

Video is not yet visible.

---

# Video Processing Pipeline

## Processing Stages

```text
Upload Complete
       ↓
Validate File
       ↓
Check Duration
       ↓
Trim If Required
       ↓
Compress Video
       ↓
Generate Thumbnail
       ↓
Generate Preview Image
       ↓
Store Final Assets
       ↓
ACTIVE
```

---

# Video Duration Policy

## Published Duration Limit

Maximum:

```text
3 Minutes
```

---

## Handling Longer Videos

If video duration exceeds:

```text
180 Seconds
```

The system automatically:

```text
Keep First 180 Seconds
Remove Remaining Content
```

Example:

```text
Uploaded:
5 Minutes 40 Seconds

Published:
3 Minutes
```

Benefits:

* Consistent content length
* Reduced storage
* Reduced bandwidth
* Faster feed consumption

---

# Video Optimization Strategy

## Resolution Limits

Maximum resolution:

```text
1080 × 1920
```

If user uploads:

```text
1440p
2K
4K
```

Video is resized.

Benefits:

* Lower storage
* Faster delivery
* Better mobile performance

---

## Frame Rate

Convert all videos to:

```text
30 FPS
```

Benefits:

* Smaller files
* Consistent playback
* Reduced bandwidth

---

## Codec

Recommended:

```text
H.264
```

Future upgrade:

```text
H.265
```

Benefits:

* Better compression
* Reduced storage usage

---

# Storage Optimization

## Original File

Original uploads are temporary.

Purpose:

* Validation
* Compression
* Trimming

---

## Final Storage

Store only:

* Optimized Video
* Thumbnail
* Preview Image
* Metadata

After processing:

```text
Delete Original Upload
```

Benefits:

* Major storage savings
* Lower R2 costs

---

# Thumbnail Generation

Generate:

### Main Thumbnail

Used for:

* Search results
* Profile pages
* Video previews

---

### Preview Image

Used for:

* Feed placeholders
* Fast loading

---

## Image Format

Recommended:

```text
WebP
```

Benefits:

* Smaller files
* Faster loading

---

# Feed System

## Following Feed

Display videos from followed users.

Ordering:

```text
Newest First
```

---

## Discover Feed

Display recommended content.

Ranking factors:

* Watch Time
* Completion Rate
* Likes
* Comments
* Shares
* Recency

---

## Trending Feed

Display fast-growing content.

Ranking factors:

* Views
* Likes
* Growth Rate
* Engagement

---

# Video Playback Strategy

## Auto Play

When video becomes visible:

```text
Play Automatically
```

When no longer visible:

```text
Pause
```

Benefits:

* Reduced resource usage
* Better battery life

---

# Client Caching Strategy

## Problem

Without caching:

```text
Watch Video
Scroll Down
Request New Video

Scroll Up
Request Again
```

This creates repeated downloads.

---

## Solution

Keep cache of:

```text
Current Video
Previous Video
Next Two Videos
```

Example:

```text
Current = Video 50

Cached:

49
50
51
52
```

When moving to Video 51:

```text
Remove 49
Prefetch 53
```

Benefits:

* Fewer R2 requests
* Smooth scrolling
* Faster playback

---

# View Counting

## Count View When

User watches:

```text
At Least 3 Seconds
```

OR

```text
30% Of Video
```

---

## Ignore

Do not count:

* Instant skips
* Bots
* Extremely short views

---

# Like System

Users can:

* Like
* Unlike

Rules:

```text
One Like Per User
```

Track:

* Total Likes
* User Like Status

---

# Comment System

## Features

Users can:

* Add Comment
* Edit Comment
* Delete Comment
* Reply To Comment
* Like Comment

---

## Structure

Recommended:

```text
Comment
   ↓
Reply
```

Maximum nesting:

```text
2 Levels
```

---

# Hashtags

Example:

```text
#travel
#music
#kenya
```

Capabilities:

* Search
* Discovery
* Trending Topics

---

# Notifications

Generate notifications for:

## Video Events

* Video Like
* Comment
* Reply

## Social Events

* New Follower

---

# Analytics

Track per video:

* Views
* Unique Viewers
* Likes
* Comments
* Shares
* Watch Time
* Completion Rate

---

# Moderation

Before publication:

## Validation

* File Type
* File Size
* Corruption Check

---

## Safety

* Malware Scan
* Spam Detection
* Content Review

---

# Video Status Lifecycle

```text
DRAFT
    ↓
UPLOADING
    ↓
PROCESSING
    ↓
ACTIVE
```

Failure:

```text
PROCESSING
    ↓
FAILED
```

Review:

```text
PROCESSING
    ↓
REVIEW_REQUIRED
```

---

# Database Changes

## New Video Entity

Store:

* Creator
* Caption
* Video URL
* Thumbnail URL
* Duration
* Status
* View Count
* Like Count
* Comment Count
* Share Count

---

## New Comment Entity

Store:

* User
* Video
* Parent Comment
* Content

---

## New Like Entity

Store:

* User
* Video

---

## New Hashtag Entity

Store:

* Name
* Usage Count

---

# Redis Caching

Use Redis for:

* Trending Videos
* Most Viewed Videos
* Most Liked Videos
* Feed Caching
* View Counters
* Like Counters

Benefits:

* Fast reads
* Reduced database load

---

# Future Enhancements

## Phase 2

* Saved Videos
* Video Drafts
* Mentions
* Collections

---

## Phase 3

* Duets
* Stitch Videos
* Live Streaming
* Creator Monetization
* Sponsored Videos

---

# Success Metrics

## Technical

* Upload Success Rate
* Processing Time
* Feed Load Time
* Cache Hit Rate

## Business

* Videos Posted Per Day
* Average Watch Time
* Engagement Rate
* Retention Rate

---

# Key Decisions

* Videos uploaded directly to Cloudflare R2
* Backend performs all optimization
* Videos automatically trimmed to 3 minutes
* Videos resized to mobile-friendly resolution
* Original uploads deleted after processing
* Feed videos cached on device
* Likes and comments supported
* Hashtag discovery enabled
* Analytics collected for recommendations

This design provides a scalable TikTok-style video system while minimizing storage costs, bandwidth usage, and backend load.

