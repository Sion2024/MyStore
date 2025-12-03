package com.softuni.finalexam.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle built-in Spring exception: IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.error("Illegal argument exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Невалиден аргумент: " + ex.getMessage());
        model.addAttribute("errorTitle", "Грешка при валидация");
        return "error";
    }

    // Handle built-in Spring exception: MethodArgumentNotValidException (for @RequestBody validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, Model model) {
        log.error("Validation exception: {}", ex.getMessage(), ex);
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null 
                                ? fieldError.getDefaultMessage() 
                                : "Invalid value",
                        (existing, replacement) -> existing
                ));
        
        String errorDetails = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        model.addAttribute("errorMessage", "Грешка при валидация на данните: " + errorDetails);
        model.addAttribute("errorTitle", "Грешка при валидация");
        model.addAttribute("fieldErrors", fieldErrors);
        return "error";
    }

    // Handle built-in Spring exception: BindException (for @ModelAttribute validation)
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBindException(BindException ex, Model model) {
        log.error("Binding exception: {}", ex.getMessage(), ex);
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage() != null 
                    ? error.getDefaultMessage() 
                    : "Invalid value");
        });
        
        String errorDetails = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        model.addAttribute("errorMessage", "Грешка при валидация на данните: " + errorDetails);
        model.addAttribute("errorTitle", "Грешка при валидация");
        model.addAttribute("fieldErrors", fieldErrors);
        return "error";
    }

    // Handle built-in Spring exception: ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException ex, Model model) {
        log.error("Constraint violation exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Нарушение на ограничения: " + ex.getMessage());
        model.addAttribute("errorTitle", "Грешка при валидация");
        return "error";
    }

    // Handle custom exception: UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex, Model model) {
        log.error("User not found: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Потребител не е намерен");
        return "error";
    }

    // Handle custom exception: UserAlreadyExistsException
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException ex, Model model) {
        log.error("User already exists: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Потребител вече съществува");
        return "error";
    }

    // Handle custom exception: OrderNotFoundException
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleOrderNotFoundException(OrderNotFoundException ex, Model model) {
        log.error("Order not found: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Поръчка не е намерена");
        return "error";
    }

    // Handle custom exception: InsufficientStockException
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientStockException(InsufficientStockException ex, Model model) {
        log.error("Insufficient stock: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Недостатъчна наличност");
        return "error";
    }

    // Handle generic RuntimeException as fallback
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        log.error("Unexpected runtime exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Възникна неочаквана грешка. Моля, опитайте отново.");
        model.addAttribute("errorTitle", "Грешка");
        return "error";
    }

    // Handle 404 Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandlerFoundException(NoHandlerFoundException ex, Model model) {
        log.error("No handler found for: {}", ex.getRequestURL(), ex);
        model.addAttribute("errorMessage", "Страницата не е намерена");
        model.addAttribute("errorTitle", "404 - Страницата не е намерена");
        return "error";
    }
}

