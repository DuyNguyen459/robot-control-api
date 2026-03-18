class Area {
  final String id;
  final String factoryId;
  final String name;
  final String description;
  final String status;

  Area({
    required this.id,
    required this.factoryId,
    required this.name,
    required this.description,
    required this.status,
  });

  factory Area.fromJson(Map<String, dynamic> json) {
    return Area(
      id: (json['areaId'] ?? json['area_id'] ?? '').toString(),
      factoryId: (json['factoryId'] ?? json['factory_id'] ?? '').toString(),
      name: (json['areaName'] ?? json['area_name'] ?? 'No Name').toString(),
      description: (json['areaDescription'] ?? json['area_description'] ?? '')
          .toString(),
      status: (json['areaStatus'] ?? json['area_status'] ?? 'Active').toString(),
    );
  }
}
