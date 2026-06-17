package com.agenda.api.services.impl;

import com.agenda.api.services.ICRUD;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class ICRUDImpl<T,ID> implements ICRUD<T, ID> {

    public abstract JpaRepository<T, ID> getRepository();

    @Override
    public T registrar(T bean) throws Exception {
        return getRepository().save(bean);
    }
    @Override
    public T actualizar(T bean) throws Exception {
        return getRepository().save(bean);
    }
    @Override
    public void eliminar(ID cod) throws Exception {
        getRepository().deleteById(cod);
    }
    @Override
    public List<T> listar() throws Exception {
        return getRepository().findAll();
    }
    @Override
    public T buscarPorId(ID cod) throws Exception {
        return getRepository().findById(cod).orElse(null);
    }

}