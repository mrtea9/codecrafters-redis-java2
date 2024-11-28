package command;

import command.builtin.EchoCommand;
import type.RArray;
import type.RError;
import type.RString;
import command.builtin.PingCommand;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommandParser {

    private final Map<String, BiFunction<String, List<RString>, Command>> parsers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandParser() {
        register("PING", noArgumentCommand(PingCommand::new));
        register("ECHO", singleArgumentCommand(EchoCommand::new));
    }

    public void register(String name, BiFunction<String, List<RString>, Command> parser) {
        parsers.put(name, parser);
    }

    public ParsedCommand parse(RArray<RString> arguments) {
        if (arguments.isEmpty()) throw new RError("ERR command array is empty").asException();

        final var name = arguments.items().getFirst().content().toUpperCase();

        final var parser = parsers.get(name);
        if (parser == null) throw new RError("ERR unknown '%s' command".formatted(name)).asException();

        final var command = parser.apply(name, arguments.items().subList(1, arguments.items().size()));

        return new ParsedCommand(arguments, command);
    }

    private BiFunction<String, List<RString>, Command> noArgumentCommand(Supplier<Command> constructor) {
        return (name, arguments) -> {
            if (!arguments.isEmpty()) throw wrongNumberOfArguments(name).asException();

            return constructor.get();
        };
    }

    private BiFunction<String, List<RString>, Command> singleArgumentCommand(Function<RString, Command> constructor) {
        return (name, arguments) -> {
            if (arguments.size() != 1) throw wrongNumberOfArguments(name).asException();

            final var first = arguments.getFirst();
            return constructor.apply(first);
        };
    }

    private RError wrongNumberOfArguments(String name) {
        return new RError("ERR wrong number of arguments for '%s' command".formatted(name));
    }
}
