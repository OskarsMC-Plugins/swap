package com.oskarsmc.swap.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.velocity.VelocityCaptionKeys;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class PermissionServerArgument<C> extends CommandArgument<C, RegisteredServer> {

    private PermissionServerArgument(
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                required,
                name,
                new PermissionServerArgument.ServerParser<>(),
                "",
                TypeToken.get(RegisteredServer.class),
                suggestionsProvider,
                defaultDescription,
                argumentPreprocessors
        );
    }

    public static <C> CommandArgument.@NonNull Builder<C, RegisteredServer> newBuilder(
            final @NonNull String name
    ) {
        return new PermissionServerArgument.Builder<C>(
                name
        ).withParser(
                new PermissionServerArgument.ServerParser<>()
        );
    }

    public static <C> @NonNull CommandArgument<C, RegisteredServer> of(
            final @NonNull String name
    ) {
        return cloud.commandframework.velocity.arguments.ServerArgument.<C>newBuilder(name).asRequired().build();
    }

    public static <C> @NonNull CommandArgument<C, RegisteredServer> optional(final @NonNull String name) {
        return cloud.commandframework.velocity.arguments.ServerArgument.<C>newBuilder(name).asOptional().build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, RegisteredServer> {

        private Builder(final @NonNull String name) {
            super(TypeToken.get(RegisteredServer.class), name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull RegisteredServer> build() {
            return new PermissionServerArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription(),
                    new LinkedList<>()
            );
        }

    }

    public static final class ServerParser<C> implements ArgumentParser<C, RegisteredServer> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull RegisteredServer> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        cloud.commandframework.velocity.arguments.ServerArgument.ServerParser.class,
                        commandContext
                ));
            }
            final RegisteredServer server = commandContext.<ProxyServer>get("ProxyServer").getServer(input).orElse(null);
            if (server == null) {
                return ArgumentParseResult.failure(
                        new PermissionServerArgument.ServerParseException(
                                input,
                                commandContext
                        )
                );
            }
            inputQueue.remove();
            return ArgumentParseResult.success(server);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return commandContext.<ProxyServer>get("ProxyServer")
                    .getAllServers()
                    .stream()
                    .map(s -> s.getServerInfo().getName())
                    .collect(Collectors.toList());
        }

    }

    public static final class ServerParseException extends ParserException {

        private static final long serialVersionUID = 9168156226853233788L;

        private ServerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    cloud.commandframework.velocity.arguments.ServerArgument.ServerParser.class,
                    context,
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
