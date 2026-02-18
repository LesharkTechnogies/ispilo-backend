import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';
import '../../model/social_model.dart';
import '../../model/repository/social_repository.dart';

class PostCommentsPage extends StatefulWidget {
  final Map<String, dynamic> postJson;
  const PostCommentsPage({super.key, required this.postJson});

  @override
  State<PostCommentsPage> createState() => _PostCommentsPageState();
}

class _PostCommentsPageState extends State<PostCommentsPage> {
  late final PostModel post;
  final TextEditingController _controller = TextEditingController();
  List<CommentModel> _comments = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    post = PostModel.fromJson(widget.postJson);
    _loadComments();
  }

  Future<void> _loadComments() async {
    setState(() => _loading = true);
    try {
      _comments = await PostRepository.getComments(postId: post.id, page: 0, size: 50);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to load comments: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _addComment() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    try {
      final newComment = await PostRepository.addComment(postId: post.id, content: text);
      setState(() {
        _comments.insert(0, newComment);
        _controller.clear();
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to comment: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Comments')),
      body: Column(
        children: [
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    padding: EdgeInsets.all(3.w),
                    itemCount: _comments.length,
                    itemBuilder: (context, index) {
                      final c = _comments[index];
                      return ListTile(
                        leading: CircleAvatar(backgroundImage: c.userAvatar.isNotEmpty ? NetworkImage(c.userAvatar) : null),
                        title: Text(c.username),
                        subtitle: Text(c.content),
                        trailing: Text('${c.likesCount}'),
                      );
                    },
                  ),
          ),
          SafeArea(
            child: Padding(
              padding: EdgeInsets.all(3.w),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _controller,
                      decoration: const InputDecoration(hintText: 'Write a comment...'),
                    ),
                  ),
                  SizedBox(width: 2.w),
                  ElevatedButton(onPressed: _addComment, child: const Text('Post')),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
