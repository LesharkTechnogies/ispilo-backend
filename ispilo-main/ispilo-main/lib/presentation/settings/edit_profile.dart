import 'dart:io';

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:image_picker/image_picker.dart';
import 'package:sizer/sizer.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../model/repository/social_repository.dart';
import '../../model/social_model.dart';
import '../../widgets/custom_bottom_bar.dart';

class EditProfile extends StatefulWidget {
  const EditProfile({super.key});

  @override
  State<EditProfile> createState() => _EditProfileState();
}

class _EditProfileState extends State<EditProfile> {
  static const String _prefName = 'profile_name';
  static const String _prefUsername = 'profile_username';
  static const String _prefEmail = 'profile_email';
  static const String _prefPhone = 'profile_phone';
  static const String _prefBirthday = 'profile_birthday';
  static const String _prefAvatarPath = 'profile_avatar_path';

  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameCtrl = TextEditingController();
  final TextEditingController _usernameCtrl = TextEditingController();
  final TextEditingController _emailCtrl = TextEditingController();
  final TextEditingController _phoneCtrl = TextEditingController();
  final TextEditingController _companyCtrl = TextEditingController();
  final TextEditingController _townCtrl = TextEditingController();
  final TextEditingController _quoteCtrl = TextEditingController();
  bool _avatarPublic = true;
  DateTime? _birthday;
  String? _avatarPath;
  UserModel? _currentUser;
  bool _saving = false;

  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _loadProfile();
    _loadFromBackend();
  }

  Future<void> _loadProfile() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _nameCtrl.text = prefs.getString(_prefName) ?? '';
      _usernameCtrl.text = prefs.getString(_prefUsername) ?? '';
      _emailCtrl.text = prefs.getString(_prefEmail) ?? '';
      _phoneCtrl.text = prefs.getString(_prefPhone) ?? '';
      final birthdayMs = prefs.getInt(_prefBirthday);
      if (birthdayMs != null) {
        _birthday = DateTime.fromMillisecondsSinceEpoch(birthdayMs);
      }
      _avatarPath = prefs.getString(_prefAvatarPath);
    });
  }

  Future<void> _loadFromBackend() async {
    try {
      final user = await UserRepository.getCurrentUser();
      setState(() {
        _currentUser = user;
        _nameCtrl.text = user.name;
        _usernameCtrl.text = user.username;
        _emailCtrl.text = ''; // email typically managed separately; keep as-is
        _phoneCtrl.text = '';
        _companyCtrl.text = user.company ?? '';
        _townCtrl.text = user.town ?? '';
        _quoteCtrl.text = user.quote ?? '';
        _avatarPublic = user.avatarPublic;
      });
    } catch (e) {
      debugPrint('Failed to load user from backend: $e');
    }
  }

  Future<void> _saveProfile() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefName, _nameCtrl.text.trim());
    await prefs.setString(_prefUsername, _usernameCtrl.text.trim());
    await prefs.setString(_prefEmail, _emailCtrl.text.trim());
    await prefs.setString(_prefPhone, _phoneCtrl.text.trim());
    if (_birthday != null) {
      await prefs.setInt(_prefBirthday, _birthday!.millisecondsSinceEpoch);
    }
    if (_avatarPath != null) {
      await prefs.setString(_prefAvatarPath, _avatarPath!);
    }

    // Build selective payload to avoid overwriting unchanged fields on backend
    try {
      final String name = _nameCtrl.text.trim();
      final String bio = _quoteCtrl.text.trim();
      final String location = _townCtrl.text.trim();
      final String company = _companyCtrl.text.trim();

      final updated = await UserRepository.updateProfile(
        name: name.isNotEmpty ? name : null,
        bio: bio.isNotEmpty ? bio : null,
        avatar: _avatarPath, // send only if picked
        location: location.isNotEmpty ? location : null,
        company: company.isNotEmpty ? company : null,
        quote: bio.isNotEmpty ? bio : null, // use bio as quote mapping
        avatarPublic: _avatarPublic,
      );
      _currentUser = updated;

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Profile updated successfully')),
      );
      // Refetch latest profile from backend to ensure UI reflects server state
      await _loadFromBackend();
      setState(() => _saving = false);
      Navigator.pop(context);
    } catch (e) {
      if (!mounted) return;
      setState(() => _saving = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to update profile: $e')),
      );
    }
  }

  Future<void> _pickAvatar() async {
    final result =
        await _picker.pickImage(source: ImageSource.gallery, maxWidth: 1024);
    if (result != null) {
      setState(() => _avatarPath = result.path);
    }
  }

  Future<void> _pickBirthday() async {
    final now = DateTime.now();
    final initial = _birthday ?? DateTime(now.year - 18, now.month, now.day);
    final first = DateTime(1900);
    final last = DateTime(now.year, now.month, now.day);
    final picked = await showDatePicker(
      context: context,
      initialDate: initial,
      firstDate: first,
      lastDate: last,
    );
    if (picked != null) {
      setState(() => _birthday = picked);
    }
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _usernameCtrl.dispose();
    _emailCtrl.dispose();
    _phoneCtrl.dispose();
    _companyCtrl.dispose();
    _townCtrl.dispose();
    _quoteCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Profile'),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(4.w),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Stack(
                  children: [
                    CircleAvatar(
                      radius: 40,
                      backgroundColor:
                          colorScheme.outline.withValues(alpha: 0.2),
                      backgroundImage: _avatarPath != null
                          ? FileImage(File(_avatarPath!))
                          : null,
                      child: _avatarPath == null
                          ? const Icon(Icons.person, size: 40)
                          : null,
                    ),
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: InkWell(
                        onTap: _pickAvatar,
                        child: Container(
                          decoration: BoxDecoration(
                            color: colorScheme.primary,
                            borderRadius: BorderRadius.circular(16),
                          ),
                          padding: const EdgeInsets.all(6),
                          child: Icon(Icons.edit,
                              color: colorScheme.onPrimary, size: 18),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              SizedBox(height: 2.h),
              TextFormField(
                controller: _nameCtrl,
                decoration: const InputDecoration(
                  labelText: 'Full Name',
                ),
                textInputAction: TextInputAction.next,
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Required' : null,
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _usernameCtrl,
                decoration: const InputDecoration(
                  labelText: 'Username',
                ),
                textInputAction: TextInputAction.next,
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Required' : null,
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _emailCtrl,
                decoration: const InputDecoration(
                  labelText: 'Email',
                ),
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                validator: (v) {
                  if (v == null || v.trim().isEmpty) return 'Required';
                  final emailReg = RegExp(r'^.+@.+\..+');
                  return emailReg.hasMatch(v.trim()) ? null : 'Invalid email';
                },
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _phoneCtrl,
                decoration: const InputDecoration(
                  labelText: 'Phone Number',
                ),
                keyboardType: TextInputType.phone,
                textInputAction: TextInputAction.next,
                validator: (v) =>
                    (v == null || v.trim().length < 7) ? 'Invalid phone' : null,
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _companyCtrl,
                decoration: const InputDecoration(
                  labelText: 'Company (e.g., Leshark Technologies)',
                ),
                textInputAction: TextInputAction.next,
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _townCtrl,
                decoration: const InputDecoration(
                  labelText: 'Town/Location',
                ),
                textInputAction: TextInputAction.next,
              ),
              SizedBox(height: 1.5.h),
              TextFormField(
                controller: _quoteCtrl,
                decoration: const InputDecoration(
                  labelText: 'Quote / About You',
                ),
                maxLines: 2,
                textInputAction: TextInputAction.newline,
              ),
              SizedBox(height: 1.5.h),
              SwitchListTile(
                value: _avatarPublic,
                title: const Text('Make profile picture public'),
                subtitle: const Text('If off, others will see a generic icon'),
                onChanged: (v) => setState(() => _avatarPublic = v),
              ),
              SizedBox(height: 2.h),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _saving ? null : _saveProfile,
                  child: _saving
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Save Changes'),
                ),
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: const CustomBottomBar(
        currentIndex: 3,
        variant: CustomBottomBarVariant.standard,
      ),
    );
  }
}
