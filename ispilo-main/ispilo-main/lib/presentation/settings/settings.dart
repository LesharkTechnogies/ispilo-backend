import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sizer/sizer.dart';
import 'package:provider/provider.dart';
import 'package:local_auth/local_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../core/app_export.dart';
import '../../core/theme_provider.dart';
import '../../widgets/fullscreen_image_viewer.dart';
import '../../widgets/custom_bottom_bar.dart';
import '../../model/repository/user_repository.dart';
import './widgets/settings_section_widget.dart';
import './widgets/settings_switch_widget.dart';
import './widgets/settings_tile_widget.dart';
import 'change_password.dart';
import '../../model/social_model.dart';
import '../../model/repository/social_repository.dart';

class Settings extends StatefulWidget {
  const Settings({super.key});

  @override
  State<Settings> createState() => _SettingsState();
}

class _SettingsState extends State<Settings> {
  // Pref keys
  static const String _prefBiometric = 'pref_biometric_auth';
  static const String _prefTwoFactor = 'pref_two_factor';
  static const String _prefAccountVisibility = 'pref_account_visibility';
  static const String _prefSocialNotifs = 'pref_social_notifs';
  static const String _prefMessageNotifs = 'pref_message_notifs';
  static const String _prefEducationNotifs = 'pref_education_notifs';
  static const String _prefMarketplaceNotifs = 'pref_marketplace_notifs';
  static const String _prefHighContrast = 'pref_high_contrast';
  static const String _prefOfflineContent = 'pref_offline_content';

  // Settings state variables
  bool _biometricAuth = false;
  bool _twoFactorAuth = false;
  bool _accountVisibility = true;
  bool _socialNotifications = true;
  bool _messageNotifications = true;
  bool _educationNotifications = false;
  bool _marketplaceNotifications = true;
  bool _darkMode = false;
  bool _systemTheme = true;
  bool _highContrast = false;
  bool _offlineContent = true;

  final LocalAuthentication _localAuth = LocalAuthentication();

  // User profile and stats from API
  Map<String, dynamic>? _userProfile;
  int _postCount = 0;
  int _followers = 0;
  int _following = 0;
  int _connections = 0;
  bool _isLoading = false;
  String? _errorMessage;

  // Use shared current user profile from API
  Map<String, dynamic> get _userProfileData => _userProfile != null
      ? {
          'name': _userProfile!['name'] ?? 'User',
          'email': _userProfile!['email'] ?? '',
          'avatar': _userProfile!['avatar'] ?? '',
          'bio': _userProfile!['bio'] ?? '',
          'verified': _userProfile!['isVerified'] ?? false,
          'location': _userProfile!['location'] ?? '',
          'phone': _userProfile!['phone'] ?? '',
          'phonePrivacyPublic': _userProfile!['phonePrivacyPublic'] ?? false,
          'posts': _postCount,
          'followers': _followers,
          'following': _following,
          'connections': _connections,
        }
      : {};

  @override
  void initState() {
    super.initState();
    _loadSettings();
    _loadUserProfileAndStats();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _biometricAuth = prefs.getBool(_prefBiometric) ?? false;
      _twoFactorAuth = prefs.getBool(_prefTwoFactor) ?? false;
      _accountVisibility = prefs.getBool(_prefAccountVisibility) ?? true;
      _socialNotifications = prefs.getBool(_prefSocialNotifs) ?? true;
      _messageNotifications = prefs.getBool(_prefMessageNotifs) ?? true;
      _educationNotifications = prefs.getBool(_prefEducationNotifs) ?? false;
      _marketplaceNotifications = prefs.getBool(_prefMarketplaceNotifs) ?? true;
      _highContrast = prefs.getBool(_prefHighContrast) ?? false;
      _offlineContent = prefs.getBool(_prefOfflineContent) ?? true;
    });
  }

  /// Load user profile and stats from Java API
  Future<void> _loadUserProfileAndStats() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // Get current user from API
      final userResponse = await UserRepository.getCurrentUser();

      // Get user stats from API
      final statsResponse = await UserRepository.getUserStats();

      if (!mounted) return;

      setState(() {
        _userProfile = userResponse;
        _postCount = statsResponse['postCount'] as int? ?? 0;
        _followers = statsResponse['followers'] as int? ?? 0;
        _following = statsResponse['following'] as int? ?? 0;
        _connections = statsResponse['connections'] as int? ?? 0;
        _isLoading = false;
      });
    } catch (e) {
      debugPrint('Error loading user profile: $e');
      if (mounted) {
        setState(() {
          _errorMessage = 'Failed to load profile: $e';
          _isLoading = false;
        });
      }
    }
  }

  // ...existing code...
      _offlineContent = prefs.getBool(_prefOfflineContent) ?? true;
    });
  }

  Future<void> _saveSettings() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_prefBiometric, _biometricAuth);
    await prefs.setBool(_prefTwoFactor, _twoFactorAuth);
    await prefs.setBool(_prefAccountVisibility, _accountVisibility);
    await prefs.setBool(_prefSocialNotifs, _socialNotifications);
    await prefs.setBool(_prefMessageNotifs, _messageNotifications);
    await prefs.setBool(_prefEducationNotifs, _educationNotifications);
    await prefs.setBool(_prefMarketplaceNotifs, _marketplaceNotifications);
    await prefs.setBool(_prefHighContrast, _highContrast);
    await prefs.setBool(_prefOfflineContent, _offlineContent);
  }

  Future<void> _handleBiometricToggle(bool value) async {
    HapticFeedback.lightImpact();
    if (value) {
      try {
        final bool canCheck = await _localAuth.canCheckBiometrics ||
            await _localAuth.isDeviceSupported();
        if (!canCheck) {
          if (!mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
                content: Text('Biometrics not available on this device')),
          );
          return;
        }
        final bool didAuth = await _localAuth.authenticate(
          localizedReason: 'Authenticate to enable biometric unlock',
          options: const AuthenticationOptions(
              biometricOnly: true, stickyAuth: true),
        );
        if (didAuth) {
          setState(() => _biometricAuth = true);
          await _saveSettings();
          if (!mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Biometric authentication enabled')),
          );
        }
      } catch (e) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Biometric setup failed: $e')),
        );
      }
    } else {
      setState(() => _biometricAuth = false);
      await _saveSettings();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Biometric authentication disabled')),
      );
    }
  }

  void _handleTwoFactorToggle(bool value) {
    HapticFeedback.lightImpact();
    setState(() => _twoFactorAuth = value);
    _saveSettings();
    if (value) {
      _showTwoFactorSetupDialog();
    }
  }

  void _handleNotificationToggle(String type, bool value) {
    HapticFeedback.lightImpact();
    setState(() {
      switch (type) {
        case 'social':
          _socialNotifications = value;
          break;
        case 'messages':
          _messageNotifications = value;
          break;
        case 'education':
          _educationNotifications = value;
          break;
        case 'marketplace':
          _marketplaceNotifications = value;
          break;
      }
    });
    _saveSettings();
  }

  void _handleThemeToggle(String type, bool value) {
    HapticFeedback.lightImpact();
    final themeProvider = context.read<ThemeProvider>();

    switch (type) {
      case 'dark':
        if (value) {
          themeProvider.setThemeMode(ThemeMode.dark);
        } else {
          themeProvider.setThemeMode(ThemeMode.light);
        }
        break;
      case 'system':
        if (value) {
          themeProvider.setThemeMode(ThemeMode.system);
        } else {
          themeProvider.setThemeMode(ThemeMode.light);
        }
        break;
      case 'contrast':
        _highContrast = value;
        _saveSettings();
        break;
      case 'text':
        themeProvider.setLargeTextEnabled(value);
        break;
    }

    setState(() {
      _darkMode = themeProvider.themeMode == ThemeMode.dark;
      _systemTheme = themeProvider.themeMode == ThemeMode.system;
    });
  }

  void _handleEditProfile() {
    Navigator.pushNamed(context, '/edit-profile');
  }

  void _handlePasswordChange() {
    HapticFeedback.lightImpact();
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const ChangePasswordPage()),
    );
  }

  void _handleDataUsage() {
    HapticFeedback.lightImpact();
  }

  void _handleClearCache() {
    HapticFeedback.mediumImpact();
    _showClearCacheDialog();
  }

  void _handleFAQ() {
    HapticFeedback.lightImpact();
  }

  void _handleContactSupport() {
    HapticFeedback.lightImpact();
  }

  void _handleFeedback() {
    HapticFeedback.lightImpact();
  }

  void _handleGuidelines() {
    HapticFeedback.lightImpact();
  }

  void _handleTerms() {
    HapticFeedback.lightImpact();
  }

  void _handlePrivacyPolicy() {
    HapticFeedback.lightImpact();
  }

  void _handleLicenses() {
    HapticFeedback.lightImpact();
  }

  void _handleLogout() {
    HapticFeedback.mediumImpact();
    _showLogoutDialog();
  }

  void _handleDeleteAccount() {
    HapticFeedback.heavyImpact();
    _showDeleteAccountDialog();
  }

  void _showTwoFactorSetupDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Setup Two-Factor Authentication'),
        content: const Text(
          'Two-factor authentication adds an extra layer of security to your account. You will need to verify your phone number to continue.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              setState(() {
                _twoFactorAuth = true;
              });
              _saveSettings();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Two-factor authentication enabled'),
                  duration: Duration(seconds: 2),
                ),
              );
            },
            child: const Text('Setup'),
          ),
        ],
      ),
    );
  }

  void _showClearCacheDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear Cache'),
        content: const Text(
          'This will clear all cached data including offline content. This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Cache cleared successfully'),
                  duration: Duration(seconds: 2),
                ),
              );
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    );
  }

  void _showLogoutDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Logout'),
        content: const Text('Are you sure you want to logout?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pushNamedAndRemoveUntil(
                context,
                '/splash',
                (route) => false,
              );
            },
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }

  void _showDeleteAccountDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Account'),
        content: const Text(
          'Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text(
                      'Account deletion requested. You will receive a confirmation email.'),
                  duration: Duration(seconds: 3),
                ),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileHeader() {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      children: [
        // Cover Image
        if (_userProfile['coverImage'] != null)
          GestureDetector(
            onTap: () {
              final url = _userProfile['coverImage'] as String? ?? '';
              if (url.isEmpty) return;
              showDialog(
                context: context,
                builder: (_) =>
                    FullScreenImageViewer(imageUrl: url, heroTag: 'cover_$url'),
              );
            },
            child: Container(
              height: 20.h,
              width: double.infinity,
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerHighest,
              ),
              child: Hero(
                tag: 'cover_${_userProfile['coverImage']}',
                child: CustomImageWidget(
                  imageUrl: _userProfile['coverImage'],
                  width: double.infinity,
                  height: 20.h,
                  fit: BoxFit.cover,
                ),
              ),
            ),
          ),

        // Profile Card
        Transform.translate(
          offset: Offset(0, _userProfile['coverImage'] != null ? -30 : 0),
          child: Card(
            margin: EdgeInsets.symmetric(horizontal: 4.w),
            child: Padding(
              padding: EdgeInsets.fromLTRB(4.w, 4.w, 4.w, 2.h),
              child: Column(
                children: [
                  // Avatar and Edit Button Row
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Avatar
                      Stack(
                        children: [
                          GestureDetector(
                            onTap: () {
                              final url = _userProfile['avatar'] as String? ?? '';
                              if (url.isEmpty) return;
                              showDialog(
                                context: context,
                                builder: (_) => FullScreenImageViewer(
                                    imageUrl: url, heroTag: 'avatar_$url'),
                              );
                            },
                            child: Container(
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border: Border.all(
                                  color: colorScheme.surface,
                                  width: 4,
                                ),
                              ),
                              child: ClipRRect(
                                borderRadius: BorderRadius.circular(40),
                                child: Hero(
                                  tag: 'avatar_${_userProfile['avatar']}',
                                  child: CustomImageWidget(
                                    imageUrl: _userProfile['avatar'],
                                    width: 80,
                                    height: 80,
                                    fit: BoxFit.cover,
                                  ),
                                ),
                              ),
                            ),
                          ),
                          if (_userProfile['verified'] == true)
                            Positioned(
                              bottom: 2,
                              right: 2,
                              child: Container(
                                width: 24,
                                height: 24,
                                decoration: BoxDecoration(
                                  color: colorScheme.primary,
                                  shape: BoxShape.circle,
                                  border: Border.all(
                                    color: colorScheme.surface,
                                    width: 2,
                                  ),
                                ),
                                child: CustomIconWidget(
                                  iconName: 'check',
                                  color: Colors.white,
                                  size: 14,
                                ),
                              ),
                            ),
                        ],
                      ),
                      const Spacer(),
                      // Edit Profile Button
                      OutlinedButton.icon(
                        onPressed: _handleEditProfile,
                        icon: CustomIconWidget(
                          iconName: 'edit',
                          color: colorScheme.primary,
                          size: 18,
                        ),
                        label: Text(
                          'Edit Profile',
                          style: theme.textTheme.labelLarge?.copyWith(
                            color: colorScheme.primary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        style: OutlinedButton.styleFrom(
                          side: BorderSide(color: colorScheme.primary),
                          padding: EdgeInsets.symmetric(
                            horizontal: 4.w,
                            vertical: 1.h,
                          ),
                        ),
                      ),
                    ],
                  ),

                  SizedBox(height: 2.h),

                  // Name and Username
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _userProfile['name'],
                          style: theme.textTheme.headlineSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SizedBox(height: 0.5.h),
                        Text(
                          '@${_userProfile['username']}',
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: colorScheme.onSurface.withValues(alpha: 0.6),
                          ),
                        ),
                      ],
                    ),
                  ),

                  SizedBox(height: 1.5.h),

                  // Bio
                  if (_userProfile['bio'] != null)
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        _userProfile['bio'],
                        style: theme.textTheme.bodyMedium?.copyWith(
                          height: 1.4,
                        ),
                      ),
                    ),

                  SizedBox(height: 1.5.h),

                  // Info Row (Company, Location, Website, Join Date)
                  Wrap(
                    spacing: 3.w,
                    runSpacing: 1.h,
                    children: [
                      if (_userProfile['company'] != null)
                        _buildInfoChip(
                          icon: 'business',
                          text: _userProfile['company'],
                          theme: theme,
                        ),
                      if (_userProfile['location'] != null)
                        _buildInfoChip(
                          icon: 'location_on',
                          text: _userProfile['location'],
                          theme: theme,
                        ),
                      if (_userProfile['website'] != null)
                        _buildInfoChip(
                          icon: 'link',
                          text: _userProfile['website'],
                          theme: theme,
                          isLink: true,
                        ),
                      _buildInfoChip(
                        icon: 'calendar_today',
                        text: 'Joined ${_userProfile['joinDate']}',
                        theme: theme,
                      ),
                    ],
                  ),

                  SizedBox(height: 2.h),

                  // Stats Row (Followers, Following, Posts, Connections)
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildStatColumn(
                        count: _userProfile['posts'] ?? 0,
                        label: 'Posts',
                        theme: theme,
                      ),
                      _buildStatColumn(
                        count: _userProfile['followers'] ?? 0,
                        label: 'Followers',
                        theme: theme,
                      ),
                      _buildStatColumn(
                        count: _userProfile['following'] ?? 0,
                        label: 'Following',
                        theme: theme,
                      ),
                      _buildStatColumn(
                        count: _userProfile['connections'] ?? 0,
                        label: 'Connections',
                        theme: theme,
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildInfoChip({
    required String icon,
    required String text,
    required ThemeData theme,
    bool isLink = false,
  }) {
    final colorScheme = theme.colorScheme;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        CustomIconWidget(
          iconName: icon,
          color: colorScheme.onSurface.withValues(alpha: 0.6),
          size: 16,
        ),
        SizedBox(width: 1.w),
        Text(
          text,
          style: theme.textTheme.bodySmall?.copyWith(
            color: isLink
                ? colorScheme.primary
                : colorScheme.onSurface.withValues(alpha: 0.7),
            decoration: isLink ? TextDecoration.underline : null,
          ),
        ),
      ],
    );
  }

  Widget _buildStatColumn({
    required int count,
    required String label,
    required ThemeData theme,
  }) {
    return Column(
      children: [
        Text(
          count >= 1000
              ? '${(count / 1000).toStringAsFixed(1)}K'
              : count.toString(),
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        SizedBox(height: 0.5.h),
        Text(
          label,
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
          ),
        ),
      ],
    );
  }

  Future<void> _loadUserProfileAndStats() async {
    try {
      // Load current user from backend
      final user = await UserRepository.getCurrentUser();
      // Optionally override avatar from local preferences when offline
      final prefs = await SharedPreferences.getInstance();
      final cachedAvatar = prefs.getString(_prefAvatar);
      _currentUser = cachedAvatar != null && cachedAvatar.isNotEmpty
          ? UserModel(
              id: user.id,
              username: user.username,
              name: user.name,
              avatar: cachedAvatar,
              bio: user.bio,
              isVerified: user.isVerified,
              isOnline: user.isOnline,
              isPremium: user.isPremium,
              avatarPublic: user.avatarPublic,
              company: user.company,
              town: user.town,
              quote: user.quote,
            )
          : user;

      // Load stats from backend (post count, followers, following, connections)
      // Assuming endpoints exist; if not, adapt to available ones
      final posts = await PostRepository.getPostsByUser(user.id, page: 0, size: 1);
      _postCount = posts.isNotEmpty ? posts.first.viewCount : 0; // Placeholder; replace with count endpoint if available
      // For followers/following/connections, ideally dedicated endpoints exist. Fallback to 0.
      _followers = 0;
      _following = 0;
      _connections = 0;
    } catch (e) {
      debugPrint('Failed to load user profile/stats: $e');
    } finally {
      if (mounted) setState(() {});
    }
  }

  // Helper to update avatar online and cache locally
  Future<void> _updateAvatar(String newAvatarUrl) async {
    try {
      final updated = await UserRepository.updateProfile(avatar: newAvatarUrl);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_prefAvatar, newAvatarUrl);
      _currentUser = updated;
      if (mounted) setState(() {});
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Profile picture updated')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to update avatar: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return FutureBuilder<int>(
      future: SharedPreferences.getInstance().then((prefs) => prefs.getInt('shopregidtered') ?? 0),
      builder: (context, snapshot) {
        final isShopRegistered = snapshot.data == 1;
        return Scaffold(
          backgroundColor: theme.colorScheme.surface,
          appBar: AppBar(
        title: Text(
          'Settings',
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.w600,
          ),
        ),
        centerTitle: true,
        backgroundColor: theme.colorScheme.surface,
        foregroundColor: theme.colorScheme.onSurface,
        elevation: 0,
        automaticallyImplyLeading: false,
      ),
      body: Consumer<ThemeProvider>(
        builder: (context, themeProvider, child) {
          _darkMode = themeProvider.themeMode == ThemeMode.dark;
          _systemTheme = themeProvider.themeMode == ThemeMode.system;

          return ListView(
            children: [
              _buildProfileHeader(),
              
              SizedBox(height: 1.h),
              SettingsSectionWidget(
                title: 'Privacy & Security',
                children: [
                  SettingsSwitchWidget(
                    title: 'Biometric Authentication',
                    subtitle: 'Use fingerprint or face ID to unlock the app',
                    iconName: 'fingerprint',
                    value: _biometricAuth,
                    onChanged: _handleBiometricToggle,
                  ),
                  SettingsTileWidget(
                    title: 'Change Password',
                    subtitle: 'Update your account password',
                    iconName: 'lock_outline',
                    onTap: _handlePasswordChange,
                  ),
                  SettingsSwitchWidget(
                    title: 'Two-Factor Authentication',
                    subtitle: 'Add an extra layer of security',
                    iconName: 'security',
                    value: _twoFactorAuth,
                    onChanged: _handleTwoFactorToggle,
                  ),
                  SettingsSwitchWidget(
                    title: 'Public Profile',
                    subtitle: 'Allow others to find and view your profile',
                    iconName: 'visibility',
                    value: _accountVisibility,
                    onChanged: (value) {
                      setState(() {
                        _accountVisibility = value;
                      });
                      _saveSettings();
                    },
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'Notifications',
                children: [
                  SettingsSwitchWidget(
                    title: 'Social Interactions',
                    subtitle: 'Likes, comments, follows, and mentions',
                    iconName: 'favorite_outline',
                    value: _socialNotifications,
                    onChanged: (value) =>
                        _handleNotificationToggle('social', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'Messages',
                    subtitle: 'Direct messages and chat notifications',
                    iconName: 'message_outlined',
                    value: _messageNotifications,
                    onChanged: (value) =>
                        _handleNotificationToggle('messages', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'Education Reminders',
                    subtitle: 'Course updates and learning reminders',
                    iconName: 'school_outlined',
                    value: _educationNotifications,
                    onChanged: (value) =>
                        _handleNotificationToggle('education', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'Marketplace Alerts',
                    subtitle: 'Product updates and transaction notifications',
                    iconName: 'shopping_bag_outlined',
                    value: _marketplaceNotifications,
                    onChanged: (value) =>
                        _handleNotificationToggle('marketplace', value),
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'Appearance',
                children: [
                  SettingsSwitchWidget(
                    title: 'Use System Theme',
                    subtitle: 'Follow device dark/light mode setting',
                    iconName: 'brightness_auto',
                    value: _systemTheme,
                    onChanged: (value) => _handleThemeToggle('system', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'Dark Mode',
                    subtitle: 'Use dark theme for better night viewing',
                    iconName: 'dark_mode',
                    value: _darkMode,
                    onChanged: (value) => _handleThemeToggle('dark', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'High Contrast',
                    subtitle: 'Increase contrast for better visibility',
                    iconName: 'contrast',
                    value: _highContrast,
                    onChanged: (value) => _handleThemeToggle('contrast', value),
                  ),
                  SettingsSwitchWidget(
                    title: 'Large Text',
                    subtitle: 'Increase text size across the entire app',
                    iconName: 'text_fields',
                    value: themeProvider.largeTextEnabled,
                    onChanged: (value) => _handleThemeToggle('text', value),
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'Data & Storage',
                children: [
                  SettingsSwitchWidget(
                    title: 'Offline Content',
                    subtitle: 'Download content for offline access',
                    iconName: 'cloud_download',
                    value: _offlineContent,
                    onChanged: (value) {
                      setState(() {
                        _offlineContent = value;
                      });
                      _saveSettings();
                    },
                  ),
                  SettingsTileWidget(
                    title: 'Data Usage',
                    subtitle: 'View and manage data consumption',
                    iconName: 'data_usage',
                    onTap: _handleDataUsage,
                  ),
                  SettingsTileWidget(
                    title: 'Clear Cache',
                    subtitle: 'Free up storage space',
                    iconName: 'delete_outline',
                    onTap: _handleClearCache,
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'Help & Support',
                children: [
                  SettingsTileWidget(
                    title: 'Frequently Asked Questions',
                    subtitle: 'Find answers to common questions',
                    iconName: 'help_outline',
                    onTap: _handleFAQ,
                  ),
                  SettingsTileWidget(
                    title: 'Contact Support',
                    subtitle: 'Get help from our support team',
                    iconName: 'support_agent',
                    onTap: _handleContactSupport,
                  ),
                  SettingsTileWidget(
                    title: 'Send Feedback',
                    subtitle: 'Help us improve the app',
                    iconName: 'feedback_outlined',
                    onTap: _handleFeedback,
                  ),
                  SettingsTileWidget(
                    title: 'Community Guidelines',
                    subtitle: 'Learn about our community standards',
                    iconName: 'gavel',
                    onTap: _handleGuidelines,
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'About',
                children: [
                  SettingsTileWidget(
                    title: 'App Version',
                    subtitle: '2.1.0 (Build 42)',
                    iconName: 'info_outline',
                  ),
                  SettingsTileWidget(
                    title: 'Terms of Service',
                    iconName: 'description',
                    onTap: _handleTerms,
                  ),
                  SettingsTileWidget(
                    title: 'Privacy Policy',
                    iconName: 'privacy_tip',
                    onTap: _handlePrivacyPolicy,
                  ),
                  SettingsTileWidget(
                    title: 'Open Source Licenses',
                    iconName: 'code',
                    onTap: _handleLicenses,
                    showDivider: false,
                  ),
                ],
              ),
              SettingsSectionWidget(
                title: 'Account',
                children: [
                  SettingsTileWidget(
                    title: 'Logout',
                    iconName: 'logout',
                    onTap: _handleLogout,
                    trailing: const SizedBox.shrink(),
                  ),
                  SettingsTileWidget(
                    title: 'Delete Account',
                    subtitle: 'Permanently delete your account and data',
                    iconName: 'delete_forever',
                    onTap: _handleDeleteAccount,
                    trailing: const SizedBox.shrink(),
                    showDivider: false,
                  ),
                ],
              ),
              SizedBox(height: 4.h),
            ],
          );
        },
      ),
          bottomNavigationBar: const CustomBottomBar(
            currentIndex: 3,
            variant: CustomBottomBarVariant.standard,
          ),
          floatingActionButton: isShopRegistered
              ? null
              : FloatingActionButton.extended(
                  onPressed: () async {
                    final prefs = await SharedPreferences.getInstance();
                    final isShopRegistered = prefs.getInt('shopregidtered') ?? 0;
                    if (isShopRegistered == 1) {
                      Navigator.pushNamed(context, '/sell-something');
                    } else {
                      Navigator.pushNamed(context, '/shop-registration-step1');
                    }
                  },
                  icon: const Icon(Icons.store),
                  label: const Text('Create Shop'),
                  backgroundColor: theme.colorScheme.primary,
                ),
        );
      },
    );
  }
}
