package edu.upc.essi.dtim.odin.exception;

import edu.upc.essi.dtim.odin.projects.ProjectController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalServerErrorException extends RuntimeException{

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    public InternalServerErrorException(String messageToSend, String error) {
        super(messageToSend);
        logger.error(error);
    }
}
