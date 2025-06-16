package com.electriccharge.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String result;    // SUCCESS or ERROR
    private String message;   // success or error message
    private T data;           // return object from service class, if successful
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "Opération réussie", data);
    }
    
    public static ApiResponse<?> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }
    
    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
    
    public static ApiResponse<?> error(HttpStatus status, String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
} 