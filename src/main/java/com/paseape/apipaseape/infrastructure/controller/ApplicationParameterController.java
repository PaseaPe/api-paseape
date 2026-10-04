package com.paseape.apipaseape.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paseape.apipaseape.application.service.ApplicationParameterService;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.ApplicationParameterResDto;
import com.paseape.apipaseape.infrastructure.dto.response.parameter.TipoRazaItemResDto;
import com.paseape.apipaseape.infrastructure.shared.RequestService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/application-parameters")
@RequiredArgsConstructor
public class ApplicationParameterController extends BaseController {

    private final ApplicationParameterService applicationParameterService;
    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<ResponseDto<ApplicationParameterResDto>> getAllParameters() {
        var response = new ResponseDto<ApplicationParameterResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ApplicationParameterResDto data = applicationParameterService.getAllParameters();

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Parámetros del sistema recuperados exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "getAllParameters");
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al recuperar los parámetros de la aplicación");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping("/razas/{tipoMascotaId}")
    public ResponseEntity<ResponseDto<List<TipoRazaItemResDto>>> getRazasByTipoMascota(@PathVariable Integer tipoMascotaId) {
        var response = new ResponseDto<List<TipoRazaItemResDto>>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            List<TipoRazaItemResDto> razas = applicationParameterService.getRazasByTipoMascotaId(tipoMascotaId);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Razas recuperadas exitosamente para el tipo de mascota especificado");
            response.setHeader(headerDto);
            response.setResponse(razas);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "tipoMascotaId=" + tipoMascotaId);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al recuperar las razas filtradas");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}