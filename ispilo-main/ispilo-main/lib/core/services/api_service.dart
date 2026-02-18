import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiService {
  static const String baseUrl = 'http://localhost:8080/api';
  // For production use: static const String baseUrl = 'https://api.ispilo.co.ke/api/v1';

  static Future<Map<String, String>> getHeaders({bool includeAuth = true}) async {
    final prefs = await SharedPreferences.getInstance();
    final token = includeAuth ? prefs.getString('auth_token') : null;

    // Import app security service
    // final appSecurity = AppSecurityService();
    // final securityHeaders = appSecurity.getSecurityHeaders();

    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      HttpHeaders.userAgentHeader: 'IspiloApp/1.0',
      if (token != null) 'Authorization': 'Bearer $token',
      // Device info for analytics and debugging
      'X-App-Version': '1.0.0',
      'X-Platform': Platform.isAndroid ? 'android' : 'ios',
      // TODO: Add security headers from AppSecurityService
      // These prevent unauthorized access to other users' data
      // 'X-App-ID': securityHeaders['X-App-ID'] ?? '',
      // 'X-Device-ID': securityHeaders['X-Device-ID'] ?? '',
      // 'X-App-Signature': securityHeaders['X-App-Signature'] ?? '',
    };
  }

  /// GET request with error handling
  static Future<dynamic> get(String endpoint) async {
    try {
      final headers = await getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
      ).timeout(
        const Duration(seconds: 30),
        onTimeout: () => throw ApiException('Request timeout'),
      );

      return _handleResponse(response);
    } on SocketException {
      throw ApiException('No internet connection');
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Unexpected error: $e');
    }
  }

  /// POST request with error handling
  static Future<dynamic> post(String endpoint, dynamic data) async {
    try {
      final headers = await getHeaders();
      final response = await http.post(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
        body: jsonEncode(data),
      ).timeout(
        const Duration(seconds: 30),
        onTimeout: () => throw ApiException('Request timeout'),
      );

      return _handleResponse(response);
    } on SocketException {
      throw ApiException('No internet connection');
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Unexpected error: $e');
    }
  }

  /// PUT request with error handling
  static Future<dynamic> put(String endpoint, dynamic data) async {
    try {
      final headers = await getHeaders();
      final response = await http.put(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
        body: jsonEncode(data),
      ).timeout(
        const Duration(seconds: 30),
        onTimeout: () => throw ApiException('Request timeout'),
      );

      return _handleResponse(response);
    } on SocketException {
      throw ApiException('No internet connection');
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Unexpected error: $e');
    }
  }

  /// DELETE request with error handling
  static Future<dynamic> delete(String endpoint) async {
    try {
      final headers = await getHeaders();
      final response = await http.delete(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
      ).timeout(
        const Duration(seconds: 30),
        onTimeout: () => throw ApiException('Request timeout'),
      );

      return _handleResponse(response);
    } on SocketException {
      throw ApiException('No internet connection');
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Unexpected error: $e');
    }
  }

  /// Handle HTTP response and error codes
  static dynamic _handleResponse(http.Response response) {
    try {
      final statusCode = response.statusCode;

      if (statusCode >= 200 && statusCode < 300) {
        if (response.body.isEmpty) return null;
        return jsonDecode(response.body);
      }

      // Handle specific error codes
      switch (statusCode) {
        case 400:
          throw ApiException('Bad request: ${_getErrorMessage(response)}');
        case 401:
          // Token expired or invalid - clear local storage
          _handleUnauthorized();
          throw ApiException('Unauthorized - Please login again');
        case 403:
          throw ApiException('Forbidden: You do not have permission');
        case 404:
          throw ApiException('Resource not found');
        case 409:
          throw ApiException('Conflict: ${_getErrorMessage(response)}');
        case 429:
          throw ApiException('Too many requests - Please try again later');
        case 500:
          throw ApiException('Server error - Please try again later');
        case 503:
          throw ApiException('Service unavailable - Please try again later');
        default:
          throw ApiException('Error ${statusCode}: ${_getErrorMessage(response)}');
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('Failed to parse response: $e');
    }
  }

  /// Extract error message from response
  static String _getErrorMessage(http.Response response) {
    try {
      final data = jsonDecode(response.body);
      if (data is Map && data.containsKey('message')) {
        return data['message'] ?? 'Unknown error';
      }
      return response.body;
    } catch (_) {
      return response.body;
    }
  }

  /// Handle unauthorized access - clear token and redirect to login
  static Future<void> _handleUnauthorized() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('auth_token');
    await prefs.remove('refresh_token');
    // TODO: Trigger navigation to login screen using your routing mechanism
  }
}

/// Custom exception class for API errors
class ApiException implements Exception {
  final String message;
  ApiException(this.message);

  @override
  String toString() => message;
}
