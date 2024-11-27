package type;

public record RError(RString message) implements RValue {

    private static final RError SYNTAX = new RError("ERR syntax error");

    public RError(String message) {
        this(RString.simple(message));
    }
}