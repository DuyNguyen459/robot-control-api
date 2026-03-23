import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart' show kDebugMode, debugPrint;
import 'package:http/http.dart' as http;

import '../models/area.dart';
import '../models/app_user.dart';
import '../models/device.dart';
import '../models/factory.dart';
import '../models/hub.dart';
import '../models/robot_alert.dart';
import '../models/robot_log.dart';
import '../models/robot_telemetry.dart';
import 'session_service.dart';

class ApiAuthException implements Exception {
  final String message;

  ApiAuthException(this.message);

  @override
  String toString() => message;
}

class ApiService {
  // Can be provided at build time: --dart-define=API_BASE_URL=https://.../api
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://robot-control-system-rmbw.onrender.com/api',
  );

  static const String _bootAccessToken = String.fromEnvironment(
    'API_ACCESS_TOKEN',
    defaultValue: '',
  );
  static String _accessToken = _bootAccessToken;
  static const Duration _requestTimeout = Duration(seconds: 20);

  bool get hasToken => _accessToken.isNotEmpty;
  String get accessToken => _accessToken;

  static void restoreAccessToken(String token) {
    _accessToken = token.trim();
  }

  static void clearAccessToken() {
    _accessToken = '';
  }

  void setAccessToken(String token) {
    _accessToken = token.trim();
  }

  String _extractApiMessage(String body) {
    try {
      final dynamic decoded = json.decode(body);
      if (decoded is Map<String, dynamic>) {
        final dynamic message = decoded['message'] ?? decoded['error'];
        if (message != null) {
          return message.toString();
        }
      }
    } catch (_) {
      // Ignore parse failures and return raw body.
    }
    return body;
  }

  String _formatDateParam(DateTime date) {
    final local = date.toLocal();
    final year = local.year.toString().padLeft(4, '0');
    final month = local.month.toString().padLeft(2, '0');
    final day = local.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  String _formatDateParamUtc(DateTime date) {
    final utc = date.toUtc();
    final year = utc.year.toString().padLeft(4, '0');
    final month = utc.month.toString().padLeft(2, '0');
    final day = utc.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  void _logRequestDuration({
    required String method,
    required Uri uri,
    required Stopwatch stopwatch,
    int? statusCode,
    Object? error,
  }) {
    if (!kDebugMode) {
      return;
    }

    final statusText = statusCode == null ? '' : ' status=$statusCode';
    final errorText = error == null ? '' : ' error=$error';
    debugPrint(
      '[API] $method $uri - ${stopwatch.elapsedMilliseconds}ms$statusText$errorText',
    );
  }

  Future<AppUser> login({
    required String username,
    required String password,
  }) async {
    final uri = Uri.parse('$baseUrl/auth/login');
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .post(
            uri,
            headers: const {
              'Content-Type': 'application/json',
              'accept': '*/*',
            },
            body: json.encode({'username': username, 'password': password}),
          )
          .timeout(_requestTimeout);

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw ApiAuthException(
          'Login failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final dynamic decoded = json.decode(response.body);
      if (decoded is! Map<String, dynamic>) {
        throw ApiAuthException('Invalid login response format');
      }

      final dynamic data = decoded['data'];
      if (data is! Map<String, dynamic>) {
        throw ApiAuthException('Token payload not found in response');
      }

      final String? token = data['accessToken']?.toString();
      if (token == null || token.isEmpty) {
        throw ApiAuthException('accessToken not found in response');
      }

      final user = AppUser.fromJson(data);
      if (user.username.isEmpty || user.role.isEmpty) {
        throw ApiAuthException('User profile not found in login response');
      }

      setAccessToken(token);
      await SessionService.persistSession(token: token, user: user);
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );
      return user;
    } on ApiAuthException {
      _logRequestDuration(method: 'POST', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw ApiAuthException(
        'Login timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw ApiAuthException('Unable to connect to backend: $e');
    }
  }

  Map<String, String> _authorizedHeaders() {
    if (_accessToken.isEmpty) {
      throw ApiAuthException('Sign in is required to access this resource');
    }

    return {'accept': '*/*', 'Authorization': 'Bearer $_accessToken'};
  }

  Map<String, String> _jsonAuthorizedHeaders() {
    return {..._authorizedHeaders(), 'Content-Type': 'application/json'};
  }

  String? _normalizeUserStatusForUpdate(String? status) {
    if (status == null || status.trim().isEmpty) {
      return null;
    }

    final normalized = status.trim().toUpperCase();
    if (normalized == 'ACTIVE') {
      return 'Active';
    }
    if (normalized == 'INACTIVE') {
      return 'Inactive';
    }

    return status.trim();
  }

  List<dynamic> _extractItemsFromResponse(String body) {
    final dynamic decoded = json.decode(body);

    if (decoded is List) {
      return decoded;
    }

    if (decoded is Map<String, dynamic>) {
      final dynamic nested =
          decoded['data'] ?? decoded['items'] ?? decoded['results'];
      if (nested is List) {
        return nested;
      }
    }

    throw Exception('Invalid response format');
  }

  Future<List<T>> _fetchList<T>(
    String endpoint,
    T Function(Map<String, dynamic>) fromJson, {
    Map<String, String>? queryParameters,
  }) async {
    final uri = Uri.parse(
      '$baseUrl$endpoint',
    ).replace(queryParameters: queryParameters);
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .get(uri, headers: _authorizedHeaders())
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode != 200) {
        throw Exception(
          'Server error (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final items = _extractItemsFromResponse(response.body);
      await SessionService.touchSession();
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );
      return items.whereType<Map<String, dynamic>>().map(fromJson).toList();
    } on ApiAuthException {
      _logRequestDuration(method: 'GET', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to connect to backend: $e');
    }
  }

  Future<T> _fetchObject<T>(
    String endpoint,
    T Function(Map<String, dynamic>) fromJson, {
    Map<String, String>? queryParameters,
  }) async {
    final uri = Uri.parse(
      '$baseUrl$endpoint',
    ).replace(queryParameters: queryParameters);
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .get(uri, headers: _authorizedHeaders())
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode != 200) {
        throw Exception(
          'Server error (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final dynamic decoded = json.decode(response.body);
      final dynamic payload;

      if (decoded is Map<String, dynamic> &&
          decoded['data'] is Map<String, dynamic>) {
        payload = decoded['data'];
      } else if (decoded is Map<String, dynamic>) {
        payload = decoded;
      } else {
        throw Exception('Invalid response format');
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );
      return fromJson(payload as Map<String, dynamic>);
    } on ApiAuthException {
      _logRequestDuration(method: 'GET', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'GET',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to connect to backend: $e');
    }
  }

  Future<List<Factory>> fetchFactories() async {
    return _fetchList('/factories/all', Factory.fromJson);
  }

  Future<List<Area>> fetchAreasByFactory(String factoryId) {
    return _fetchList('/factories/$factoryId/areas', Area.fromJson);
  }

  Future<List<Hub>> fetchHubsByArea(String areaId) {
    return _fetchList('/areas/$areaId/hubs', Hub.fromJson);
  }

  Future<List<Device>> fetchDevicesByHub(String hubId) {
    return _fetchList('/hubs/$hubId/devices', Device.fromJson);
  }

  Future<List<RobotLogEntry>> fetchRobotLogsToday(
    int robotId, {
    DateTime? date,
    int limit = 50,
  }) {
    final day = _formatDateParam(date ?? DateTime.now());
    final params = <String, String>{'date': day, 'limit': '$limit'};

    return _fetchList(
      '/logs/robots/$robotId',
      RobotLogEntry.fromJson,
      queryParameters: params,
    );
  }

  Future<List<RobotLogEntry>> fetchRobotAlerts(
    int robotId, {
    String? severity,
    DateTime? date,
    int limit = 50,
  }) {
    final day = _formatDateParam(date ?? DateTime.now());
    final params = <String, String>{'date': day, 'limit': '$limit'};
    if (severity != null && severity.trim().isNotEmpty) {
      params['severity'] = severity.trim();
    }

    return _fetchList(
      '/logs/robots/$robotId/alerts',
      RobotLogEntry.fromJson,
      queryParameters: params,
    );
  }

  Future<List<RobotAlert>> fetchRobotAlertsByDate(
    int robotId, {
    required DateTime date,
    String? severity,
    int limit = 100,
  }) {
    final day = _formatDateParam(date);
    final params = <String, String>{'date': day, 'limit': '$limit'};
    if (severity != null && severity.trim().isNotEmpty) {
      params['severity'] = severity.trim();
    }

    return _fetchList(
      '/logs/robots/$robotId/alerts',
      RobotAlert.fromJson,
      queryParameters: params,
    );
  }

  Future<List<RobotTelemetry>> fetchTelemetryHistoryByDate(
    int robotId, {
    required DateTime date,
    int limit = 300,
  }) async {
    final day = _formatDateParam(date);

    final allLogs = await _fetchList<Map<String, dynamic>>(
      '/logs/robots/$robotId',
      (json) => json,
      queryParameters: {'date': day, 'limit': '$limit'},
    );

    return allLogs
        .where(_looksLikeTelemetryLog)
        .map(_mapTelemetryFromRobotLog)
        .toList();
  }

  Future<List<RobotTelemetry>> fetchRobotTelemetryToday(
    int robotId, {
    int limit = 50,
  }) async {
    final allLogs = await _fetchList<Map<String, dynamic>>(
      '/logs/robots/$robotId/today',
      (json) => json,
      queryParameters: {'limit': '$limit'},
    );

    final telemetryCandidates = allLogs
        .where(_looksLikeTelemetryLog)
        .map(_mapTelemetryFromRobotLog)
        .toList();

    return telemetryCandidates;
  }

  Future<List<RobotAlert>> fetchRobotAlertsToday(
    int robotId, {
    String? severity,
    int limit = 50,
  }) {
    final day = _formatDateParamUtc(DateTime.now());
    final params = <String, String>{'date': day, 'limit': '$limit'};
    if (severity != null && severity.trim().isNotEmpty) {
      params['severity'] = severity.trim();
    }

    return _fetchList(
      '/logs/robots/$robotId/alerts',
      RobotAlert.fromJson,
      queryParameters: params,
    );
  }

  RobotTelemetry _mapTelemetryFromRobotLog(Map<String, dynamic> log) {
    final rawMetadata = log['metadata'];

    Map<String, dynamic> metadata;
    if (rawMetadata is Map<String, dynamic>) {
      metadata = Map<String, dynamic>.from(rawMetadata);
    } else if (rawMetadata is Map) {
      metadata = rawMetadata.map(
        (key, value) => MapEntry(key.toString(), value),
      );
    } else {
      metadata = <String, dynamic>{};
    }

    if (metadata.isEmpty) {
      final rawMessage = log['message'];
      if (rawMessage is String && rawMessage.trim().isNotEmpty) {
        try {
          final decoded = json.decode(rawMessage);
          if (decoded is Map<String, dynamic>) {
            metadata = Map<String, dynamic>.from(decoded);
          } else if (decoded is Map) {
            metadata = decoded.map(
              (key, value) => MapEntry(key.toString(), value),
            );
          }
        } catch (_) {
          // Keep metadata empty when message is not JSON.
        }
      }
    }

    metadata = _extractTelemetryPayloadFromMetadata(metadata);

    final telemetryJson = <String, dynamic>{
      'jointData':
          log['jointData'] ?? log['joint_data'] ?? metadata['jointData'],
      'batteryPercent':
          log['batteryPercent'] ??
          log['battery_percent'] ??
          metadata['batteryPercent'],
      'batteryStatus':
          log['batteryStatus'] ??
          log['battery_status'] ??
          metadata['batteryStatus'],
      'uptimeSeconds':
          log['uptimeSeconds'] ??
          log['uptime_seconds'] ??
          metadata['uptimeSeconds'],
      'robotTemp':
          log['robotTemp'] ?? log['robot_temp'] ?? metadata['robotTemp'],
      'motorTemp':
          log['motorTemp'] ?? log['motor_temp'] ?? metadata['motorTemp'],
      'cpuTemp': log['cpuTemp'] ?? log['cpu_temp'] ?? metadata['cpuTemp'],
      'fps': log['fps'] ?? metadata['fps'],
      'internetReachability':
          log['internetReachability'] ??
          log['internet_reachability'] ??
          metadata['internetReachability'],
      'deviceModel':
          log['deviceModel'] ?? log['device_model'] ?? metadata['deviceModel'],
      ...metadata,
      'deviceId':
          metadata['deviceId'] ?? metadata['device_id'] ?? log['robotId'],
      'timestamp':
          metadata['timestamp'] ?? metadata['eventTime'] ?? log['eventTime'],
      'deviceName':
          metadata['deviceName'] ?? metadata['device_name'] ?? metadata['name'],
      'deviceType': metadata['deviceType'] ?? metadata['device_type'],
    };

    return RobotTelemetry.fromJson(telemetryJson);
  }

  Map<String, dynamic> _extractTelemetryPayloadFromMetadata(
    Map<String, dynamic> metadata,
  ) {
    if (metadata.isEmpty) {
      return metadata;
    }

    for (final key in ['telemetry', 'data', 'payload']) {
      final nested = metadata[key];
      if (nested is Map<String, dynamic>) {
        return nested;
      }
      if (nested is Map) {
        return nested.map((k, v) => MapEntry(k.toString(), v));
      }
    }

    return metadata;
  }

  bool _looksLikeTelemetryLog(Map<String, dynamic> log) {
    final logType = (log['logType'] ?? log['log_type'] ?? '').toString();
    if (logType.toUpperCase() == 'TELEMETRY') {
      return true;
    }

    final metadataRaw = log['metadata'];
    Map<String, dynamic> metadata = <String, dynamic>{};
    if (metadataRaw is Map<String, dynamic>) {
      metadata = metadataRaw;
    } else if (metadataRaw is Map) {
      metadata = metadataRaw.map((k, v) => MapEntry(k.toString(), v));
    }

    if (metadata.isNotEmpty) {
      final payload = _extractTelemetryPayloadFromMetadata(metadata);
      if (payload.containsKey('jointData') ||
          payload.containsKey('joint_data') ||
          payload.containsKey('angles') ||
          payload.containsKey('batteryPercent') ||
          payload.containsKey('battery_percent') ||
          payload.containsKey('battery') ||
          payload.containsKey('fps') ||
          payload.containsKey('robotTemp') ||
          payload.containsKey('robot_temp') ||
          payload.containsKey('cpuTemp') ||
          payload.containsKey('cpu_temp') ||
          payload.containsKey('motorTemp') ||
          payload.containsKey('motor_temp') ||
          payload.containsKey('internetReachability') ||
          payload.containsKey('internet_reachability')) {
        return true;
      }
    }

    final message = log['message'];
    if (message is String && message.trim().isNotEmpty) {
      try {
        final decoded = json.decode(message);
        if (decoded is Map<String, dynamic>) {
          return decoded.containsKey('jointData') ||
              decoded.containsKey('joint_data') ||
              decoded.containsKey('angles') ||
              decoded.containsKey('batteryPercent') ||
              decoded.containsKey('battery_percent') ||
              decoded.containsKey('battery') ||
              decoded.containsKey('fps') ||
              decoded.containsKey('robotTemp') ||
              decoded.containsKey('robot_temp') ||
              decoded.containsKey('cpuTemp') ||
              decoded.containsKey('cpu_temp') ||
              decoded.containsKey('motorTemp') ||
              decoded.containsKey('motor_temp') ||
              decoded.containsKey('internetReachability') ||
              decoded.containsKey('internet_reachability');
        }
      } catch (_) {
        // Ignore non-json message values.
      }
    }

    return false;
  }

  Future<List<RobotLogEntry>> fetchSessionLogs(
    int sessionId, {
    int limit = 100,
  }) {
    return _fetchList(
      '/logs/sessions/$sessionId',
      RobotLogEntry.fromJson,
      queryParameters: {'limit': '$limit'},
    );
  }

  Future<LatestRobotStatus> fetchLatestRobotStatus(int robotId) {
    return _fetchObject(
      '/logs/robots/$robotId/latest-status',
      LatestRobotStatus.fromJson,
    );
  }

  Future<List<AppUser>> fetchUsers({
    int page = 0,
    int size = 50,
    String sortBy = 'userId',
    String sortDir = 'asc',
  }) async {
    final stopwatch = Stopwatch()..start();
    final safeSize = size > 20 ? 20 : size;
    final attempts = <Uri>[
      Uri.parse('$baseUrl/users').replace(
        queryParameters: {
          'page': '$page',
          'size': '$size',
          'sortBy': sortBy,
          'sortDir': sortDir,
        },
      ),
      Uri.parse('$baseUrl/users').replace(
        queryParameters: {
          'page': '$page',
          'size': '$safeSize',
          'sortBy': 'username',
          'sortDir': 'asc',
        },
      ),
      Uri.parse(
        '$baseUrl/users',
      ).replace(queryParameters: {'page': '$page', 'size': '$safeSize'}),
      Uri.parse('$baseUrl/users'),
    ];

    Object? lastError;
    Uri lastUri = attempts.first;

    try {
      for (final uri in attempts) {
        lastUri = uri;
        final response = await http
            .get(uri, headers: _authorizedHeaders())
            .timeout(_requestTimeout);

        _logRequestDuration(
          method: 'GET',
          uri: uri,
          stopwatch: stopwatch,
          statusCode: response.statusCode,
          error: response.statusCode == 200
              ? null
              : _extractApiMessage(response.body),
        );

        if (response.statusCode == 401 || response.statusCode == 403) {
          await SessionService.clearSession();
          throw ApiAuthException(
            'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
          );
        }

        if (response.statusCode != 200) {
          lastError = Exception(
            'Server error (${response.statusCode}): ${_extractApiMessage(response.body)}',
          );

          // Retry only for server-side failures.
          if (response.statusCode >= 500) {
            continue;
          }

          throw lastError;
        }

        final decoded = json.decode(response.body);
        if (decoded is! Map<String, dynamic>) {
          lastError = Exception('Invalid users response format');
          continue;
        }

        final data = decoded['data'];
        final dynamic rawItems;
        if (data is Map<String, dynamic>) {
          rawItems = data['content'] ?? data['items'] ?? data['results'];
        } else {
          rawItems = data;
        }

        final items = rawItems is List ? rawItems : <dynamic>[];
        await SessionService.touchSession();

        return items
            .whereType<Map<String, dynamic>>()
            .map(AppUser.fromJson)
            .toList();
      }

      throw lastError ?? Exception('Unable to load users after retries');
    } on ApiAuthException {
      _logRequestDuration(method: 'GET', uri: lastUri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'GET',
        uri: lastUri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'GET',
        uri: lastUri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to load users: $e');
    }
  }

  Future<AppUser> createUser({
    required String username,
    required String email,
    required String password,
    required String role,
    int? factoryId,
  }) async {
    final uri = Uri.parse('$baseUrl/users');
    final stopwatch = Stopwatch()..start();
    final normalizedRole = role.trim().toUpperCase();

    try {
      final response = await http
          .post(
            uri,
            headers: _jsonAuthorizedHeaders(),
            body: json.encode(
              {
                'username': username,
                'email': email,
                'password': password,
                'role': normalizedRole,
                if (normalizedRole == 'OPERATOR') 'factoryId': factoryId,
              }..removeWhere((key, value) => value == null),
            ),
          )
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception(
          'Create user failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final decoded = json.decode(response.body);
      if (decoded is! Map<String, dynamic> ||
          decoded['data'] is! Map<String, dynamic>) {
        throw Exception('Invalid create user response format');
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );

      return AppUser.fromJson(decoded['data'] as Map<String, dynamic>);
    } on ApiAuthException {
      _logRequestDuration(method: 'POST', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'POST',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to create user: $e');
    }
  }

  Future<AppUser> updateUser({
    required int userId,
    String? username,
    String? email,
    String? password,
    String? status,
    int? factoryId,
  }) async {
    final uri = Uri.parse('$baseUrl/users/$userId');
    final stopwatch = Stopwatch()..start();
    final normalizedStatus = _normalizeUserStatusForUpdate(status);

    try {
      final response = await http
          .put(
            uri,
            headers: _jsonAuthorizedHeaders(),
            body: json.encode(
              {
                'username': username,
                'email': email,
                if (password != null && password.isNotEmpty)
                  'password': password,
                'status': normalizedStatus,
                'factoryId': factoryId,
              }..removeWhere((key, value) => value == null),
            ),
          )
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception(
          'Update user failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final decoded = json.decode(response.body);
      if (decoded is! Map<String, dynamic> ||
          decoded['data'] is! Map<String, dynamic>) {
        throw Exception('Invalid update user response format');
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'PUT',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );

      return AppUser.fromJson(decoded['data'] as Map<String, dynamic>);
    } on ApiAuthException {
      _logRequestDuration(method: 'PUT', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'PUT',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'PUT',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to update user: $e');
    }
  }

  Future<AppUser> updateUserRole({
    required int userId,
    required String role,
  }) async {
    final uri = Uri.parse('$baseUrl/users/$userId/role');
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .patch(
            uri,
            headers: _jsonAuthorizedHeaders(),
            body: json.encode({'role': role.toUpperCase()}),
          )
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception(
          'Update role failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      final decoded = json.decode(response.body);
      if (decoded is! Map<String, dynamic> ||
          decoded['data'] is! Map<String, dynamic>) {
        throw Exception('Invalid update role response format');
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );

      return AppUser.fromJson(decoded['data'] as Map<String, dynamic>);
    } on ApiAuthException {
      _logRequestDuration(method: 'PATCH', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to update role: $e');
    }
  }

  Future<void> updateUserStatus({
    required int userId,
    required String status,
  }) async {
    final uri = Uri.parse('$baseUrl/users/$userId/status');
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .patch(
            uri,
            headers: _jsonAuthorizedHeaders(),
            body: json.encode({'status': status}),
          )
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception(
          'Update status failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );
    } on ApiAuthException {
      _logRequestDuration(method: 'PATCH', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'PATCH',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to update status: $e');
    }
  }

  Future<void> deleteUser(int userId) async {
    final uri = Uri.parse('$baseUrl/users/$userId');
    final stopwatch = Stopwatch()..start();

    try {
      final response = await http
          .delete(uri, headers: _authorizedHeaders())
          .timeout(_requestTimeout);

      if (response.statusCode == 401 || response.statusCode == 403) {
        await SessionService.clearSession();
        throw ApiAuthException(
          'Token is invalid or expired (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception(
          'Delete user failed (${response.statusCode}): ${_extractApiMessage(response.body)}',
        );
      }

      await SessionService.touchSession();
      _logRequestDuration(
        method: 'DELETE',
        uri: uri,
        stopwatch: stopwatch,
        statusCode: response.statusCode,
      );
    } on ApiAuthException {
      _logRequestDuration(method: 'DELETE', uri: uri, stopwatch: stopwatch);
      rethrow;
    } on TimeoutException {
      _logRequestDuration(
        method: 'DELETE',
        uri: uri,
        stopwatch: stopwatch,
        error: 'timeout',
      );
      throw Exception(
        'Request timed out after ${_requestTimeout.inSeconds}s. The backend may be sleeping or unreachable.',
      );
    } catch (e) {
      _logRequestDuration(
        method: 'DELETE',
        uri: uri,
        stopwatch: stopwatch,
        error: e,
      );
      throw Exception('Unable to delete user: $e');
    }
  }
}
