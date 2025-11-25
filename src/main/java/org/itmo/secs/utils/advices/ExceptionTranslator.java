package org.itmo.secs.utils.advices;

import org.itmo.secs.model.dto.ErrorDto;
import org.itmo.secs.utils.exceptions.ItemNotFoundException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionTranslator {
    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDto processItemNotFoundException(ItemNotFoundException ex) {
        return new ErrorDto(ex.getMessage());
    }
}
