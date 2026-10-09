class OrganizerEventType {
  const OrganizerEventType(this.code, this.name);
  final String code;
  final String name;
  factory OrganizerEventType.fromJson(Map<String, dynamic> json) =>
      OrganizerEventType(json['codigo'] as String, json['nombre'] as String);
}

class OrganizerApplication {
  const OrganizerApplication({
    required this.state,
    required this.canRequest,
    this.reason,
    this.rejectionReason,
    this.additionalInformation,
    this.eventTypes = const [],
    this.eventTypeNames = const [],
  });
  final String state;
  final bool canRequest;
  final String? reason;
  final String? rejectionReason;
  final String? additionalInformation;
  final List<String> eventTypes;
  final List<String> eventTypeNames;
  factory OrganizerApplication.fromJson(Map<String, dynamic> json) =>
      OrganizerApplication(
        state: json['estado'] as String,
        canRequest: json['puedeSolicitar'] == true,
        reason: json['motivoSolicitud'] as String?,
        rejectionReason: json['motivoRechazo'] as String?,
        additionalInformation: json['informacionAdicional'] as String?,
        eventTypes: List<String>.from(json['tiposEventos'] ?? []),
        eventTypeNames: List<String>.from(json['nombresTiposEventos'] ?? []),
      );
}
