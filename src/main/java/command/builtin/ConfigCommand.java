package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RArray;
import type.RError;
import type.RString;

public record ConfigCommand(RString action, RString key) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        if (RString.equalsIgnoreCase(action, "GET")) {
            final var property = redis.getConfiguration().option(key.content());
            if (property == null) return new CommandResponse(RArray.empty());

            return new CommandResponse(RArray.of(
                    key,
                    RString.simple(String.valueOf(property.argument(0).get()))
            ));
        }


        throw new RError("ERR unknown subcommand '%s'. TRY CONFIG HELP.".formatted(action)).asException();
    }
}
