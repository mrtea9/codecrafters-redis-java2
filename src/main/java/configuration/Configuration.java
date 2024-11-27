package configuration;

import lombok.Getter;
import lombok.experimental.Accessors;
import configuration.common.PortArgument;

import java.util.Arrays;
import java.util.List;

@Accessors
public class Configuration {

    private final @Getter Option port = new Option("port", List.of(new PortArgument(6379)));

    private final List<Option> options = Arrays.asList(port);

    public List<Option> options() {
        return options;
    }

    public Option option(String key) {
        for (final Option property : options) {
            if (property.name().equalsIgnoreCase(key)) return property;
        }

        return null;
    }

    public Option port() {
        return port;
    }
}
