package com.paseape.apipaseape.infrastructure.constant;

public class Constant {

    //TIPO DE USUARIO
    public static final int ID_CLIENTE = 1;
    public static final int ID_PASEADOR = 2;
    public static final int ID_ADMINISTRADOR = 3;

    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ROL_PASEADOR = "PASEADOR";
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    //USUARIO ESTADO
    public static final int ID_ACTIVO = 1;
    public static final int ID_INACTIVO = 2;
    public static final int ID_PENDIENTE_ACTIVACION = 3;
    public static final int ID_SUSPENDIDO = 4;
    public static final int ID_BLOQUEADO = 5;

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";
    public static final String ESTADO_PENDIENTE_ACTIVACION = "PENDIENTE_ACTIVACION";
    public static final String ESTADO_SUSPENDIDO = "SUSPENDIDO";
    public static final String ESTADO_BLOQUEADO = "BLOQUEADO";

    //TIPO PROVEEDOR AUTH
    public static final int ID_LOCAL = 1;
    public static final int ID_GOOGLE = 2;

    public static final String PROVEEDOR_LOCAL = "LOCAL";
    public static final String PROVEEDOR_GOOGLE = "GOOGLE";

    //TIPO DOCUMENTO
    public static final int ID_DNI = 1;
    public static final int ID_CARNET_EXTRANJERIA = 2;
    public static final int ID_PASAPORTE = 3;

    public static final String DOC_DNI = "DNI";
    public static final String DOC_CARNET_EXTRANJERIA = "CARNET_EXTRANJERIA";
    public static final String DOC_PASAPORTE = "PASAPORTE";

    public static final int LONGITUD_DNI = 8;

    //PASEADOR ESTADO VERIFICACION
    public static final int ID_PENDIENTE = 1;
    public static final int ID_EN_REVISION = 2;
    public static final int ID_APROBADO = 3;
    public static final int ID_RECHAZADO = 4;
    public static final int ID_OBSERVADO = 5;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EN_REVISION = "EN_REVISION";
    public static final String ESTADO_APROBADO = "APROBADO";
    public static final String ESTADO_RECHAZADO = "RECHAZADO";
    public static final String ESTADO_OBSERVADO = "OBSERVADO";

    //DISTRITO LIMA
    public static final int ID_LIMA = 1;
    public static final int ID_BARRANCO = 4;
    public static final int ID_JESUS_MARIA = 13;
    public static final int ID_LA_MOLINA = 14;
    public static final int ID_LINCE = 16;
    public static final int ID_MAGDALENA_DEL_MAR = 20;
    public static final int ID_MIRAFLORES = 21;
    public static final int ID_PUEBLO_LIBRE = 24;
    public static final int ID_SAN_BORJA = 30;
    public static final int ID_SAN_ISIDRO = 31;
    public static final int ID_SAN_MIGUEL = 36;
    public static final int ID_SANTIAGO_DE_SURCO = 40;
    public static final int ID_SURQUILLO = 41;

    public static final String UBIGEO_LIMA = "150101";
    public static final String UBIGEO_BARRANCO = "150104";
    public static final String UBIGEO_JESUS_MARIA = "150113";
    public static final String UBIGEO_LA_MOLINA = "150114";
    public static final String UBIGEO_LINCE = "150116";
    public static final String UBIGEO_MAGDALENA_DEL_MAR = "150120";
    public static final String UBIGEO_MIRAFLORES = "150121";
    public static final String UBIGEO_PUEBLO_LIBRE = "150124";
    public static final String UBIGEO_SAN_BORJA = "150130";
    public static final String UBIGEO_SAN_ISIDRO = "150131";
    public static final String UBIGEO_SAN_MIGUEL = "150136";
    public static final String UBIGEO_SANTIAGO_DE_SURCO = "150140";
    public static final String UBIGEO_SURQUILLO = "150141";

    //TIPO MASCOTA
    public static final int ID_PERRO = 1;
    public static final int ID_GATO = 2;

    public static final String MASCOTA_PERRO = "PERRO";
    public static final String MASCOTA_GATO = "GATO";

    //TIPO TAMAÑO MASCOTA
    public static final int ID_TOY = 1;
    public static final int ID_PEQUENO = 2;
    public static final int ID_MEDIANO = 3;
    public static final int ID_GRANDE = 4;
    public static final int ID_GIGANTE = 5;

    //TIPO NIVEL ENERGIA
    public static final int ID_BAJO = 1;
    public static final int ID_MODERADO = 2;
    public static final int ID_ALTO = 3;
    public static final int ID_MUY_ALTO = 4;

    //TIPO GENERO MASCOTA
    public static final int ID_MACHO = 1;
    public static final int ID_HEMBRA = 2;

    //TIPO RAZA
    // Caninos
    public static final int ID_CANINO_MESTIZO = 1;
    public static final int ID_PERRO_SIN_PELO = 2;
    public static final int ID_LABRADOR_RETRIEVER = 3;
    public static final int ID_GOLDEN_RETRIEVER = 4;
    public static final int ID_PASTOR_ALEMAN = 5;
    public static final int ID_BULLDOG_FRANCES = 6;
    public static final int ID_BULLDOG_INGLES = 7;
    public static final int ID_BEAGLE = 8;
    public static final int ID_POODLE = 9;
    public static final int ID_SCHNAUZER = 10;
    public static final int ID_PUG = 11;
    public static final int ID_BOXER = 12;
    public static final int ID_ROTTWEILER = 13;
    public static final int ID_SIBERIAN_HUSKY = 14;
    public static final int ID_BORDER_COLLIE = 15;
    public static final int ID_SHIH_TZU = 16;
    public static final int ID_YORKSHIRE_TERRIER = 17;
    public static final int ID_CHIHUAHUA = 18;
    public static final int ID_DACHSHUND = 19;
    public static final int ID_AMERICAN_PIT_BULL_TERRIER = 20;
    public static final int ID_AMERICAN_BULLY = 21;
    public static final int ID_COCKER_SPANIEL = 22;
    public static final int ID_DOBERMAN = 23;
    public static final int ID_SAMOYEDO = 24;
    public static final int ID_CHOW_CHOW = 25;
    public static final int ID_JACK_RUSSELL_TERRIER = 26;
    public static final int ID_BULL_TERRIER = 27;
    public static final int ID_DALMATA = 28;
    public static final int ID_OTRA_RAZA_CANINA = 29;

    // Felinos
    public static final int ID_FELINO_MESTIZO = 30;
    public static final int ID_SIAMES = 31;
    public static final int ID_PERSA = 32;
    public static final int ID_MAINE_COON = 33;
    public static final int ID_BENGALA = 34;
    public static final int ID_ANGORA_TURCO = 35;
    public static final int ID_RAGDOLL = 36;
    public static final int ID_SPHYNX = 37;
    public static final int ID_BRITISH_SHORTHAIR = 38;
    public static final int ID_AZUL_RUSO = 39;
    public static final int ID_OTRA_RAZA_FELINA = 40;

    //BREVO
    public static final String HEADER_API_KEY = "api-key";
    public static final String NOTIF_RECUPERACION_CONTRASENA = "RECUPERACION_CONTRASENA";
    public static final String ASUNTO_RECUPERACION_CONTRASENA = "PaseaPe - Recuperación de Contraseña";
    public static final String PURPOSE_CLAIM = "purpose";
    public static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final long EXPIRATION_RESET_TOKEN_MS = 900000L; // 15 minutos en milisegundos

    //ESTADO LOGICO
    public static final int ESTADO_LOGICO_ACTIVO = 1;
    public static final int ESTADO_LOGICO_INACTIVO = 0;

    public static final String MENSAJE_RECUPERACION_GENERICO = "Si el correo se encuentra registrado en PaseaPe, recibirás un mensaje con las instrucciones.";
}
