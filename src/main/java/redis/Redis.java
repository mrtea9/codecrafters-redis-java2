package redis;

import client.Client;
import client.SocketClient;
import command.CommandParser;
import command.CommandResponse;
import command.ParsedCommand;
import configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import store.Storage;
import type.RArray;
import type.RError;
import type.RErrorException;
import type.RString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class Redis {

    private final @Getter Storage storage;
    private final @Getter Configuration configuration;
    private final @Getter List<SocketClient> replicas = Collections.synchronizedList(new ArrayList<>());
    private @Getter AtomicLong replicationOffset = new AtomicLong();
    private final CommandParser commandParser = new CommandParser();

    public Redis(Storage storage, Configuration configuration) {
        this.storage = storage;
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    public CommandResponse evaluate(Client client, Object value, long read) {
        try {

            if (value instanceof RArray array) {
                return execute(client, (RArray<RString>) array);
            }

            return new CommandResponse(new RError("ERR command be sent in an array"));
        } finally {
            log("da");
        }
    }

    private CommandResponse execute(Client client, RArray<RString> arguments) {
        try {
            final var command = commandParser.parse(arguments);

            return doExecute(client, command);
        } catch (RErrorException exception) {
            return new CommandResponse(new RString("error", false));
        }
    }

    public CommandResponse execute(Client client, ParsedCommand command) {
        try {
            return doExecute(client, command);
        } catch (RErrorException exception) {
            return new CommandResponse(new RString("error", false));
        }
    }

    private CommandResponse doExecute(Client client, ParsedCommand parsedCommand) {
        final var command = parsedCommand.command();

        final var response = command.execute(this, client);

        return response;
    }

    public String getMasterReplicationId() {
        return configuration.masterReplicationId().argument(0, String.class).get();
    }

    public static void log(String message) {
        System.out.println("[%d] [%s] %s".formatted(ProcessHandle.current().pid(), LocalDateTime.now(), message));
    }

    public static void error(String message) {
        System.err.println("[%d] [%s] %s".formatted(ProcessHandle.current().pid(), LocalDateTime.now(), message));
    }

    public Storage getStorage() {
        return storage;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public AtomicLong getReplicationOffset() {
        return replicationOffset;
    }

    public List<SocketClient> getReplicas() {
        return replicas;
    }
}
