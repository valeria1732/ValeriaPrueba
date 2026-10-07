package com.proyecto.servicios.service;

import com.proyecto.servicios.model.usuario.LoginRequest;
import com.proyecto.servicios.model.usuario.LoginResponse;
import com.proyecto.servicios.model.usuario.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuario.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    LoginResponse login(LoginRequest request);

    UsuarioResponse agregarUsuario(UsuarioCreacionRequest request);

    List<UsuarioResponse> consultarUsuariosConFiltro(String correo, Boolean activo);
}
