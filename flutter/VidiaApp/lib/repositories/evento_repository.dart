import '../models/evento.dart';

abstract interface class EventoRepository {
  Future<List<Evento>> fetchPublished();
  Future<Evento> fetchById(String id);
}
