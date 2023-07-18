package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.awt.*;
@Component
@AllArgsConstructor
@Getter
@Setter

public class Pokemon {

    private Integer id;
    private String name;
    private Image sprite;
    private Integer currentHP;
    private Integer maxHP;
    private Type type;
    private Move[] moves;
    private String originalTrainer;
}