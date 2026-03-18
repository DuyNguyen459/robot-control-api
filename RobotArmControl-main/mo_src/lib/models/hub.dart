class Hub {
  final String id;
  final String areaId;
  final String name;
  final String description;
  final String status;

  Hub({
    required this.id,
    required this.areaId,
    required this.name,
    required this.description,
    required this.status,
  });

  factory Hub.fromJson(Map<String, dynamic> json) {
    return Hub(
      id: (json['hubId'] ?? json['hub_id'] ?? '').toString(),
      areaId: (json['areaId'] ?? json['area_id'] ?? '').toString(),
      name: (json['hubName'] ?? json['hub_name'] ?? 'No Name').toString(),
      description: (json['hubDescription'] ?? json['hub_description'] ?? '')
          .toString(),
      status: (json['hubStatus'] ?? json['hub_status'] ?? 'Active').toString(),
    );
  }
}
