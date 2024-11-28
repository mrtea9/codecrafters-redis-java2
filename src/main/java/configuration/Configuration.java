package configuration;

import configuration.common.PathOption;
import configuration.common.RemoteOption;
import configuration.common.StringArgument;
import lombok.Getter;
import lombok.experimental.Accessors;
import configuration.common.PortArgument;

import java.util.Arrays;
import java.util.List;

@Accessors(fluent = true)
public class Configuration {

    private final @Getter Option port = new Option("port", List.of(new PortArgument(6379)));
    private final @Getter PathOption directory = new PathOption("dir");
    private final @Getter PathOption databaseFilename = new PathOption("dbfilename");
    private final @Getter RemoteOption replicaOf = new RemoteOption("replicaof");
    private final @Getter Option masterReplicationId = new Option("master-replid", List.of(
            new StringArgument("id", "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb")
    ));

    private final List<Option> options = Arrays.asList(
            port,
            directory,
            databaseFilename,
            replicaOf
    );

    public List<Option> options() {
        return options;
    }

    public Option option(String key) {
        for (final Option property : options) {
            if (property.name().equalsIgnoreCase(key)) return property;
        }

        return null;
    }

    public boolean isSlave() {
        return replicaOf.hostAndPortArgument().isSet();
    }

    public Option port() {
        return port;
    }

    public PathOption directory() {
        return directory;
    }

    public PathOption databaseFilename() {
        return databaseFilename;
    }

    public Option masterReplicationId() {
        return masterReplicationId;
    }

    public RemoteOption replicaOf() {
        return replicaOf;
    }
}
