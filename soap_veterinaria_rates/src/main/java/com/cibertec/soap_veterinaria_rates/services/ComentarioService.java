package com.cibertec.soap_veterinaria_rates.services;

import com.cibertec.soap_veterinaria_rates.repository.ComentarioRepository;
import com.example.veterinaria_vitalvet.comentarios.Comentario;
import com.example.veterinaria_vitalvet.comentarios.GetComentarioResponse;
import com.example.veterinaria_vitalvet.comentarios.PostComentarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository repo;

    public GetComentarioResponse findAll() {
        GetComentarioResponse response=new GetComentarioResponse();
        response.setSalida(repo.listComentarios());
        return response;
    }
    public PostComentarioResponse save(Comentario bean) {
        PostComentarioResponse response=new PostComentarioResponse();
        response.setSalida(repo.save(bean));
        return response;
    }

}
