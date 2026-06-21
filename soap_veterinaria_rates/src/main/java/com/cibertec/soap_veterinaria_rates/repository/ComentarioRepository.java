package com.cibertec.soap_veterinaria_rates.repository;

import com.example.veterinaria_vitalvet.comentarios.Comentario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ComentarioRepository {

    @Autowired
    JdbcTemplate template;


    public List<Comentario> listComentarios(){
        String sql="select * from feedback";

        List<Comentario> items = template.query(sql,
                    (result,rowNum)->new Comentario(result.getInt(1),result.getString(2),result.getString(3),
                        result.getInt(4)));
        return items;
    }
    public int save(Comentario bean) {
        return template.update("insert into feedback values(null,?,?,?)",bean.getNombreVeterinario(),bean.getOpinion(),bean.getEstrellas());
    }



}
