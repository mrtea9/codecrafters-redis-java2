package command.builtin.replication;

import client.Client;
import client.SocketClient;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RError;

public record PSyncCommand() implements Command {

    private static final RError COULD_NOT_ENABLE = new RError("ERR could not enable replica");

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        return null;
    }
}
