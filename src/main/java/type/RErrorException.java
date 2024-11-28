package type;

public class RErrorException extends RuntimeException {

    private final RError error;

    public RErrorException(RError error) {
        this.error = error;
    }

    public RError error() {
        return error;
    }
}
