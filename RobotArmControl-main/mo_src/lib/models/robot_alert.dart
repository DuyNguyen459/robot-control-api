class RobotAlert {
  final int? robotId;
  final DateTime? logDate;
  final DateTime? eventTime;
  final int? userId;
  final int? factoryId;
  final String logType;
  final String severity;
  final String status;
  final String command;

  const RobotAlert({
    required this.robotId,
    required this.logDate,
    required this.eventTime,
    required this.userId,
    required this.factoryId,
    required this.logType,
    required this.severity,
    required this.status,
    required this.command,
  });

  factory RobotAlert.fromJson(Map<String, dynamic> json) {
    return RobotAlert(
      robotId: _toInt(json['robotId'] ?? json['robot_id']),
      logDate: _toDateTime(json['logDate'] ?? json['log_date']),
      eventTime: _toDateTime(json['eventTime'] ?? json['event_time']),
      userId: _toInt(json['userId'] ?? json['user_id']),
      factoryId: _toInt(json['factoryId'] ?? json['factory_id']),
      logType: (json['logType'] ?? json['log_type'] ?? '').toString(),
      severity: (json['severity'] ?? '').toString(),
      status: (json['status'] ?? '').toString(),
      command: (json['command'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'robotId': robotId,
      'logDate': _dateOnlyIso(logDate),
      'eventTime': eventTime?.toUtc().toIso8601String(),
      'userId': userId,
      'factoryId': factoryId,
      'logType': logType,
      'severity': severity,
      'status': status,
      'command': command,
    };
  }

  static int? _toInt(dynamic value) {
    if (value == null) {
      return null;
    }
    if (value is int) {
      return value;
    }
    return int.tryParse(value.toString());
  }

  static DateTime? _toDateTime(dynamic value) {
    if (value == null) {
      return null;
    }
    return DateTime.tryParse(value.toString());
  }

  static String? _dateOnlyIso(DateTime? date) {
    if (date == null) {
      return null;
    }
    final utc = date.toUtc();
    final mm = utc.month.toString().padLeft(2, '0');
    final dd = utc.day.toString().padLeft(2, '0');
    return '${utc.year}-$mm-$dd';
  }
}
