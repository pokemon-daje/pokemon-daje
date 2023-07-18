package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;



@AllArgsConstructor
@Getter
@Setter
public class Type {

    private Integer id;
    private String name;
    private String urlIcon;


}