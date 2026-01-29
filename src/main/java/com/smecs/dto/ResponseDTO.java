package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseDTO<T> {
    private String status;
    private String message;
    private T data;

    public ResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
