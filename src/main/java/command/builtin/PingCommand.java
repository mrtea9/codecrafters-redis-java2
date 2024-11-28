package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RString;

public record PingCommand() implements Command {

    private static final RString PONG = new RString("PONG", false);

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        return new CommandResponse(PONG);
    }
}
