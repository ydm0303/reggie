package com.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(annotations = {RestControllerAdvice.class})
@Slf4j
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception ex){
        log.error(ex.getMessage());
        return R.error("失败了！");
    }
}
