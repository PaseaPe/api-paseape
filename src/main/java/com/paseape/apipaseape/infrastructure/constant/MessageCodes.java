package com.paseape.apipaseape.infrastructure.constant;

public class MessageCodes {
    private MessageCodes() {}
    // =========================================================================
    // RESPUESTAS SATISFACTORIAS (ÉXITO)
    // =========================================================================
    public static final String ResponseCodeS00 = "00"; // Operación ejecutada con éxito

    // =========================================================================
    // VALIDACIONES DE NEGOCIO Y PETICIÓN (BAD REQUEST)
    // =========================================================================
    public static final String ResponseCodeBR01 = "01"; // Parámetro obligatorio ausente o formato no válido
    public static final String ResponseCodeBR02 = "02"; // Rol de usuario inválido o no reconocido
    public static final String ResponseCodeBR03 = "03"; // Conflicto de negocio o precondición no cumplida
    public static final String ResponseCodeBR04 = "04"; // Operación denegada por estado de la entidad

    // =========================================================================
    // RECURSOS NO ENCONTRADOS (NOT FOUND)
    // =========================================================================
    public static final String ResponseCodeBR98 = "98"; // Recurso o registro no encontrado

    // =========================================================================
    // SEGURIDAD, AUTENTICACIÓN Y AUTORIZACIÓN (SECURITY / AUTH)
    // =========================================================================
    public static final String ResponseCodeE01 = "E01"; // Token de seguridad inválido, expirado o ausente

    public static final String ResponseCodeE99 = "99"; // Error interno generico
}
