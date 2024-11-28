package command.builtin;

import client.Client;
import command.Command;
import command.CommandResponse;
import redis.Redis;
import type.ROk;
import type.RString;

import java.time.Duration;
import java.util.Optional;

public record SetCommand(RString key, RString value, Optional<Duration> expiration) implements Command {

    @Override
    public CommandResponse execute(Redis redis, Client client) {
        if (expiration.isPresent()) {
            redis.getStorage().set(key, value, expiration.get());
        } else {
            redis.getStorage().set(key, value);
        }

        return new CommandResponse(ROk.OK);
    }

    @Override
    public boolean isPropagatable() {
        return true;
    }
}
