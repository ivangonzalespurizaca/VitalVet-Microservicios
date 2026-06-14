package com.pacientes.api.services;

import java.util.List;

public interface ICRUD<T, ID> {
    T registrar(T bean) throws Exception;
    T actualizar(T bean) throws Exception;
    void eliminar(ID id) throws Exception;
    List<T> listar() throws Exception;
    T buscarPorId(ID id) throws Exception;
}
