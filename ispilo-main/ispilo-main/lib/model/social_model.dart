/// Post model for social feed
class PostModel {
  final String id;
  final String userId;
  final String username;
  final String userAvatar;
  final bool avatarPublic;
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
  final List<String>? ctaButtons;

  PostModel({
    required this.id,
    required this.userId,
    required this.username,
    required this.userAvatar,
    required this.avatarPublic,
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
    this.ctaButtons,
  });

  factory PostModel.fromJson(Map<String, dynamic> json) {
    return PostModel(
      id: json['id'] as String,
      userId: json['user']['id'] as String? ?? '',
      username: json['user']['name'] as String? ?? 'Unknown',
      userAvatar: json['user']['avatar'] as String? ?? '',
      avatarPublic: json['user']['avatarPublic'] as bool? ?? true,
      content: json['content'] as String? ?? '',
      imageUrl: json['imageUrl'] as String?,
      images: List<String>.from(json['mediaUrls'] as List? ?? []),
      likesCount: json['likesCount'] as int? ?? 0,
      commentsCount: json['commentsCount'] as int? ?? 0,
      viewCount: json['viewCount'] as int? ?? 0,
      isLiked: json['isLiked'] as bool? ?? false,
      isSaved: json['isSaved'] as bool? ?? false,
      isSponsored: json['isSponsored'] as bool? ?? false,
      hasVerification: json['hasVerification'] as bool? ?? false,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : DateTime.now(),
      ctaButtons: json['ctaButtons'] != null
          ? List<String>.from(json['ctaButtons'] as List)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'username': username,
      'userAvatar': userAvatar,
      'avatarPublic': avatarPublic,
      'content': content,
      'imageUrl': imageUrl,
      'mediaUrls': images,
      'likesCount': likesCount,
      'commentsCount': commentsCount,
      'viewCount': viewCount,
      'isLiked': isLiked,
      'isSaved': isSaved,
      'isSponsored': isSponsored,
      'hasVerification': hasVerification,
      'createdAt': createdAt.toIso8601String(),
      'ctaButtons': ctaButtons,
    };
  }

  /// Create a copy with modified fields
  PostModel copyWith({
    String? id,
    String? userId,
    String? username,
    String? userAvatar,
    bool? avatarPublic,
    String? content,
    String? imageUrl,
    List<String>? images,
    int? likesCount,
    int? commentsCount,
    int? viewCount,
    bool? isLiked,
    bool? isSaved,
    bool? isSponsored,
    bool? hasVerification,
    DateTime? createdAt,
    List<String>? ctaButtons,
  }) {
    return PostModel(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      username: username ?? this.username,
      userAvatar: userAvatar ?? this.userAvatar,
      avatarPublic: avatarPublic ?? this.avatarPublic,
      content: content ?? this.content,
      imageUrl: imageUrl ?? this.imageUrl,
      images: images ?? this.images,
      likesCount: likesCount ?? this.likesCount,
      commentsCount: commentsCount ?? this.commentsCount,
      viewCount: viewCount ?? this.viewCount,
      isLiked: isLiked ?? this.isLiked,
      isSaved: isSaved ?? this.isSaved,
      isSponsored: isSponsored ?? this.isSponsored,
      hasVerification: hasVerification ?? this.hasVerification,
      createdAt: createdAt ?? this.createdAt,
      ctaButtons: ctaButtons ?? this.ctaButtons,
    );
  }
}

/// Comment model
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
      id: json['id'] as String,
      postId: json['postId'] as String,
      userId: json['user']['id'] as String? ?? '',
      username: json['user']['name'] as String? ?? 'Unknown',
      userAvatar: json['user']['avatar'] as String? ?? '',
      content: json['content'] as String? ?? '',
      likesCount: json['likesCount'] as int? ?? 0,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'postId': postId,
      'userId': userId,
      'username': username,
      'userAvatar': userAvatar,
      'content': content,
      'likesCount': likesCount,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}

/// User model for social feed
class UserModel {
  final String id;
  final String username;
  final String name;
  final String avatar;
  final String? bio;
  final bool isVerified;
  final bool isOnline;
  final bool isPremium;
  final bool avatarPublic;
  final String? company;
  final String? town;
  final String? quote;
  final String? coverImage;
  final DateTime? createdAt;

  UserModel({
    required this.id,
    required this.username,
    required this.name,
    required this.avatar,
    this.bio,
    required this.isVerified,
    required this.isOnline,
    required this.isPremium,
    required this.avatarPublic,
    this.company,
    this.town,
    this.quote,
    this.coverImage,
    this.createdAt,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String,
      username: json['email'] as String? ?? '',
      name: json['name'] as String? ?? '',
      avatar: json['avatar'] as String? ?? '',
      bio: json['bio'] as String?,
      isVerified: json['isVerified'] as bool? ?? false,
      isOnline: json['isOnline'] as bool? ?? false,
      isPremium: json['isPremium'] as bool? ?? false,
      avatarPublic: json['avatarPublic'] as bool? ?? true,
      company: json['company'] as String?,
      town: json['town'] as String?,
      quote: json['quote'] as String?,
      coverImage: json['coverImage'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'] as String) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'name': name,
      'avatar': avatar,
      'bio': bio,
      'isVerified': isVerified,
      'isOnline': isOnline,
      'isPremium': isPremium,
      'avatarPublic': avatarPublic,
      'company': company,
      'town': town,
      'quote': quote,
      'coverImage': coverImage,
      'createdAt': createdAt?.toIso8601String(),
    };
  }
}

/// Story model
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
      id: json['id'] as String,
      userId: json['userId'] as String,
      username: json['username'] as String,
      avatar: json['avatar'] as String,
      isViewed: json['isViewed'] as bool? ?? false,
      isOwn: json['isOwn'] as bool? ?? false,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'username': username,
      'avatar': avatar,
      'isViewed': isViewed,
      'isOwn': isOwn,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}
