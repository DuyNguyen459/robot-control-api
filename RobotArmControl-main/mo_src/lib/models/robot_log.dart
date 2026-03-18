class RobotLogEntry {
  final String eventId;
  final int? robotId;
  final DateTime? logDate;
  final DateTime? eventTime;
  final int? sessionId;
  final String logType;
  final String severity;
  final String status;
  final String source;
  final String command;
  final String message;
  final String traceId;

  RobotLogEntry({
    required this.eventId,
    required this.robotId,
    required this.logDate,
    required this.eventTime,
    required this.sessionId,
    required this.logType,
    required this.severity,
    required this.status,
    required this.source,
    required this.command,
    required this.message,
    required this.traceId,
  });

  factory RobotLogEntry.fromJson(Map<String, dynamic> json) {
    return RobotLogEntry(
      eventId: (json['eventId'] ?? json['event_id'] ?? '').toString(),
      robotId: int.tryParse(
        (json['robotId'] ?? json['robot_id'] ?? '').toString(),
      ),
      logDate: DateTime.tryParse(
        (json['logDate'] ?? json['log_date'] ?? '').toString(),
      ),
      eventTime: DateTime.tryParse(
        (json['eventTime'] ?? json['event_time'] ?? '').toString(),
      ),
      sessionId: int.tryParse(
        (json['sessionId'] ?? json['session_id'] ?? '').toString(),
      ),
      logType: (json['logType'] ?? json['log_type'] ?? '').toString(),
      severity: (json['severity'] ?? '').toString(),
      status: (json['status'] ?? '').toString(),
      source: (json['source'] ?? '').toString(),
      command: (json['command'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      traceId: (json['traceId'] ?? json['trace_id'] ?? '').toString(),
    );
  }
}

class LatestRobotStatus {
  final int? robotId;
  final DateTime? lastEventTime;
  final int? sessionId;
  final String status;
  final String severity;
  final String source;
  final String message;
  final String traceId;

  LatestRobotStatus({
    required this.robotId,
    required this.lastEventTime,
    required this.sessionId,
    required this.status,
    required this.severity,
    required this.source,
    required this.message,
    required this.traceId,
  });

  factory LatestRobotStatus.fromJson(Map<String, dynamic> json) {
    return LatestRobotStatus(
      robotId: int.tryParse(
        (json['robotId'] ?? json['robot_id'] ?? '').toString(),
      ),
      lastEventTime: DateTime.tryParse(
        (json['lastEventTime'] ?? json['last_event_time'] ?? '').toString(),
      ),
      sessionId: int.tryParse(
        (json['sessionId'] ?? json['session_id'] ?? '').toString(),
      ),
      status: (json['status'] ?? '').toString(),
      severity: (json['severity'] ?? '').toString(),
      source: (json['source'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      traceId: (json['traceId'] ?? json['trace_id'] ?? '').toString(),
    );
  }
}
