package com.imcode.exceptions.factories;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.imcode.exceptions.wrappers.GeneralError;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.validation.Errors;

import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ruslan on 19.08.16.
 */
public class ErrorBuilder {

    public static GeneralError buildValidationError(Errors errors) {

        GeneralError generalError = new GeneralError();

        generalError.setErrorCode(GeneralError.VALIDATION_EC);

        int errorCount = errors.getErrorCount();
        generalError.setErrorMessage("Validation failed. " + errorCount + " error" + (errorCount > 1 ? "s" : ""));

        List<String> errorDescription = errors.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toCollection(LinkedList::new));
        generalError.setErrorDescription(errorDescription);

        return generalError;
    }

    public static GeneralError buildDatabasePersistenceError(Exception e) {

        return buildException(e, GeneralError.DATABASE_PERSISTENCE_EC);

    }

    public static GeneralError buildJsonMappingException(Exception e) {

        return buildException(e, GeneralError.JSON_MAPPING_EC);

    }

    public static GeneralError buildUncaughtException(Exception e) {

        return buildException(e, GeneralError.UNCAUGHT_EC);

    }

    private static GeneralError buildException(Exception e, int errorCode) {

        GeneralError generalError = new GeneralError();

        generalError.setErrorCode(errorCode);

        String message = null;

        switch (errorCode) {

            case GeneralError.DATABASE_PERSISTENCE_EC:
                message = "Database persistence error";
                break;

            case GeneralError.JSON_MAPPING_EC:
                message = "JSON mapping error";
                break;

            case GeneralError.UNCAUGHT_EC:
                message = "Uncaught error";
                break;

        }

        generalError.setErrorMessage(message);

        String cause = e.getCause().toString();

        List<String> errorDescription = Arrays.asList(cause);
        generalError.setErrorDescription(errorDescription);

        return generalError;

    }

}