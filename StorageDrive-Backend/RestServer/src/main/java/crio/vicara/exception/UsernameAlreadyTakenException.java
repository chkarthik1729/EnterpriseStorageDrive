package crio.vicara.exception;

import javax.management.openmbean.KeyAlreadyExistsException;

public class UsernameAlreadyTakenException extends KeyAlreadyExistsException {

    public UsernameAlreadyTakenException() {
        super();
    }

    public UsernameAlreadyTakenException(String msg) {
        super(msg);
    }
}
