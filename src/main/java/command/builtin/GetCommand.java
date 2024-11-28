package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RNil;
import type.RString;
import type.RValue;

public record GetCommand(RString key) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        final var value = redis.getStorage().get(key);

        if (value == null) return new CommandResponse(RNil.BULK);

        return new CommandResponse((RValue) value);
    }
}
