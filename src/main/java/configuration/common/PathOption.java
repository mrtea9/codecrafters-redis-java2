package configuration.common;

import configuration.Argument;
import configuration.Option;

import java.util.List;

public class PathOption extends Option {

    public PathOption(String name) {
        super(name, List.of(new StringArgument("path")));
    }

    public Argument<String> pathArgument() {
        return argument(0, String.class);
    }
}
