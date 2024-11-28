package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RString;

public record KeysCommand(RString pattern) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        final var keys = redis.getStorage().keys();

        return new CommandResponse(keys);
    }

}
