package com.paseape.apipaseape.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private Long id;
    private String uuid;
    private String nombres;
    private String apellidos;
    private String correo;
    private Integer correoVerificado;
    private String contrasenaHash;
    private String telefono;
    private String fotoPerfilUrl;
    private TipoUsuario tipoUsuario;
    private UsuarioEstado usuarioEstado;
    private TipoProveedorAuth tipoProveedorAuth;
    private String providerId;
    private Integer estado;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;
    private String creadoPor;
    private String actualizadoPor;
}
