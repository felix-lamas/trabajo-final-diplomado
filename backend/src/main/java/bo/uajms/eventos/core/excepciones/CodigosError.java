package bo.uajms.eventos.core.excepciones;

/** Codigos estables consumidos por clientes web y moviles. */
public final class CodigosError {
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String AUTH_INVALID_TOKEN = "AUTH_INVALID_TOKEN";
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_INVALID_SESSION = "AUTH_INVALID_SESSION";
    public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String EMAIL_VERIFICATION_TOKEN_INVALID = "EMAIL_VERIFICATION_TOKEN_INVALID";
    public static final String EMAIL_VERIFICATION_TOKEN_EXPIRED = "EMAIL_VERIFICATION_TOKEN_EXPIRED";
    public static final String EMAIL_VERIFICATION_TOKEN_USED = "EMAIL_VERIFICATION_TOKEN_USED";
    public static final String MAIL_SERVICE_UNAVAILABLE = "MAIL_SERVICE_UNAVAILABLE";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String EVENT_INVALID_STATE = "EVENT_INVALID_STATE";
    public static final String EVENT_NOT_PUBLISHED = "EVENT_NOT_PUBLISHED";
    public static final String INSCRIPTION_REQUIRED = "INSCRIPTION_REQUIRED";
    public static final String INSCRIPTION_NOT_CONFIRMED = "INSCRIPTION_NOT_CONFIRMED";
    public static final String INSCRIPTION_DUPLICATED = "INSCRIPTION_DUPLICATED";
    public static final String INSCRIPTION_CAPACITY_FULL = "INSCRIPTION_CAPACITY_FULL";
    public static final String PAYMENT_INVALID_STATE = "PAYMENT_INVALID_STATE";
    public static final String PAYMENT_RECEIPT_REQUIRED = "PAYMENT_RECEIPT_REQUIRED";
    public static final String PAYMENT_RECEIPT_NOT_FOUND = "PAYMENT_RECEIPT_NOT_FOUND";
    public static final String QR_EXPIRED = "QR_EXPIRED";
    public static final String QR_REVOKED = "QR_REVOKED";
    public static final String QR_INVALID = "QR_INVALID";
    public static final String ATTENDANCE_DUPLICATED = "ATTENDANCE_DUPLICATED";
    public static final String ATTENDANCE_OUTSIDE_RADIUS = "ATTENDANCE_OUTSIDE_RADIUS";
    public static final String ATTENDANCE_GPS_INVALID = "ATTENDANCE_GPS_INVALID";
    public static final String ATTENDANCE_GPS_ACCURACY_INVALID = "ATTENDANCE_GPS_ACCURACY_INVALID";
    public static final String ATTENDANCE_SESSION_INACTIVE = "ATTENDANCE_SESSION_INACTIVE";
    public static final String ATTENDANCE_SESSION_NOT_REQUIRED = "ATTENDANCE_SESSION_NOT_REQUIRED";
    public static final String ATTENDANCE_OUTSIDE_WINDOW = "ATTENDANCE_OUTSIDE_WINDOW";
    public static final String CERTIFICATE_NOT_AVAILABLE = "CERTIFICATE_NOT_AVAILABLE";
    public static final String PASSWORD_INVALID = "PASSWORD_INVALID";
    public static final String PASSWORD_RESET_TOKEN_INVALID = "PASSWORD_RESET_TOKEN_INVALID";
    public static final String PASSWORD_RESET_TOKEN_EXPIRED = "PASSWORD_RESET_TOKEN_EXPIRED";
    public static final String PASSWORD_RESET_TOKEN_USED = "PASSWORD_RESET_TOKEN_USED";

    private CodigosError() {}
}
