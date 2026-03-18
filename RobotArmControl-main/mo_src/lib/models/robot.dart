// File: lib/models/robot.dart
import 'package:flutter/material.dart';

class Robot {
  final String name;
  final String id;
  final String status;
  final int battery;

  Robot({required this.name, required this.id, required this.status, required this.battery});

  // Map backend JSON data to this model.
  factory Robot.fromJson(Map<String, dynamic> json) {
    return Robot(
      id: json['id'] ?? 'Unknown',
      name: json['name'] ?? 'Unnamed Robot',
      status: json['status'] ?? 'Offline',
      battery: json['battery'] ?? 0,
    );
  }

  // Helper to derive the color from current status.
  Color get statusColor {
    if (status == 'Online') return Colors.green;
    if (status == 'Busy') return Colors.orange;
    return Colors.red;
  }
}