import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sizer/sizer.dart';

import '../../../widgets/profile_avatar.dart';

class FriendsToFollowWidget extends StatefulWidget {
  final List<Map<String, dynamic>> suggestions;

  const FriendsToFollowWidget({
    super.key,
    required this.suggestions,
  });

  @override
  State<FriendsToFollowWidget> createState() => _FriendsToFollowWidgetState();
}

class _FriendsToFollowWidgetState extends State<FriendsToFollowWidget> {
  final Set<int> followedUsers = {};

  void _handleFollow(int userId) {
    HapticFeedback.lightImpact();
    setState(() {
      if (followedUsers.contains(userId)) {
        followedUsers.remove(userId);
      } else {
        followedUsers.add(userId);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: EdgeInsets.symmetric(vertical: 2.h),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Friends to Follow',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                TextButton(
                  onPressed: () {
                    // Navigate to full suggestions page
                  },
                  child: Text(
                    'See All',
                    style: theme.textTheme.labelMedium?.copyWith(
                      color: theme.colorScheme.primary,
                    ),
                  ),
                ),
              ],
            ),
          ),
          SizedBox(height: 1.h),
          SizedBox(
            height: 25.h,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: EdgeInsets.symmetric(horizontal: 0),
              itemCount: widget.suggestions.length,
              itemBuilder: (context, index) {
                final suggestion = widget.suggestions[index];
                final userId = suggestion['id'] as int;
                final isFollowed = followedUsers.contains(userId);

                return Container(
                  width: 35.w,
                  margin: EdgeInsets.only(right: 3.w),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surface,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: theme.colorScheme.outline.withValues(alpha: 0.2),
                    ),
                  ),
                  child: Padding(
                    padding: EdgeInsets.all(3.w),
                    child: Column(
                      children: [
                        ClipOval(
                          child: ProfileAvatar(
                            imageUrl: suggestion['avatar'] as String? ?? '',
                            size: 15.w.toDouble(),
                            isOnline: suggestion['isOnline'] as bool? ?? false,
                          ),
                        ),
                        SizedBox(height: 1.h),
                        Text(
                          suggestion['name'] as String? ?? '',
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        SizedBox(height: 0.5.h),
                        Text(
                          suggestion['role'] as String? ?? '',
                          style: theme.textTheme.bodySmall,
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        SizedBox(height: 1.h),
                        Text(
                          '${suggestion['mutualFriends'] as int? ?? 0} mutual friends',
                          style: theme.textTheme.labelSmall?.copyWith(
                            color: theme.colorScheme.onSurface
                                .withValues(alpha: 0.6),
                          ),
                          textAlign: TextAlign.center,
                        ),
                        SizedBox(height: 1.h),
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: () => _handleFollow(userId),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: isFollowed
                                  ? theme.colorScheme.surface
                                  : theme.colorScheme.primary,
                              foregroundColor: isFollowed
                                  ? theme.colorScheme.onSurface
                                  : theme.colorScheme.onPrimary,
                              side: isFollowed
                                  ? BorderSide(
                                      color: theme.colorScheme.outline
                                          .withValues(alpha: 0.3))
                                  : null,
                              padding: EdgeInsets.symmetric(vertical: 1.h),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                            child: Text(
                              isFollowed ? 'Following' : 'Follow',
                              style: theme.textTheme.labelMedium?.copyWith(
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
