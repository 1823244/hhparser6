package com.ens.hhparser5.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private long id;
    private String name;
    private long userId;

    public Project(long id, String name){
        this.id = id;
        this.name = name;
    }

}
