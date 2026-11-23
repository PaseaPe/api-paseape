package com.paseape.apipaseape.infrastructure.controller;

import com.paseape.apipaseape.application.service.DigitalDocumentService;
import com.paseape.apipaseape.infrastructure.constant.Constant;
import com.paseape.apipaseape.infrastructure.constant.MessageCodes;
import com.paseape.apipaseape.infrastructure.constant.StatusCodes;
import com.paseape.apipaseape.infrastructure.dto.response.DigitalDocumentResDto;
import com.paseape.apipaseape.infrastructure.dto.response.DocumentUrlResDto;
import com.paseape.apipaseape.infrastructure.dto.response.ErrorDetailDto;
import com.paseape.apipaseape.infrastructure.dto.response.ResponseDto;
import com.paseape.apipaseape.infrastructure.exception.BadRequestException;
import com.paseape.apipaseape.infrastructure.shared.ApiResponse;
import com.paseape.apipaseape.infrastructure.validator.StorageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documento")
@RequiredArgsConstructor
public class DigitalDocumentController extends BaseController {

    private final DigitalDocumentService digitalDocumentService;
    private final StorageValidator storageValidator;

    @GetMapping("/{documentUuid}/descarga")
    public ResponseEntity<ResponseDto<DocumentUrlResDto>> descargarDocumento(@PathVariable String documentUuid) {
        try {
            ResponseDto<DocumentUrlResDto> response = new ResponseDto<>();

            DocumentUrlResDto fileDto = digitalDocumentService.descargarDocumento(documentUuid);

            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSG_DESCARGA_FOTO_EXITOSA);
            response.setResponse(fileDto);

            return ResponseEntity.ok(response);
        } catch (BadRequestException ex) {
            ResponseDto<DocumentUrlResDto> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            ResponseDto<DocumentUrlResDto> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al descargar documento de la mascota");
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @PostMapping(value = "/usuario/{usuarioUuid}/antecedentes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<List<DigitalDocumentResDto>>> subirAntecedentesPaseador(
            @PathVariable String usuarioUuid,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            ResponseDto<List<DigitalDocumentResDto>> response = new ResponseDto<>();
            ErrorDetailDto errorUuid = storageValidator.validarSubirAntecedentes(files, usuarioUuid);
            if (errorUuid != null) {
                response.setStatusCode(errorUuid.getStatusCode());
                response.setCode(errorUuid.getCode());
                response.setMessage(errorUuid.getMessage());
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

            List<DigitalDocumentResDto> responseList = digitalDocumentService.subirAntecedentesPaseador(usuarioUuid, files);
            response.setStatusCode(StatusCodes.Code201);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSG_SUBIDA_ANTECEDENTES_EXITOSA);
            response.setResponse(responseList);

            return ResponseEntity.status(StatusCodes.Code201).body(response);
        } catch (BadRequestException ex) {
            ResponseDto<List<DigitalDocumentResDto>> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            ResponseDto<List<DigitalDocumentResDto>> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al subir antecedentes policiales");
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }

    @DeleteMapping("/{documentUuid}")
    public ResponseEntity<ResponseDto<Void>> eliminarDocumento(@PathVariable String documentUuid) {
        try {
            ResponseDto<Void> response = new ResponseDto<>();

            digitalDocumentService.eliminarDocumento(documentUuid);
            response.setStatusCode(StatusCodes.Code200);
            response.setCode(MessageCodes.ResponseCodeS00);
            response.setMessage(Constant.MSG_ELIMINAR_DOCUMENTO_EXITOSO);
            response.setResponse(null);

            return ResponseEntity.ok(response);
        } catch (BadRequestException ex) {
            ResponseDto<Void> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code400);
            response.setCode(ex.code != null ? ex.code : MessageCodes.ResponseCodeBR01);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(StatusCodes.Code400).body(response);
        } catch (Exception ex) {
            ResponseDto<Void> response = new ResponseDto<>();
            response.setStatusCode(StatusCodes.Code500);
            response.setCode(MessageCodes.ResponseCodeE99);
            response.setMessage("Error interno al eliminar documento");
            return ResponseEntity.status(StatusCodes.Code500).body(response);
        }
    }
}