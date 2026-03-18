import 'app_user.dart';

class SessionIdentity {
  final String token;
  final AppUser user;

  const SessionIdentity({required this.token, required this.user});

  bool get isAdmin => user.isAdmin;
}
