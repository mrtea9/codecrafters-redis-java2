package redis;

import client.Client;
import command.CommandResponse;
import configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import type.RArray;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Redis {

    public static void log(String message) {
        System.out.println("[%d] [%s] %s".formatted(ProcessHandle.current().pid(), LocalDateTime.now(), message));
    }

    public static void error(String message) {
        System.err.println("[%d] [%s] %s".formatted(ProcessHandle.current().pid(), LocalDateTime.now(), message));
    }

}
