import '../models/evento.dart';

abstract interface class EventoRepository {
  Future<List<Evento>> fetchPublished();
  Future<List<Evento>> searchPublished({
    String? text,
    String? categoryId,
    String? type,
    String? modality,
  });
  Future<Evento> fetchById(String id);
}
