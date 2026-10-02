package com.paseape.apipaseape.infrastructure.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected void logControllerError(Logger log, Exception ex, Object controllerInstance, String methodName, String params) {
        log.error("ERROR EN CONTROLADOR | Clase: {} | Metodo: {} | Parametros: {} | Causa: {}",
                controllerInstance.getClass().getSimpleName(),
                methodName,
                params,
                ex.getMessage(),
                ex);
    }
}
