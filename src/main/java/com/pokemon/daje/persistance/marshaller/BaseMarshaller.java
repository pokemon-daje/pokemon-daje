package com.pokemon.daje.persistance.marshaller;

import com.pokemon.daje.model.BusinessInterface;
import com.pokemon.daje.persistance.dto.DTOInterface;

public interface BaseMarshaller<A
        extends BusinessInterface, B extends DTOInterface> {
    A fromDTO (B dto);
    B toDTO (A business);
}
