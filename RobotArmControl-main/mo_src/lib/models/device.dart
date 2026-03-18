class Device {
  final String id;
  final String hubId;
  final String name;
  final String deviceType;
  final String robotType;
  final String model;
  final String serialNumber;
  final String connectionType;
  final String status;

  Device({
    required this.id,
    required this.hubId,
    required this.name,
    required this.deviceType,
    required this.robotType,
    required this.model,
    required this.serialNumber,
    required this.connectionType,
    required this.status,
  });

  factory Device.fromJson(Map<String, dynamic> json) {
    return Device(
      id: (json['deviceId'] ?? json['device_id'] ?? '').toString(),
      hubId: (json['hubId'] ?? json['hub_id'] ?? '').toString(),
      name: (json['deviceName'] ?? json['device_name'] ?? 'No Name').toString(),
      deviceType: (json['deviceType'] ?? json['device_type'] ?? '').toString(),
      robotType: (json['robotType'] ?? json['robot_type'] ?? '').toString(),
      model: (json['model'] ?? '').toString(),
      serialNumber: (json['serialNumber'] ?? json['serial_number'] ?? '')
          .toString(),
      connectionType: (json['connectionType'] ?? json['connection_type'] ?? '')
          .toString(),
      status:
          (json['deviceStatus'] ?? json['device_status'] ?? 'Active').toString(),
    );
  }
}
