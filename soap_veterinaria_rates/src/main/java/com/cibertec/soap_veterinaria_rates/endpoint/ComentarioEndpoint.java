package com.cibertec.soap_veterinaria_rates.endpoint;

import com.cibertec.soap_veterinaria_rates.services.ComentarioService;
import com.example.veterinaria_vitalvet.comentarios.GetComentarioResponse;
import com.example.veterinaria_vitalvet.comentarios.PostComentarioRequest;
import com.example.veterinaria_vitalvet.comentarios.PostComentarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@CrossOrigin(origins = "http://localhost:4200")
public class ComentarioEndpoint {

    @Autowired
    private ComentarioService service;

    private static final String NAMESPACE = "http://example.com/veterinaria-vitalvet/comentarios";

    @PayloadRoot(namespace = NAMESPACE,localPart = "getComentarioRequest")
    @ResponsePayload
    public GetComentarioResponse lista() {
        return service.findAll();
    }

    @PayloadRoot(namespace = NAMESPACE,localPart = "postComentarioRequest")
    @ResponsePayload
    public PostComentarioResponse registrar(@RequestPayload PostComentarioRequest request) {
        return service.save(request.getBean());
    }


}
