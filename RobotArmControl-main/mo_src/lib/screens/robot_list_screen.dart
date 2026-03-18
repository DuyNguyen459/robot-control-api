// File: lib/screens/robot_list_screen.dart
import 'package:flutter/material.dart';
import '../widgets/robot_card.dart';

class RobotListScreen extends StatelessWidget {
  const RobotListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: const [
        Text(
          "Robots",
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        SizedBox(height: 16),
        RobotCard(name: "Robot Arm Alpha", id: "R001", status: "Online", battery: 87),
        RobotCard(name: "Robot Arm Beta", id: "R002", status: "Busy", battery: 64),
      ],
    );
  }
}