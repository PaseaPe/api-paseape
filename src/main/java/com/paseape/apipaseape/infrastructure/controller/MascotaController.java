package com.paseape.apipaseape.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paseape.apipaseape.application.service.MascotaService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.RegistrarMascotaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.MascotaResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.MascotaValidator;

import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/mascotas")
@RequiredArgsConstructor
public class MascotaController extends BaseController {

    private final MascotaService mascotaService;
    private final MascotaValidator mascotaValidator;
    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<ResponseDto<MascotaResDto>> registrarMascota(@RequestBody RegistrarMascotaReqDto request) {
        var response = new ResponseDto<MascotaResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = mascotaValidator.validateRegistroUnitario(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            MascotaResDto mascotaCreada = mascotaService.registrarMascotaUnitaria(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Mascota registrada exitosamente para el cliente");
            response.setHeader(headerDto);
            response.setResponse(mascotaCreada);
            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "usuarioUuid=" + (request != null ? request.getUsuarioUuid() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "usuarioUuid=" + (request != null ? request.getUsuarioUuid() : "null"));
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al registrar la mascota");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}