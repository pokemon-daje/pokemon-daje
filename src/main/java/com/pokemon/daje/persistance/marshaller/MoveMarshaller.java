package com.pokemon.daje.persistance.marshaller;

import com.pokemon.daje.model.Move;
import com.pokemon.daje.persistance.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoveMarshaller implements
        BaseMarshaller<Move, MoveDTO>{

    private TypeMarshaller typeMarshaller;

    @Autowired
    private MoveMarshaller(TypeMarshaller typeMarshaller){
        this.typeMarshaller = typeMarshaller;
    }
    @Override
    public Move fromDTO(MoveDTO dto) {
        Move move = null;
        if (dto != null){
            move = new Move();
            move.setId(dto.getId());
            move.setName(dto.getName());
            move.setPower(dto.getPower());
            move.setType(typeMarshaller.fromDTO(dto.getType()));
        }
        return move;
    }

    @Override
    public MoveDTO toDTO(Move business) {
        MoveDTO moveDTO = null;
        if (business != null){
            moveDTO = new MoveDTO();
            moveDTO.setId(business.getId());
            moveDTO.setName(business.getName());
            moveDTO.setPower(business.getPower());
            moveDTO.setType(typeMarshaller.toDTO(business.getType()));
        }
        return moveDTO;
    }
}