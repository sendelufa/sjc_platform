package ru.sendel.sjctaskschecker.controller.advice;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>>  noSuchElement(NoSuchElementException noSuchElementException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "element not found"));
    }

}
