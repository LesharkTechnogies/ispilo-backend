class Seller {
  final String id;
  final String name;
  final String avatar;
  final bool isVerified;
  final String? phone;
  final bool phonePrivacyPublic;
  final String? countryCode;

  Seller({
    required this.id,
    required this.name,
    required this.avatar,
    this.isVerified = false,
    this.phone,
    this.phonePrivacyPublic = false,
    this.countryCode,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'name': name,
        'avatar': avatar,
        'isVerified': isVerified,
        'phone': phone,
        'phonePrivacyPublic': phonePrivacyPublic,
        'countryCode': countryCode,
      };

  factory Seller.fromMap(Map<String, dynamic> m) => Seller(
        id: m['id'] as String,
        name: m['name'] as String,
        avatar: m['avatar'] as String,
        isVerified: m['isVerified'] as bool? ?? false,
        phone: m['phone'] as String?,
        phonePrivacyPublic: m['phonePrivacyPublic'] as bool? ?? false,
        countryCode: m['countryCode'] as String?,
      );
}
