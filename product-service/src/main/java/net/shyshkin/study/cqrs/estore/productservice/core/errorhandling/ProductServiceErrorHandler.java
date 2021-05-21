package net.shyshkin.study.cqrs.estore.productservice.core.errorhandling;

import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProductServiceErrorHandler {

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalStateException(Exception ex) {

        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Object> handleOtherExceptions(Exception ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler({CommandExecutionException.class})
    public ResponseEntity<Object> handleCommandExecutionException(CommandExecutionException ex) {

        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
