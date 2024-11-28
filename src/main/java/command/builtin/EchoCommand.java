package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RString;

public record EchoCommand(RString message) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        return new CommandResponse(message);
    }
}
