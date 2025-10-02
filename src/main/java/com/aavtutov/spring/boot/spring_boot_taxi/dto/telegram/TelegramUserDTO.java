package com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TelegramUserDTO {

    @JsonProperty("id") 
    private Long id;
    
    @JsonProperty("first_name")
    private String firstName;
    
    // username (optional, but helpful)
    @JsonProperty("username")
    private String username;
    
    // indicates the user is a bot (must be false)
    @JsonProperty("is_bot")
    private Boolean isBot;
}
