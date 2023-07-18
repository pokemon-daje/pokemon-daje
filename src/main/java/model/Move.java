package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Getter
@Setter

public class Move {

    private Integer id;
    private String name;
    private Type type;
    private Integer power;
}

