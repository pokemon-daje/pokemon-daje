package com.pokemon.daje.util.marshaller.persistance;

import com.pokemon.daje.model.business_data.BusinessInterface;
import com.pokemon.daje.persistance.dto.DTOInterface;

public interface BaseMarshaller<A
        extends BusinessInterface, B extends DTOInterface> {
    A fromDTO (B dto);
    B toDTO (A business);
}
