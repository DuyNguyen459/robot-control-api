import 'package:shared_preferences/shared_preferences.dart';

import '../models/app_user.dart';
import '../models/session_identity.dart';
import 'api_service.dart';

class SessionService {
  static const String _tokenKey = 'session_access_token';
  static const String _lastActiveEpochMsKey = 'session_last_active_epoch_ms';
  static const String _userIdKey = 'session_user_id';
  static const String _usernameKey = 'session_username';
  static const String _emailKey = 'session_email';
  static const String _roleKey = 'session_role';
  static const String _statusKey = 'session_status';
  static const String _factoryIdKey = 'session_factory_id';
  static const Duration _sessionTtl = Duration(days: 30);

  static Future<void> persistSession({
    required String token,
    required AppUser user,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    final now = DateTime.now().millisecondsSinceEpoch;

    await prefs.setString(_tokenKey, token);
    await prefs.setInt(_lastActiveEpochMsKey, now);
    await prefs.setInt(_userIdKey, user.userId);
    await prefs.setString(_usernameKey, user.username);
    await prefs.setString(_emailKey, user.email);
    await prefs.setString(_roleKey, user.role.toUpperCase());

    if (user.status != null) {
      await prefs.setString(_statusKey, user.status!);
    } else {
      await prefs.remove(_statusKey);
    }

    if (user.factoryId != null) {
      await prefs.setInt(_factoryIdKey, user.factoryId!);
    } else {
      await prefs.remove(_factoryIdKey);
    }
  }

  static Future<void> touchSession() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString(_tokenKey);
    if (token == null || token.isEmpty) {
      return;
    }

    await prefs.setInt(
      _lastActiveEpochMsKey,
      DateTime.now().millisecondsSinceEpoch,
    );
  }

  static Future<void> clearSession() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_lastActiveEpochMsKey);
    await prefs.remove(_userIdKey);
    await prefs.remove(_usernameKey);
    await prefs.remove(_emailKey);
    await prefs.remove(_roleKey);
    await prefs.remove(_statusKey);
    await prefs.remove(_factoryIdKey);
    ApiService.clearAccessToken();
  }

  static Future<bool> restoreSessionIfValid() async {
    return _restoreAndRefreshSession();
  }

  static Future<bool> refreshOrExpireSession() async {
    return _restoreAndRefreshSession();
  }

  static Future<bool> _restoreAndRefreshSession() async {
    final identity = await getSessionIdentity();
    if (identity == null) {
      return false;
    }

    final prefs = await SharedPreferences.getInstance();
    final lastActiveEpochMs = prefs.getInt(_lastActiveEpochMsKey);
    if (lastActiveEpochMs == null) {
      await clearSession();
      return false;
    }

    final lastActive = DateTime.fromMillisecondsSinceEpoch(lastActiveEpochMs);
    final inactiveDuration = DateTime.now().difference(lastActive);

    if (inactiveDuration > _sessionTtl) {
      await clearSession();
      return false;
    }

    ApiService.restoreAccessToken(identity.token);
    await touchSession();
    return true;
  }

  static Future<SessionIdentity?> getSessionIdentity() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString(_tokenKey);
    final userId = prefs.getInt(_userIdKey);
    final username = prefs.getString(_usernameKey);
    final email = prefs.getString(_emailKey);
    final role = prefs.getString(_roleKey);

    if (token == null ||
        token.isEmpty ||
        userId == null ||
        username == null ||
        email == null ||
        role == null) {
      return null;
    }

    final user = AppUser(
      userId: userId,
      username: username,
      email: email,
      role: role,
      status: prefs.getString(_statusKey),
      factoryId: prefs.getInt(_factoryIdKey),
    );

    return SessionIdentity(token: token, user: user);
  }

  static Future<String?> getCurrentRole() async {
    final identity = await getSessionIdentity();
    return identity?.user.role;
  }
}
