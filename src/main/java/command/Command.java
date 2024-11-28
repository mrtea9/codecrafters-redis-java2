package command;

import client.Client;
import redis.Redis;

public interface Command {

    CommandResponse execute(Redis redis, Client client);

}
