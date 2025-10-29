package dataaccess;

import dataaccess.exceptions.DataAccessException;

public class UnauthorizedException extends DataAccessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
