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
public class Type {

    private Integer id;
    private String name;
    private Image icon;


}