import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sizer/sizer.dart';

import '../../core/app_export.dart';
import '../../widgets/custom_app_bar.dart';
import '../../widgets/custom_bottom_bar.dart';
import '../marketplace/widgets/product_card_widget.dart';
import '../../model/social_model.dart';
import '../../model/repository/social_repository.dart';

class HomeFeed extends StatefulWidget {
  const HomeFeed({super.key});

  @override
  State<HomeFeed> createState() => _HomeFeedState();
}

class _HomeFeedState extends State<HomeFeed> with TickerProviderStateMixin {
  final ScrollController _scrollController = ScrollController();

  bool _isLoading = false;
  bool _hasMorePosts = true;
  int _currentBottomIndex = 0;
  int _page = 0;
  final int _pageSize = 20;
  final int _adInterval = 6; // Insert ad after every 6 posts
  bool _isPremium = false; // Premium users see no ads

  // Feed data
  final List<PostModel> _posts = [];
  final Set<String> _seenPostIds = <String>{};

  // Suggestions (users to follow)
  List<UserModel> _friendSuggestions = [];

  // Ads (can be fetched from API later). Each ad contains: title, image, advertiserId, cta
  final List<Map<String, dynamic>> _adsPool = [
    {
      'id': 'ad-1',
      'title': 'Upgrade your network with FiberPro',
      'imageUrl': 'https://images.unsplash.com/photo-1518770660439-4636190af475',
      'advertiserId': 'seller-123',
      'cta': 'Learn more',
    },
    {
      'id': 'ad-2',
      'title': 'Get 20% off on Cisco CCNA Course',
      'imageUrl': 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31',
      'advertiserId': 'instructor-456',
      'cta': 'Learn more',
    },
  ];

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    _initUserAndLoad();
  }

  Future<void> _initUserAndLoad() async {
    try {
      final currentUser = await UserRepository.getCurrentUser();
      _isPremium = currentUser.isPremium; // backend-driven flag
    } catch (_) {
      _isPremium = false;
    }
    await _loadInitialPosts();
    await _loadSuggestions();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      if (!_isLoading && _hasMorePosts) {
        _loadMorePosts();
      }
    }
  }

  Future<void> _loadInitialPosts() async {
    setState(() => _isLoading = true);
    try {
      final posts = await PostRepository.getFeed(page: 0, size: _pageSize);
      _appendUniquePosts(posts);
      setState(() {
        _page = 1;
        _hasMorePosts = posts.length >= _pageSize;
      });
    } catch (e) {
      debugPrint('Error loading posts: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to load feed: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _loadMorePosts() async {
    if (_isLoading) return;
    setState(() => _isLoading = true);

    try {
      final newPosts = await PostRepository.getFeed(page: _page, size: _pageSize);
      final beforeAppendCount = _posts.length;
      _appendUniquePosts(newPosts);
      setState(() {
        _page++;
        if (newPosts.length < _pageSize || _posts.length == beforeAppendCount) {
          // If fewer than page size returned or no new unique posts, assume end
          _hasMorePosts = false;
        }
      });
    } catch (e) {
      debugPrint('Error loading more posts: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to load more posts: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _appendUniquePosts(List<PostModel> posts) {
    for (final post in posts) {
      if (_seenPostIds.add(post.id)) {
        _posts.add(post);
      }
    }
  }

  Future<void> _loadSuggestions() async {
    try {
      _friendSuggestions = await UserRepository.getUserSuggestions(page: 0, size: 10);
      if (mounted) setState(() {});
    } catch (e) {
      debugPrint('Error loading suggestions: $e');
    }
  }

  Future<void> _refreshFeed() async {
    HapticFeedback.lightImpact();
    setState(() {
      _isLoading = true;
      _hasMorePosts = true;
      _page = 0;
      _posts.clear();
      _seenPostIds.clear();
    });
    await _loadInitialPosts();
    await _loadSuggestions();
  }

  // Interactions
  Future<void> _likePost(PostModel post) async {
    try {
      await PostRepository.likePost(post.id);
      final idx = _posts.indexWhere((p) => p.id == post.id);
      if (idx != -1) {
        _posts[idx] = _posts[idx].copyWith(
          isLiked: true,
          likesCount: _posts[idx].likesCount + 1,
        );
        setState(() {});
      }
    } catch (e) {
      debugPrint('Error liking post: $e');
    }
  }

  Future<void> _unlikePost(PostModel post) async {
    try {
      await PostRepository.unlikePost(post.id);
      final idx = _posts.indexWhere((p) => p.id == post.id);
      if (idx != -1) {
        _posts[idx] = _posts[idx].copyWith(
          isLiked: false,
          likesCount: max(0, _posts[idx].likesCount - 1),
        );
        setState(() {});
      }
    } catch (e) {
      debugPrint('Error unliking post: $e');
    }
  }

  Future<void> _savePost(PostModel post) async {
    try {
      await PostRepository.savePost(post.id);
      final idx = _posts.indexWhere((p) => p.id == post.id);
      if (idx != -1) {
        _posts[idx] = _posts[idx].copyWith(isSaved: true);
        setState(() {});
      }
    } catch (e) {
      debugPrint('Error saving post: $e');
    }
  }

  Future<void> _unsavePost(PostModel post) async {
    try {
      await PostRepository.unsavePost(post.id);
      final idx = _posts.indexWhere((p) => p.id == post.id);
      if (idx != -1) {
        _posts[idx] = _posts[idx].copyWith(isSaved: false);
        setState(() {});
      }
    } catch (e) {
      debugPrint('Error unsaving post: $e');
    }
  }

  void _openComments(PostModel post) {
    Navigator.pushNamed(context, '/post-comments', arguments: post.toJson());
  }

  void _sharePost(PostModel post) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Shared: ${post.content.substring(0, post.content.length > 20 ? 20 : post.content.length)}...')),
    );
  }

  // Ads rendering
  Widget _buildAdCard(ColorScheme colorScheme, Map<String, dynamic> ad) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 4.w, vertical: 1.h),
      padding: EdgeInsets.all(3.w),
      decoration: BoxDecoration(
        color: colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: colorScheme.outline.withValues(alpha: 0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (ad['imageUrl'] != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.network(
                ad['imageUrl'] as String,
                height: 18.h,
                width: double.infinity,
                fit: BoxFit.cover,
              ),
            ),
          SizedBox(height: 1.h),
          Text(
            ad['title'] as String? ?? 'Sponsored',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          SizedBox(height: 0.5.h),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Sponsored',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurface.withValues(alpha: 0.6),
                    ),
              ),
              TextButton(
                onPressed: () {
                  // Navigate to advertiser profile/page
                  Navigator.pushNamed(
                    context,
                    '/profile',
                    arguments: {'userId': ad['advertiserId']},
                  );
                },
                child: const Text('Learn more'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.surface,
      appBar: const CustomAppBar(title: 'Home'),
      body: RefreshIndicator(
        onRefresh: _refreshFeed,
        child: CustomScrollView(
          controller: _scrollController,
          slivers: [
            // Suggestions to follow
            SliverToBoxAdapter(
              child: _friendSuggestions.isEmpty
                  ? const SizedBox.shrink()
                  : Padding(
                      padding: EdgeInsets.symmetric(horizontal: 4.w, vertical: 1.h),
                      child: _buildSuggestionsRow(colorScheme),
                    ),
            ),

            // Feed posts with ad slots
            SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  // Interleave ads at intervals, skip if premium
                  if (!_isPremium && index > 0 && index % _adInterval == 0) {
                    final ad = _adsPool[(index ~/ _adInterval - 1) % _adsPool.length];
                    return _buildAdCard(colorScheme, ad);
                  }

                  final feedIndex = _isPremium
                      ? index
                      : index - (index ~/ _adInterval); // adjust index if ads are inserted

                  if (feedIndex >= _posts.length) {
                    return _buildLoading(colorScheme);
                  }

                  final post = _posts[feedIndex];
                  return _buildPostCard(colorScheme, post);
                },
                childCount: _posts.length + (_isLoading ? 1 : 0) + (_isPremium ? 0 : max(0, _posts.length ~/ _adInterval)),
              ),
            ),

            // End or loading indicator
            SliverToBoxAdapter(
              child: Padding(
                padding: EdgeInsets.all(4.w),
                child: Center(
                  child: _isLoading
                      ? const CircularProgressIndicator()
                      : (!_hasMorePosts && _posts.isNotEmpty)
                          ? Text(
                              'No more posts',
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: colorScheme.onSurface.withValues(alpha: 0.6),
                              ),
                            )
                          : const SizedBox.shrink(),
                ),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: CustomBottomBar(
        currentIndex: _currentBottomIndex,
        variant: CustomBottomBarVariant.standard,
        onTap: (i) {
          setState(() => _currentBottomIndex = i);
          // ...existing code...
        },
      ),
    );
  }

  Widget _buildLoading(ColorScheme colorScheme) {
    return Container(
      padding: EdgeInsets.all(4.w),
      child: const Center(child: CircularProgressIndicator()),
    );
  }

  Widget _buildSuggestionsRow(ColorScheme colorScheme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('Friends to follow', style: Theme.of(context).textTheme.titleMedium),
            TextButton(
              onPressed: () => Navigator.pushNamed(context, '/discover'),
              child: const Text('See all'),
            ),
          ],
        ),
        SizedBox(
          height: 12.h,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: _friendSuggestions.length,
            itemBuilder: (context, index) {
              final user = _friendSuggestions[index];
              return GestureDetector(
                onTap: () => Navigator.pushNamed(context, '/profile', arguments: {'userId': user.id}),
                child: Container(
                  width: 30.w,
                  margin: EdgeInsets.only(right: 3.w),
                  padding: EdgeInsets.all(2.w),
                  decoration: BoxDecoration(
                    color: colorScheme.surface,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: colorScheme.outline.withValues(alpha: 0.2)),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      CircleAvatar(radius: 20, backgroundImage: user.avatar.isNotEmpty ? NetworkImage(user.avatar) : null),
                      SizedBox(height: 1.h),
                      Text(user.name, maxLines: 1, overflow: TextOverflow.ellipsis),
                      TextButton(
                        onPressed: () async {
                          try {
                            await UserRepository.followUser(user.id);
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Following ${user.name}')),
                            );
                          } catch (e) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Failed to follow: $e')),
                            );
                          }
                        },
                        child: const Text('Follow'),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildPostCard(ColorScheme colorScheme, PostModel post) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 4.w, vertical: 1.h),
      padding: EdgeInsets.all(3.w),
      decoration: BoxDecoration(
        color: colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: colorScheme.outline.withValues(alpha: 0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              // Avatar with privacy awareness
              if (post.avatarPublic && post.userAvatar.isNotEmpty)
                CircleAvatar(radius: 18, backgroundImage: NetworkImage(post.userAvatar))
              else
                CircleAvatar(
                  radius: 18,
                  backgroundColor: colorScheme.outline.withValues(alpha: 0.2),
                  child: Icon(Icons.person, color: colorScheme.onSurface.withValues(alpha: 0.6)),
                ),
              SizedBox(width: 2.w),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(children: [
                      Text(post.username, style: Theme.of(context).textTheme.titleSmall),
                      if (_isPremium) ...[
                        SizedBox(width: 1.w),
                        Icon(Icons.check_circle, color: colorScheme.primary, size: 16),
                      ],
                    ]),
                    Text(
                      '${post.createdAt}',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurface.withValues(alpha: 0.6),
                          ),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.more_horiz),
                onPressed: () {
                  // Post actions: save, share, report, remove repeated posts
                  showModalBottomSheet(
                    context: context,
                    builder: (context) => SafeArea(
                      child: Wrap(
                        children: [
                          ListTile(
                            leading: const Icon(Icons.bookmark_add_outlined),
                            title: Text(post.isSaved ? 'Unsave' : 'Save'),
                            onTap: () {
                              Navigator.pop(context);
                              post.isSaved ? _unsavePost(post) : _savePost(post);
                            },
                          ),
                          ListTile(
                            leading: const Icon(Icons.share_outlined),
                            title: const Text('Share'),
                            onTap: () {
                              Navigator.pop(context);
                              _sharePost(post);
                            },
                          ),
                          ListTile(
                            leading: const Icon(Icons.flag_outlined),
                            title: const Text('Report'),
                            onTap: () => Navigator.pop(context),
                          ),
                          ListTile(
                            leading: const Icon(Icons.filter_alt_off_outlined),
                            title: const Text('Remove repeated posts'),
                            subtitle: const Text('Clean up any duplicates in your feed'),
                            onTap: () {
                              Navigator.pop(context);
                              _removeRepeatedPosts();
                            },
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ],
          ),
          SizedBox(height: 1.h),
          Text(post.content),
          if (post.imageUrl != null && post.imageUrl!.isNotEmpty) ...[
            SizedBox(height: 1.h),
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.network(post.imageUrl!, fit: BoxFit.cover),
            ),
          ],
          SizedBox(height: 1.h),
          Row(
            children: [
              IconButton(
                icon: Icon(post.isLiked ? Icons.favorite : Icons.favorite_border, color: post.isLiked ? Colors.red : null),
                onPressed: () => post.isLiked ? _unlikePost(post) : _likePost(post),
              ),
              Text('${post.likesCount}'),
              SizedBox(width: 4.w),
              IconButton(
                icon: const Icon(Icons.comment_outlined),
                onPressed: () => _openComments(post),
              ),
              Text('${post.commentsCount}'),
              const Spacer(),
              IconButton(
                icon: const Icon(Icons.bookmark_border),
                onPressed: () => post.isSaved ? _unsavePost(post) : _savePost(post),
              ),
              IconButton(
                icon: const Icon(Icons.share_outlined),
                onPressed: () => _sharePost(post),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _removeRepeatedPosts() {
    final before = _posts.length;
    final unique = <String>{};
    _posts.removeWhere((p) => !unique.add(p.id));
    _seenPostIds
      ..clear()
      ..addAll(_posts.map((p) => p.id));
    final removed = before - _posts.length;
    if (mounted) {
      setState(() {});
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(removed > 0 ? 'Removed $removed repeated post(s)' : 'No repeated posts found')),
      );
    }
  }
}
