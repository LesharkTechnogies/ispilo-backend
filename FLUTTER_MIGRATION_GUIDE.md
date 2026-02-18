# Flutter Mock Data to API Migration Guide

This guide explains how to replace all hardcoded mock data in the Flutter app with actual API calls to the Spring Boot backend.

## Overview

The Flutter application currently uses static mock data from Dart files:
- `marketplace_data.dart` - Product listings
- `mock_education_data.dart` - Videos and courses
- `mock_data.dart` - Users, posts, stories

These will be progressively replaced with API calls as the backend endpoints become available.

## Migration Steps

### 1. Products/Marketplace

#### Before (Mock Data)
```dart
// lib/presentation/marketplace/marketplace.dart
import 'package:ispilo_main/data/marketplace_data.dart';

class MarketplaceState extends State<Marketplace> {
  @override
  void initState() {
    super.initState();
    // Use static data
    final products = MarketplaceData.allProducts;
  }
}
```

#### After (API Call)
```dart
// lib/presentation/marketplace/marketplace.dart
import 'package:ispilo_main/core/services/api_service.dart';
import 'package:ispilo_main/model/product.dart';

class MarketplaceState extends State<Marketplace> {
  List<ProductModel> _products = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchProducts();
  }

  Future<void> _fetchProducts({int page = 0}) async {
    setState(() => _isLoading = true);
    try {
      final response = await ApiService.get('/products?page=$page&size=20');
      final products = (response['content'] as List)
          .map((json) => ProductModel.fromJson(json))
          .toList();
      
      setState(() => _products = products);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _searchProducts(String keyword) async {
    setState(() => _isLoading = true);
    try {
      final response = await ApiService.get(
        '/products/search?keyword=$keyword&page=0&size=20',
      );
      final products = (response['content'] as List)
          .map((json) => ProductModel.fromJson(json))
          .toList();
      
      setState(() => _products = products);
    } catch (e) {
      print('Search error: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _filterByCategory(String category) async {
    setState(() => _isLoading = true);
    try {
      final response = await ApiService.get(
        '/products/category/$category?page=0&size=20',
      );
      final products = (response['content'] as List)
          .map((json) => ProductModel.fromJson(json))
          .toList();
      
      setState(() => _products = products);
    } catch (e) {
      print('Filter error: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }
}
```

#### Product Model
```dart
// lib/model/product.dart
class ProductModel {
  final String id;
  final String title;
  final String description;
  final double price;
  final String mainImage;
  final List<String> images;
  final String category;
  final String condition;
  final double rating;
  final int reviewCount;
  final String location;
  final int stockQuantity;
  final bool isAvailable;
  final SellerModel seller;
  final DateTime createdAt;

  ProductModel({
    required this.id,
    required this.title,
    required this.description,
    required this.price,
    required this.mainImage,
    required this.images,
    required this.category,
    required this.condition,
    required this.rating,
    required this.reviewCount,
    required this.location,
    required this.stockQuantity,
    required this.isAvailable,
    required this.seller,
    required this.createdAt,
  });

  factory ProductModel.fromJson(Map<String, dynamic> json) {
    return ProductModel(
      id: json['id'],
      title: json['title'],
      description: json['description'],
      price: (json['price'] as num).toDouble(),
      mainImage: json['mainImage'],
      images: List<String>.from(json['images'] ?? []),
      category: json['category'],
      condition: json['condition'],
      rating: (json['rating'] as num).toDouble(),
      reviewCount: json['reviewCount'],
      location: json['location'],
      stockQuantity: json['stockQuantity'],
      isAvailable: json['isAvailable'],
      seller: SellerModel.fromJson(json['seller']),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class SellerModel {
  final String id;
  final String businessName;
  final String? businessLogo;
  final bool isVerified;

  SellerModel({
    required this.id,
    required this.businessName,
    this.businessLogo,
    required this.isVerified,
  });

  factory SellerModel.fromJson(Map<String, dynamic> json) {
    return SellerModel(
      id: json['id'],
      businessName: json['businessName'],
      businessLogo: json['businessLogo'],
      isVerified: json['isVerified'] ?? false,
    );
  }
}
```

### 2. Education Hub (Videos & Courses)

#### Before (Mock Data)
```dart
// lib/presentation/education_hub/education_hub.dart
import 'package:ispilo_main/data/mock_education_data.dart';

class EducationHubState extends State<EducationHub> {
  @override
  void initState() {
    super.initState();
    final videos = mockEducationVideos;
    final courses = mockEnrolledCourses;
  }
}
```

#### After (API Calls)
```dart
// lib/presentation/education_hub/education_hub.dart
import 'package:ispilo_main/core/services/api_service.dart';
import 'package:ispilo_main/model/education.dart';

class EducationHubState extends State<EducationHub> {
  List<EducationVideoModel> _videos = [];
  List<CourseModel> _courses = [];
  List<CourseEnrollmentModel> _myCourses = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchTrendingVideos();
    _fetchMyCourses();
  }

  Future<void> _fetchTrendingVideos() async {
    setState(() => _isLoading = true);
    try {
      final response = await ApiService.get(
        '/education/videos/trending?page=0&size=10',
      );
      final videos = (response['content'] as List)
          .map((json) => EducationVideoModel.fromJson(json))
          .toList();
      
      setState(() => _videos = videos);
    } catch (e) {
      print('Error fetching videos: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _fetchPopularCourses() async {
    try {
      final response = await ApiService.get(
        '/education/courses/popular?page=0&size=10',
      );
      final courses = (response['content'] as List)
          .map((json) => CourseModel.fromJson(json))
          .toList();
      
      setState(() => _courses = courses);
    } catch (e) {
      print('Error fetching courses: $e');
    }
  }

  Future<void> _fetchMyCourses() async {
    try {
      final response = await ApiService.get(
        '/education/my-courses?page=0&size=10',
      );
      final courses = (response['content'] as List)
          .map((json) => CourseEnrollmentModel.fromJson(json))
          .toList();
      
      setState(() => _myCourses = courses);
    } catch (e) {
      print('Error fetching my courses: $e');
    }
  }

  Future<void> _enrollCourse(String courseId) async {
    try {
      await ApiService.post('/education/courses/$courseId/enroll', {});
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Enrolled successfully!')),
      );
      _fetchMyCourses();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Enrollment failed: $e')),
      );
    }
  }
}
```

#### Education Models
```dart
// lib/model/education.dart

class EducationVideoModel {
  final String id;
  final String title;
  final String channel;
  final String thumbnail;
  final String videoUrl;
  final String duration;
  final int views;
  final String category;
  final double rating;

  EducationVideoModel({
    required this.id,
    required this.title,
    required this.channel,
    required this.thumbnail,
    required this.videoUrl,
    required this.duration,
    required this.views,
    required this.category,
    required this.rating,
  });

  factory EducationVideoModel.fromJson(Map<String, dynamic> json) {
    return EducationVideoModel(
      id: json['id'],
      title: json['title'],
      channel: json['channel'],
      thumbnail: json['thumbnail'],
      videoUrl: json['videoUrl'],
      duration: json['duration'],
      views: json['views'] ?? 0,
      category: json['category'],
      rating: (json['rating'] as num).toDouble(),
    );
  }
}

class CourseModel {
  final String id;
  final String title;
  final String instructor;
  final String thumbnail;
  final String description;
  final String category;
  final int enrollmentCount;
  final double rating;
  final int durationHours;
  final int totalLessons;

  CourseModel({
    required this.id,
    required this.title,
    required this.instructor,
    required this.thumbnail,
    required this.description,
    required this.category,
    required this.enrollmentCount,
    required this.rating,
    required this.durationHours,
    required this.totalLessons,
  });

  factory CourseModel.fromJson(Map<String, dynamic> json) {
    return CourseModel(
      id: json['id'],
      title: json['title'],
      instructor: json['instructor'],
      thumbnail: json['thumbnail'],
      description: json['description'],
      category: json['category'],
      enrollmentCount: json['enrollmentCount'] ?? 0,
      rating: (json['rating'] as num).toDouble(),
      durationHours: json['durationHours'] ?? 0,
      totalLessons: json['totalLessons'] ?? 0,
    );
  }
}

class CourseEnrollmentModel {
  final String id;
  final String courseId;
  final String title;
  final double progress;
  final int completedLessons;
  final bool isCompleted;
  final String thumbnail;
  final String instructor;

  CourseEnrollmentModel({
    required this.id,
    required this.courseId,
    required this.title,
    required this.progress,
    required this.completedLessons,
    required this.isCompleted,
    required this.thumbnail,
    required this.instructor,
  });

  factory CourseEnrollmentModel.fromJson(Map<String, dynamic> json) {
    return CourseEnrollmentModel(
      id: json['id'],
      courseId: json['course']['id'],
      title: json['course']['title'],
      progress: (json['progress'] as num).toDouble(),
      completedLessons: json['completedLessons'] ?? 0,
      isCompleted: json['isCompleted'] ?? false,
      thumbnail: json['course']['thumbnail'],
      instructor: json['course']['instructor'],
    );
  }
}
```

### 3. Home Feed (Posts, Users, Stories)

#### Before (Mock Data)
```dart
// lib/presentation/home_feed/home_feed.dart
import 'package:ispilo_main/presentation/home_feed/mock_data.dart';

class HomeFeedState extends State<HomeFeed> {
  @override
  void initState() {
    super.initState();
    final posts = kPosts;
    final users = kUsers;
    final stories = kStories;
  }
}
```

#### After (API Calls)
```dart
// lib/presentation/home_feed/home_feed.dart
import 'package:ispilo_main/core/services/api_service.dart';
import 'package:ispilo_main/model/social.dart';

class HomeFeedState extends State<HomeFeed> {
  List<PostModel> _posts = [];
  List<UserModel> _users = [];
  List<StoryModel> _stories = [];
  bool _isLoading = false;
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    _fetchFeed();
    _fetchUsers();
  }

  Future<void> _fetchFeed({int page = 0}) async {
    setState(() => _isLoading = true);
    try {
      final response = await ApiService.get(
        '/posts/feed?page=$page&size=20',
      );
      final posts = (response['content'] as List)
          .map((json) => PostModel.fromJson(json))
          .toList();
      
      setState(() => _posts = posts);
    } catch (e) {
      print('Error fetching feed: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _fetchUsers() async {
    try {
      final response = await ApiService.get('/users/discover?page=0&size=20');
      final users = (response['content'] as List)
          .map((json) => UserModel.fromJson(json))
          .toList();
      
      setState(() => _users = users);
    } catch (e) {
      print('Error fetching users: $e');
    }
  }

  Future<void> _likePost(String postId) async {
    try {
      await ApiService.post('/posts/$postId/like', {});
      // Update UI - increment likes count
      _fetchFeed();
    } catch (e) {
      print('Error liking post: $e');
    }
  }

  Future<void> _commentOnPost(String postId, String comment) async {
    try {
      await ApiService.post('/posts/$postId/comments', {
        'content': comment,
      });
      _fetchFeed();
    } catch (e) {
      print('Error commenting: $e');
    }
  }
}
```

#### Social Models
```dart
// lib/model/social.dart

class PostModel {
  final String id;
  final String userId;
  final String username;
  final String userAvatar;
  final String content;
  final String? imageUrl;
  final List<String> images;
  final int likesCount;
  final int commentsCount;
  final int viewCount;
  final bool isLiked;
  final bool isSaved;
  final bool isSponsored;
  final bool hasVerification;
  final DateTime createdAt;

  PostModel({
    required this.id,
    required this.userId,
    required this.username,
    required this.userAvatar,
    required this.content,
    this.imageUrl,
    required this.images,
    required this.likesCount,
    required this.commentsCount,
    required this.viewCount,
    required this.isLiked,
    required this.isSaved,
    required this.isSponsored,
    required this.hasVerification,
    required this.createdAt,
  });

  factory PostModel.fromJson(Map<String, dynamic> json) {
    return PostModel(
      id: json['id'],
      userId: json['user']['id'],
      username: json['user']['name'],
      userAvatar: json['user']['avatar'],
      content: json['content'],
      imageUrl: json['imageUrl'],
      images: List<String>.from(json['mediaUrls'] ?? []),
      likesCount: json['likesCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
      viewCount: json['viewCount'] ?? 0,
      isLiked: json['isLiked'] ?? false,
      isSaved: json['isSaved'] ?? false,
      isSponsored: json['isSponsored'] ?? false,
      hasVerification: json['hasVerification'] ?? false,
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class UserModel {
  final String id;
  final String username;
  final String name;
  final String avatar;
  final String? bio;
  final bool isVerified;
  final bool isOnline;

  UserModel({
    required this.id,
    required this.username,
    required this.name,
    required this.avatar,
    this.bio,
    required this.isVerified,
    required this.isOnline,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'],
      username: json['email'],
      name: json['name'],
      avatar: json['avatar'] ?? '',
      bio: json['bio'],
      isVerified: json['isVerified'] ?? false,
      isOnline: json['isOnline'] ?? false,
    );
  }
}

class StoryModel {
  final String id;
  final String userId;
  final String username;
  final String avatar;
  final bool isViewed;
  final bool isOwn;
  final DateTime createdAt;

  StoryModel({
    required this.id,
    required this.userId,
    required this.username,
    required this.avatar,
    required this.isViewed,
    required this.isOwn,
    required this.createdAt,
  });

  factory StoryModel.fromJson(Map<String, dynamic> json) {
    return StoryModel(
      id: json['id'],
      userId: json['userId'],
      username: json['username'],
      avatar: json['avatar'],
      isViewed: json['isViewed'] ?? false,
      isOwn: json['isOwn'] ?? false,
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class CommentModel {
  final String id;
  final String postId;
  final String userId;
  final String username;
  final String userAvatar;
  final String content;
  final int likesCount;
  final DateTime createdAt;

  CommentModel({
    required this.id,
    required this.postId,
    required this.userId,
    required this.username,
    required this.userAvatar,
    required this.content,
    required this.likesCount,
    required this.createdAt,
  });

  factory CommentModel.fromJson(Map<String, dynamic> json) {
    return CommentModel(
      id: json['id'],
      postId: json['postId'],
      userId: json['user']['id'],
      username: json['user']['name'],
      userAvatar: json['user']['avatar'],
      content: json['content'],
      likesCount: json['likesCount'] ?? 0,
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
```

## 4. Messaging Integration

Replace mock conversation data with API calls:

```dart
// lib/presentation/messages/messages.dart
import 'package:ispilo_main/core/services/websocket_service.dart';

class MessagesState extends State<Messages> {
  late WebSocketService _wsService;
  List<ConversationInfo> _conversations = [];

  @override
  void initState() {
    super.initState();
    _fetchConversations();
  }

  Future<void> _fetchConversations() async {
    try {
      _conversations = await ConversationService.getConversations(
        page: 0,
        size: 10,
      );
      setState(() {});
    } catch (e) {
      print('Error fetching conversations: $e');
    }
  }

  Future<void> _initializeWebSocket(String conversationId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('auth_token') ?? '';
    final userId = prefs.getString('user_id') ?? '';
    final encKey = prefs.getString('conv_key_$conversationId') ?? '';

    _wsService = WebSocketService();
    await _wsService.initialize(
      conversationId: conversationId,
      userId: userId,
      encryptionKey: encKey,
      authToken: token,
    );
  }
}
```

## Migration Checklist

### Phase 1: Authentication & Users
- [ ] Implement login/register with API
- [ ] Store JWT tokens securely
- [ ] Replace user mock data with API calls
- [ ] Implement user profile fetching

### Phase 2: Marketplace
- [ ] Replace marketplace_data.dart with product API calls
- [ ] Implement product search API integration
- [ ] Add category filtering
- [ ] Implement product detail view with API

### Phase 3: Education Hub
- [ ] Replace mock_education_data.dart with education API calls
- [ ] Implement video listing and search
- [ ] Implement course browsing and enrollment
- [ ] Add progress tracking

### Phase 4: Social Feed
- [ ] Replace mock_data.dart with social API calls
- [ ] Implement post fetching and pagination
- [ ] Add like/comment functionality
- [ ] Implement user discovery

### Phase 5: Real-Time Messaging
- [ ] Setup WebSocket connections
- [ ] Implement message sending with encryption
- [ ] Add typing indicators
- [ ] Implement read receipts

## Common Patterns

### Pagination Pattern
```dart
Future<List<T>> _fetchPage({required int page}) async {
  final response = await ApiService.get('/endpoint?page=$page&size=20');
  return (response['content'] as List)
      .map((json) => YourModel.fromJson(json))
      .toList();
}
```

### Error Handling Pattern
```dart
try {
  // API call
} on ApiException catch (e) {
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text('Error: ${e.message}')),
  );
} catch (e) {
  print('Unexpected error: $e');
}
```

### Loading State Pattern
```dart
if (_isLoading) {
  return const Center(child: CircularProgressIndicator());
}
if (_items.isEmpty) {
  return const Center(child: Text('No items found'));
}
// Display _items
```

## Testing API Integration

Use Postman or curl to test endpoints before integrating:

```bash
# Get products
curl http://localhost:8080/api/products?page=0&size=20 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Search products
curl http://localhost:8080/api/products/search?keyword=switch \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get trending videos
curl http://localhost:8080/api/education/videos/trending
```

## Deployment Notes

- Update `ApiService.baseUrl` to production URL before deploying
- Ensure all sensitive data is stored in `SharedPreferences` securely
- Test all API integrations thoroughly
- Monitor API response times and errors
- Implement proper logging for debugging

---

**Last Updated**: January 16, 2026
**Status**: In Progress
