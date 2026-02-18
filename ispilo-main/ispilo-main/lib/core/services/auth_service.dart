import 'package:shared_preferences/shared_preferences.dart';
import 'api_service.dart';

class AuthService {
  static Future<Map<String, dynamic>> login(String phone, String password) async {
    final response = await ApiService.post('/auth/login', {
      'phone': phone,
      'password': password,
    });
    
    if (response != null && response['token'] != null) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('auth_token', response['token']);
      await prefs.setString('refresh_token', response['refreshToken']);
      // Store user info if needed
    }
    
    return response;
  }

  static Future<Map<String, dynamic>> register({
    required String email,
    required String password,
    required String firstName,
    required String lastName,
    required String phone,
    required String countryCode,
    required String county,
    required String town,
  }) async {
    return await ApiService.post('/auth/register', {
      'email': email,
      'password': password,
      'firstName': firstName,
      'lastName': lastName,
      'phone': phone,
      'countryCode': countryCode,
      'county': county,
      'town': town,
    });
  }

  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('auth_token');
    await prefs.remove('refresh_token');
  }
}
