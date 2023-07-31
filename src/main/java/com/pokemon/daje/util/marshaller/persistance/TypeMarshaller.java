package com.pokemon.daje.util.marshaller.persistance;

import com.pokemon.daje.model.business_data.Type;
import com.pokemon.daje.model.business_data.TypesEnum;
import com.pokemon.daje.persistance.dto.TypeDTO;
import org.springframework.stereotype.Component;

@Component
public class TypeMarshaller implements BaseMarshaller<Type, TypeDTO>{


    @Override
    public Type fromDTO(TypeDTO dto) {
        Type type = null;
        if (dto != null){
            type = new Type();
            type.setId(dto.getPokedexId());
            type.setName(dto.getName());
            type.setImageUrl(dto.getImageUrl());
        }
        return type;
    }

    @Override
    public TypeDTO toDTO(Type business) {
        TypeDTO typeDTO = null;
        if (business != null){
            typeDTO = new TypeDTO();
            typeDTO.setPokedexId(TypesEnum.fromString(business.getName()).getId());
            typeDTO.setName(business.getName());
            typeDTO.setImageUrl(business.getImageUrl());
        }
        return typeDTO;
    }
}
