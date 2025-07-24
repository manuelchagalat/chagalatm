package com.webinar.copilot.copilot_demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {

    private String name;
    private String height;
    private String weight;
    private String baseExperience;

}
