package command.builtin.replication;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.RArray;
import type.ROk;
import type.RString;

import java.util.RandomAccess;

public record ReplConfCommand(RString action, RString key) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        if (RString.equalsIgnoreCase(action, "GETACK")) {
            return new CommandResponse(
                    RArray.of(
                            RString.bulk("REPLCONF"),
                            RString.bulk("ACK"),
                            RString.bulk(String.valueOf(redis.getReplicationOffset()))
                    ),
                    false
            );
        }

        return new CommandResponse(ROk.OK);
    }

    @Override
    public boolean isQueueable() {
        return false;
    }
}
