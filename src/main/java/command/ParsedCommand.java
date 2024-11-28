package command;

import type.RArray;
import type.RString;

public record ParsedCommand(RArray<RString> raw, Command command) {
}
