class Factory {
  final String id;
  final String name;
  final String location;
  final String status;

  Factory({
    required this.id,
    required this.name,
    required this.location,
    required this.status,
  });

  // Map JSON fields from backend response to the Flutter model.
  factory Factory.fromJson(Map<String, dynamic> json) {
    return Factory(
      id: (json['factory_id'] ?? json['factoryId'] ?? '').toString(),
      name: (json['factory_name'] ?? json['factoryName'] ?? 'No Name')
          .toString(),
      location: (json['location'] ?? '').toString(),
      status: (json['factory_status'] ?? json['factorystatus'] ?? 'Active')
          .toString(),
    );
  }
}
