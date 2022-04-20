package org.jumpmind.pos.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.jumpmind.pos.util.model.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.io.StringWriter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageNotWritableException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleBadRequest(Throwable ex, WebRequest request) {
        return handleErrors(ex, request);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResult handleErrors(Throwable ex, WebRequest request) {
        log.warn("A web request failed:  " + request.getDescription(true));
        if (ex instanceof PosServerException && ((PosServerException)ex).isLogMessageOnly()) {
            log.warn(ex.getMessage());
            log.debug("", ex);
        } else {
            log.warn("", ex);
        }
        String message = ex.getMessage();
        try {
            DefaultObjectMapper.defaultObjectMapper().writeValue(new StringWriter(), ex);
        } catch (Exception er) {
            log.info("The exception was not serializable, it will not be marshalled to the client");
            ex = null;
        }
        return new ErrorResult(message, ex);
    }
}
