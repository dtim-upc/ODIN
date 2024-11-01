package edu.upc.essi.dtim.odin.exception;

import edu.upc.essi.dtim.odin.projects.ProjectController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @ExceptionHandler
    public ResponseEntity<Object> handleElementNotFoundException(ElementNotFoundException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleFormatNotAccepted(FormatNotAcceptedException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleEmptyFileException(EmptyFileException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }

    // IDK if there is a status code better suited for I/O operations
    @ExceptionHandler
    public ResponseEntity<Object> handleCustomIOException(CustomIOException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleInternalServerErrorException(InternalServerErrorException e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<Object> IntegrityConstraintViolation(IntegrityConstraintViolation e) {
        logger.info(e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }





    @ExceptionHandler
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException e) {
        logger.info(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
