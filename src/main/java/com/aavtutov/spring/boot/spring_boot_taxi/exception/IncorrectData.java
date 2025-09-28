package com.aavtutov.spring.boot.spring_boot_taxi.exception;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IncorrectData {

	private String error;
	private int status;
	private String path;
	private Instant timestamp;

}
