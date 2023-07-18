package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Pokemon {

    private Integer id;
    private String name;
    private String spriteUrl;
    private Integer currentHP;
    private Integer maxHP;
    private Type type;
    private Move[] moves;
    private String originalTrainer;
}