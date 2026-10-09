package bo.uajms.eventos.modulos.usuarios.entidades;

public enum TipoEventoSolicitud {
    CONFERENCIAS_CHARLAS("Conferencias y charlas"),
    CURSOS_TALLERES("Cursos y talleres"),
    DIPLOMADOS_CAPACITACION("Diplomados y capacitación"),
    ACTIVIDADES_CULTURALES("Actividades culturales"),
    ACTIVIDADES_DEPORTIVAS("Actividades deportivas"),
    OTROS_EVENTOS_UNIVERSITARIOS("Otros eventos universitarios");
    private final String nombre;
    TipoEventoSolicitud(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
}
