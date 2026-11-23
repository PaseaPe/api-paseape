package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "usuarios")
@SQLRestriction("estado = 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36, unique = true)
    private String uuid;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    @Column(name = "correo_verificado", nullable = false)
    private Integer correoVerificado;

    @Column(name = "contrasena_hash")
    private String contrasenaHash;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @ManyToOne
    @JoinColumn(name = "tipo_usuario_id", nullable = false)
    private TipoUsuarioEntity tipoUsuario;

    @ManyToOne
    @JoinColumn(name = "usuario_estado_id", nullable = false)
    private UsuarioEstadoEntity usuarioEstado;

    @ManyToOne
    @JoinColumn(name = "tipo_proveedor_auth_id", nullable = false)
    private TipoProveedorAuthEntity tipoProveedorAuth;

    @Column(name = "provider_id", length = 100, unique = true)
    private String providerId;

    @ManyToOne
    @JoinColumn(name = "folder_digital_document_id")
    private FolderDigitalDocumentEntity folderDigitalDocument;

    @Column(name = "estado", nullable = false)
    private Integer estado;
}
