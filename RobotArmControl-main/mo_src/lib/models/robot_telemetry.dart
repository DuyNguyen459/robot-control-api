import 'dart:convert';

class RobotTelemetry {
  final int? deviceId;
  final DateTime? timestamp;
  final List<double> jointData;
  final int batteryPercent;
  final String batteryStatus;
  final double uptimeSeconds;
  final double robotTemp;
  final double motorTemp;
  final double cpuTemp;
  final double fps;
  final String internetReachability;
  final String deviceModel;
  final String deviceName;
  final String deviceType;

  const RobotTelemetry({
    required this.deviceId,
    required this.timestamp,
    required this.jointData,
    required this.batteryPercent,
    required this.batteryStatus,
    required this.uptimeSeconds,
    required this.robotTemp,
    required this.motorTemp,
    required this.cpuTemp,
    required this.fps,
    required this.internetReachability,
    required this.deviceModel,
    required this.deviceName,
    required this.deviceType,
  });

  factory RobotTelemetry.fromJson(Map<String, dynamic> json) {
    return RobotTelemetry(
      deviceId: _toInt(json['deviceId'] ?? json['device_id']),
      timestamp: _toDateTime(json['timestamp']),
      jointData: _toDoubleList(
        json['jointData'] ??
            json['joint_data'] ??
            json['jointAngles'] ??
            json['joint_angles'],
      ),
      batteryPercent:
          _toInt(
            json['batteryPercent'] ??
                json['battery_percent'] ??
                json['batteryLevel'] ??
                json['battery_level'],
          ) ??
          0,
      batteryStatus: (json['batteryStatus'] ?? json['battery_status'] ?? '')
          .toString(),
      uptimeSeconds:
          _toDouble(json['uptimeSeconds'] ?? json['uptime_seconds']) ?? 0.0,
      robotTemp:
          _toDouble(
            json['robotTemp'] ?? json['robot_temp'] ?? json['temperature'],
          ) ??
          0.0,
      motorTemp: _toDouble(json['motorTemp'] ?? json['motor_temp']) ?? 0.0,
      cpuTemp: _toDouble(json['cpuTemp'] ?? json['cpu_temp']) ?? 0.0,
      fps: _toDouble(json['fps']) ?? 0.0,
      internetReachability:
          (json['internetReachability'] ??
                  json['internet_reachability'] ??
                  json['networkStatus'] ??
                  json['network_status'] ??
                  '')
              .toString(),
      deviceModel: (json['deviceModel'] ?? json['device_model'] ?? '')
          .toString(),
      deviceName: (json['deviceName'] ?? json['device_name'] ?? '').toString(),
      deviceType: (json['deviceType'] ?? json['device_type'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'deviceId': deviceId,
      'timestamp': timestamp?.toUtc().toIso8601String(),
      'jointData': jointData,
      'batteryPercent': batteryPercent,
      'batteryStatus': batteryStatus,
      'uptimeSeconds': uptimeSeconds,
      'robotTemp': robotTemp,
      'motorTemp': motorTemp,
      'cpuTemp': cpuTemp,
      'fps': fps,
      'internetReachability': internetReachability,
      'deviceModel': deviceModel,
      'deviceName': deviceName,
      'deviceType': deviceType,
    };
  }

  static int? _toInt(dynamic value) {
    if (value == null) {
      return null;
    }
    if (value is int) {
      return value;
    }
    if (value is double) {
      return value.round();
    }
    return int.tryParse(value.toString());
  }

  static double? _toDouble(dynamic value) {
    if (value == null) {
      return null;
    }
    if (value is double) {
      return value;
    }
    if (value is int) {
      return value.toDouble();
    }
    return double.tryParse(value.toString());
  }

  static DateTime? _toDateTime(dynamic value) {
    if (value == null) {
      return null;
    }
    return DateTime.tryParse(value.toString());
  }

  static List<double> _toDoubleList(dynamic value) {
    if (value == null) {
      return const [];
    }

    if (value is List) {
      return value.map((e) => _toDouble(e) ?? 0.0).toList();
    }

    if (value is String && value.trim().isNotEmpty) {
      try {
        final decoded = jsonDecode(value);
        if (decoded is List) {
          return decoded.map((e) => _toDouble(e) ?? 0.0).toList();
        }
      } catch (_) {
        // Keep empty list when input string is not valid JSON.
      }
    }

    return const [];
  }
}
