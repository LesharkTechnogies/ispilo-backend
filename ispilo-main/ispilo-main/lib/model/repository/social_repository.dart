import 'package:ispilo_main/core/services/api_service.dart';
import 'social_model.dart';

/// Repository for social feed API calls
class PostRepository {
  static const String _baseEndpoint = '/posts';

  /// Get feed posts with pagination
  static Future<List<PostModel>> getFeed({
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get('$_baseEndpoint/feed?page=$page&size=$size');
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => PostModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch posts: $e');
    }
  }

  /// Get post by ID
  static Future<PostModel> getPostById(String postId) async {
    try {
      final response = await ApiService.get('$_baseEndpoint/$postId');
      return PostModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to fetch post: $e');
    }
  }

  /// Get posts by user
  static Future<List<PostModel>> getPostsByUser(
    String userId, {
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/user/$userId?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => PostModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch user posts: $e');
    }
  }

  /// Create a new post
  static Future<PostModel> createPost({
    required String content,
    List<String>? images,
    String? imageUrl,
    List<String>? ctaButtons,
  }) async {
    try {
      final payload = {
        'content': content,
        'mediaUrls': images ?? [],
        'imageUrl': imageUrl,
        'ctaButtons': ctaButtons,
      };

      final response = await ApiService.post(_baseEndpoint, payload);
      return PostModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to create post: $e');
    }
  }

  /// Update post
  static Future<PostModel> updatePost({
    required String postId,
    required String content,
    List<String>? images,
  }) async {
    try {
      final payload = {
        'content': content,
        'mediaUrls': images,
      };

      final response = await ApiService.put('$_baseEndpoint/$postId', payload);
      return PostModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to update post: $e');
    }
  }

  /// Delete post
  static Future<void> deletePost(String postId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$postId');
    } catch (e) {
      throw Exception('Failed to delete post: $e');
    }
  }

  /// Like post
  static Future<void> likePost(String postId) async {
    try {
      await ApiService.post('$_baseEndpoint/$postId/like', {});
    } catch (e) {
      throw Exception('Failed to like post: $e');
    }
  }

  /// Unlike post
  static Future<void> unlikePost(String postId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$postId/like');
    } catch (e) {
      throw Exception('Failed to unlike post: $e');
    }
  }

  /// Save post
  static Future<void> savePost(String postId) async {
    try {
      await ApiService.post('$_baseEndpoint/$postId/save', {});
    } catch (e) {
      throw Exception('Failed to save post: $e');
    }
  }

  /// Unsave post
  static Future<void> unsavePost(String postId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$postId/save');
    } catch (e) {
      throw Exception('Failed to unsave post: $e');
    }
  }

  /// Get post comments
  static Future<List<CommentModel>> getComments({
    required String postId,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/$postId/comments?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => CommentModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch comments: $e');
    }
  }

  /// Add comment to post
  static Future<CommentModel> addComment({
    required String postId,
    required String content,
  }) async {
    try {
      final response = await ApiService.post(
        '$_baseEndpoint/$postId/comments',
        {'content': content},
      );
      return CommentModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to add comment: $e');
    }
  }

  /// Delete comment
  static Future<void> deleteComment(String commentId) async {
    try {
      await ApiService.delete('/comments/$commentId');
    } catch (e) {
      throw Exception('Failed to delete comment: $e');
    }
  }
}

/// Repository for user API calls
class UserRepository {
  static const String _baseEndpoint = '/users';

  /// Get current user profile
  static Future<UserModel> getCurrentUser() async {
    try {
      final response = await ApiService.get('$_baseEndpoint/me');
      return UserModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to fetch current user: $e');
    }
  }

  /// Get user by ID
  static Future<UserModel> getUserById(String userId) async {
    try {
      final response = await ApiService.get('$_baseEndpoint/$userId');
      return UserModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to fetch user: $e');
    }
  }

  /// Get users to follow (suggestions)
  static Future<List<UserModel>> getUserSuggestions({
    int page = 0,
    int size = 10,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/discover?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => UserModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return []; // Return empty list on error
    }
  }

  /// Follow user
  static Future<void> followUser(String userId) async {
    try {
      await ApiService.post('$_baseEndpoint/$userId/follow', {});
    } catch (e) {
      throw Exception('Failed to follow user: $e');
    }
  }

  /// Unfollow user
  static Future<void> unfollowUser(String userId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$userId/follow');
    } catch (e) {
      throw Exception('Failed to unfollow user: $e');
    }
  }

  /// Update user profile with selective fields (only non-null fields are sent)
  static Future<UserModel> updateProfile({
    String? name,
    String? bio,
    String? avatar,
    String? location,
    String? company,
    String? quote,
    bool? avatarPublic,
  }) async {
    try {
      final payload = <String, dynamic>{};
      if (name != null) payload['name'] = name;
      if (bio != null) payload['bio'] = bio;
      if (avatar != null) payload['avatar'] = avatar;
      if (location != null) payload['location'] = location;
      if (company != null) payload['company'] = company;
      if (quote != null) payload['quote'] = quote;
      if (avatarPublic != null) payload['avatarPublic'] = avatarPublic;

      final response = await ApiService.put('$_baseEndpoint/me', payload);
      return UserModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to update profile: $e');
    }
  }
}
