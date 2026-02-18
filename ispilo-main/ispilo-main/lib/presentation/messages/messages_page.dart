import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';
import '../../model/message_model.dart';
import '../../core/services/message_service.dart';
import '../../widgets/custom_app_bar.dart';
import '../../widgets/custom_bottom_bar.dart';

class MessagesPage extends StatefulWidget {
  const MessagesPage({super.key});

  @override
  State<MessagesPage> createState() => _MessagesPageState();
}

class _MessagesPageState extends State<MessagesPage> {
  final TextEditingController _searchController = TextEditingController();
  List<ConversationModel> _conversations = [];
  List<ConversationModel> _filteredConversations = [];
  bool _isLoading = true;
  int _unreadCount = 0;

  @override
  void initState() {
    super.initState();
    _loadConversations();
    _loadUnreadCount();
  }

  Future<void> _loadConversations() async {
    setState(() => _isLoading = true);
    try {
      final conversations = await MessageService.getConversations();
      setState(() {
        _conversations = conversations;
        _filteredConversations = conversations;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error loading conversations: $e')),
        );
      }
    }
  }

  Future<void> _loadUnreadCount() async {
    try {
      final count = await MessageService.getUnreadMessageCount();
      setState(() => _unreadCount = count);
    } catch (e) {
      debugPrint('Error loading unread count: $e');
    }
  }

  void _filterConversations(String query) {
    setState(() {
      if (query.isEmpty) {
        _filteredConversations = _conversations;
      } else {
        _filteredConversations = _conversations
            .where((c) => c.name.toLowerCase().contains(query.toLowerCase()))
            .toList();
      }
    });
  }

  void _openConversation(ConversationModel conversation) {
    // Navigate to conversation detail page
    // Navigator.pushNamed(context, '/chat', arguments: conversation);
  }

  Future<void> _deleteConversation(ConversationModel conversation) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete conversation?'),
        content: Text('Delete conversation with ${conversation.name}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await MessageService.deleteConversation(conversation.id);
      await _loadConversations();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.surface,
      appBar: CustomAppBar(
        title: 'Messages',
        actions: [
          if (_unreadCount > 0)
            Container(
              margin: EdgeInsets.only(right: 2.w),
              padding: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
              decoration: BoxDecoration(
                color: Colors.red,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                _unreadCount.toString(),
                style: theme.textTheme.labelSmall?.copyWith(color: Colors.white),
              ),
            ),
        ],
      ),
      body: Column(
        children: [
          // Search bar
          Padding(
            padding: EdgeInsets.all(4.w),
            child: TextField(
              controller: _searchController,
              onChanged: _filterConversations,
              decoration: InputDecoration(
                hintText: 'Search conversations',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          _filterConversations('');
                        },
                      )
                    : null,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ),

          // Conversations list
          Expanded(
            child: _isLoading
                ? Center(
                    child: CircularProgressIndicator(color: colorScheme.primary),
                  )
                : _filteredConversations.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.mail_outline, size: 48, color: colorScheme.outline),
                            SizedBox(height: 2.h),
                            Text(
                              'No messages',
                              style: theme.textTheme.bodyLarge,
                            ),
                          ],
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _loadConversations,
                        child: ListView.builder(
                          itemCount: _filteredConversations.length,
                          itemBuilder: (context, index) {
                            final conversation = _filteredConversations[index];
                            return _buildConversationTile(conversation, colorScheme);
                          },
                        ),
                      ),
          ),
        ],
      ),
      bottomNavigationBar: const CustomBottomBar(
        currentIndex: 2,
        variant: CustomBottomBarVariant.standard,
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // Show new conversation dialog
          _showNewConversationDialog();
        },
        child: const Icon(Icons.add_comment),
      ),
    );
  }

  Widget _buildConversationTile(
    ConversationModel conversation,
    ColorScheme colorScheme,
  ) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
      child: InkWell(
        onTap: () => _openConversation(conversation),
        child: Container(
          padding: EdgeInsets.all(3.w),
          decoration: BoxDecoration(
            color: conversation.unreadCount > 0
                ? colorScheme.primary.withValues(alpha: 0.05)
                : colorScheme.surface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(
              color: colorScheme.outline.withValues(alpha: 0.2),
            ),
          ),
          child: Row(
            children: [
              // Avatar
              if (conversation.participants.isNotEmpty)
                CircleAvatar(
                  radius: 24,
                  backgroundImage: conversation.participants.first.avatar != null
                      ? NetworkImage(conversation.participants.first.avatar!)
                      : null,
                  child: conversation.participants.first.avatar == null
                      ? const Icon(Icons.person)
                      : null,
                )
              else
                CircleAvatar(
                  radius: 24,
                  child: Icon(
                    conversation.isGroup ? Icons.people : Icons.person,
                  ),
                ),
              SizedBox(width: 3.w),
              // Conversation info
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            conversation.name,
                            style: Theme.of(context).textTheme.titleSmall?.copyWith(
                                  fontWeight: conversation.unreadCount > 0
                                      ? FontWeight.bold
                                      : FontWeight.normal,
                                ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (conversation.unreadCount > 0)
                          Container(
                            padding: EdgeInsets.symmetric(horizontal: 1.5.w, vertical: 0.3.h),
                            decoration: BoxDecoration(
                              color: Colors.red,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Text(
                              conversation.unreadCount.toString(),
                              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                    color: Colors.white,
                                  ),
                            ),
                          ),
                      ],
                    ),
                    SizedBox(height: 0.5.h),
                    Text(
                      conversation.lastMessage,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurface.withValues(alpha: 0.7),
                          ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    SizedBox(height: 0.5.h),
                    Text(
                      _getTimeAgo(conversation.lastMessageTime),
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                            color: colorScheme.onSurface.withValues(alpha: 0.5),
                          ),
                    ),
                  ],
                ),
              ),
              SizedBox(width: 2.w),
              // Options
              PopupMenuButton(
                itemBuilder: (context) => [
                  PopupMenuItem(
                    child: const Text('Delete'),
                    onTap: () => _deleteConversation(conversation),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _getTimeAgo(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inSeconds < 60) {
      return 'just now';
    } else if (difference.inMinutes < 60) {
      return '${difference.inMinutes}m';
    } else if (difference.inHours < 24) {
      return '${difference.inHours}h';
    } else if (difference.inDays < 7) {
      return '${difference.inDays}d';
    } else {
      return dateTime.toString().split(' ')[0];
    }
  }

  void _showNewConversationDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Start a new conversation'),
        content: const Text('This feature is coming soon'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }
}
