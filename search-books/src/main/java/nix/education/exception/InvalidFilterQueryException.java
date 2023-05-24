package nix.education.exception;

public class InvalidFilterQueryException extends RuntimeException {

    public InvalidFilterQueryException(String queryFilter) {
        super(String.format("Invalid filter query: '%s'", queryFilter));
    }
}
