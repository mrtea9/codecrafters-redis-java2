package configuration.common;

import configuration.Argument;

import java.util.function.Function;

public class StringArgument extends Argument<String> {

    public StringArgument(String name) {
        super(name, Function.identity());
    }

    public StringArgument(String name, String defaultValue) {
        super(name, Function.identity(), defaultValue);
    }
}
