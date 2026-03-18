import 'package:flutter/material.dart';

class RobotCard extends StatelessWidget {
  final String name, id, status;
  final int battery;

  const RobotCard({
    super.key,
    required this.name,
    required this.id,
    required this.status,
    required this.battery,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: const CircleAvatar(child: Icon(Icons.adb)),
        title: Text(name),
        subtitle: Text("ID: $id - Battery: $battery%"),
        trailing: Text(status),
      ),
    );
  }
}