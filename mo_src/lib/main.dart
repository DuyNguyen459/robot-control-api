import 'package:flutter/material.dart'; // QUAN TRỌNG: Phải có dòng này!

void main() => runApp(const RobotControlApp());

class RobotControlApp extends StatelessWidget {
  const RobotControlApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primaryColor: const Color(0xFF0091FF)),
      home: const MainScreen(),
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        leading: const Icon(Icons.smart_toy, color: Colors.blue),
        title: const Text(
          "Robot Control System",
          style: TextStyle(
            color: Colors.black,
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      body: _buildRobotList(),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.teal,
        type: BottomNavigationBarType.fixed,
        onTap: (index) => setState(() => _selectedIndex = index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.smart_toy), label: 'Robots'),
          BottomNavigationBarItem(icon: Icon(Icons.list_alt), label: 'Tasks'),
          BottomNavigationBarItem(icon: Icon(Icons.map), label: 'Map'),
          BottomNavigationBarItem(
            icon: Icon(Icons.videogame_asset),
            label: 'Control',
          ),
        ],
      ),
    );
  }

  Widget _buildRobotList() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: const [
        Text(
          "Robots",
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        SizedBox(height: 16),
        RobotCard(
          name: "Robot Arm Alpha",
          id: "R001",
          status: "Online",
          battery: 87,
        ),
        RobotCard(
          name: "Robot Arm Beta",
          id: "R002",
          status: "Busy",
          battery: 64,
        ),
      ],
    );
  }
}

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
