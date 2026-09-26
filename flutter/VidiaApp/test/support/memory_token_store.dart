import 'package:vidia/services/token_store.dart';

class MemoryTokenStore implements TokenStore {
  String? token;
  String? user;

  @override
  Future<void> clear() async {
    token = null;
    user = null;
  }

  @override
  Future<String?> readToken() async => token;

  @override
  Future<String?> readUser() async => user;

  @override
  Future<void> saveSession(
      {required String token, required String user}) async {
    this.token = token;
    this.user = user;
  }
}
