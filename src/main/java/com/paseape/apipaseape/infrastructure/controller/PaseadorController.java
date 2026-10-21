package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.repository.IClienteRepository;
import com.paseape.apipaseape.application.repository.IUsuarioRepository;
import com.paseape.apipaseape.application.service.DisponibilidadPaseadorService;
import com.paseape.apipaseape.application.service.PaseadorService;
import com.paseape.apipaseape.domain.entity.Cliente;
import com.paseape.apipaseape.domain.entity.Usuario;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.request.CambiarDistritoCoberturaReqDto;
import com.paseape.apipaseape.infrastructure.dto.response.DisponibilidadPaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.HeaderDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorDetalleResDto;
import com.paseape.apipaseape.infrastructure.dto.response.PaseadorResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.dto.response.UsuarioResDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.RequestService;
import com.paseape.apipaseape.infrastructure.validator.PaseadorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/paseadores")
@RequiredArgsConstructor
public class PaseadorController extends BaseController {

    private final PaseadorService paseadorService;
    private final DisponibilidadPaseadorService disponibilidadPaseadorService;
    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final PaseadorValidator paseadorValidator;
    private final RequestService requestService;

    @PutMapping("/distrito-cobertura")
    public ResponseEntity<ResponseDto<PaseadorResDto>> cambiarDistritoCobertura(@RequestBody CambiarDistritoCoberturaReqDto request) {
        var response = new ResponseDto<PaseadorResDto>();
        try {
            Date startDatetime = Calendar.getInstance().getTime();

            ErrorDetailDto validationError = paseadorValidator.validateCambiarDistritoCobertura(request);
            if (validationError != null) {
                response.setStatusCode(validationError.getStatusCode());
                response.setCode(validationError.getCode());
                response.setMessage(validationError.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            PaseadorResDto paseadorResDto = paseadorService.cambiarDistritoCobertura(request);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Distrito de cobertura actualizado exitosamente");
            response.setHeader(headerDto);
            response.setResponse(paseadorResDto);
            return ResponseEntity.status(response.getStatusCode()).body(response);
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
            response.setMessage("Error interno al actualizar el distrito de cobertura");
            response.setHeader(requestService.getResponseHeader(Calendar.getInstance().getTime(), endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ResponseDto<PaseadorDetalleResDto>> obtenerPerfil(
            @PathVariable String uuid,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        var response = new ResponseDto<PaseadorDetalleResDto>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            UsuarioResDto perfil = paseadorService.obtenerPaseador(uuid);
            DisponibilidadPaseadorResDto disponibilidad = disponibilidadPaseadorService.obtenerDisponibilidadPorPaseadorUuidYFecha(uuid, fecha);

            PaseadorDetalleResDto data = PaseadorDetalleResDto.builder()
                    .usuario(perfil)
                    .disponibilidad(disponibilidad)
                    .build();

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Perfil del paseador obtenido exitosamente");
            response.setHeader(headerDto);
            response.setResponse(data);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (BadRequestException ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid + ", fecha=" + fecha);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "uuid=" + uuid + ", fecha=" + fecha);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el perfil del paseador");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<PaseadorResDto>>> obtenerPaseadores(
            Principal principal,
            @RequestParam(name = "distrito_ids", required = false) List<Integer> distritoIds,
            @RequestParam(name = "distrito_id", required = false) Integer distritoId
    ) {
        var response = new ResponseDto<List<PaseadorResDto>>();
        Date startDatetime = Calendar.getInstance().getTime();
        try {
            List<Integer> distritosAFiltrar = null;
            if (distritoIds != null && !distritoIds.isEmpty()) {
                distritosAFiltrar = distritoIds.stream().filter(Objects::nonNull).toList();
            } else if (distritoId != null) {
                distritosAFiltrar = List.of(distritoId);
            } else {
                Integer distritoCliente = resolverDistritoCliente(principal);
                if (distritoCliente != null) {
                    distritosAFiltrar = List.of(distritoCliente);
                }
            }

            List<PaseadorResDto> paseadorResDtos = paseadorService.obtenerPaseadores(distritosAFiltrar);

            Date endDatetime = Calendar.getInstance().getTime();
            HeaderDto headerDto = requestService.getResponseHeader(startDatetime, endDatetime);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage("Paseadores obtenidos exitosamente");
            response.setHeader(headerDto);
            response.setResponse(paseadorResDtos);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception ex) {
            String method = new Object() {}.getClass().getEnclosingMethod().getName();
            logControllerError(logger, ex, this, method, "distritoIds=" + distritoIds);
            Date endDatetime = Calendar.getInstance().getTime();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al obtener el paseador");
            response.setHeader(requestService.getResponseHeader(startDatetime, endDatetime));
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    private Integer resolverDistritoCliente(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByCorreo(principal.getName().trim().toLowerCase());
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente != null && cliente.getDistrito() != null) {
            return cliente.getDistrito().getId();
        }
        return null;
    }

}
