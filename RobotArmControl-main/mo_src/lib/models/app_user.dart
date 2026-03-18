class AppUser {
  final int userId;
  final String username;
  final String email;
  final String role;
  final String? status;
  final int? factoryId;

  const AppUser({
    required this.userId,
    required this.username,
    required this.email,
    required this.role,
    this.status,
    this.factoryId,
  });

  bool get isAdmin => role.toUpperCase() == 'ADMIN';

  factory AppUser.fromJson(Map<String, dynamic> json) {
    final dynamic userIdRaw = json['userId'] ?? json['id'];
    final dynamic factoryIdRaw = json['factoryId'];

    return AppUser(
      userId: int.tryParse(userIdRaw?.toString() ?? '') ?? 0,
      username: (json['username'] ?? '').toString(),
      email: (json['email'] ?? '').toString(),
      role: (json['role'] ?? 'VIEWER').toString(),
      status: json['status']?.toString(),
      factoryId: int.tryParse(factoryIdRaw?.toString() ?? ''),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'username': username,
      'email': email,
      'role': role,
      if (status != null) 'status': status,
      if (factoryId != null) 'factoryId': factoryId,
    };
  }
}
