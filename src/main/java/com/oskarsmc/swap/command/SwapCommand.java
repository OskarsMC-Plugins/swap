package com.oskarsmc.swap.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.velocity.VelocityCaptionKeys;
import cloud.commandframework.velocity.VelocityCommandManager;
import cloud.commandframework.velocity.arguments.ServerArgument;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SwapCommand {

    private final AtomicInteger sent = new AtomicInteger();

    @Inject
    public SwapCommand(ProxyServer proxyServer, Metrics metrics, VelocityCommandManager<CommandSource> commandManager) {
        metrics(metrics);
        commandManager.setCommandSuggestionProcessor(new CloudSuggestionProcessor());

        Command.Builder<CommandSource> serverCommand = commandManager.commandBuilder("server", "swap")
                .senderType(Player.class);

        CommandArgument.Builder<CommandSource, RegisteredServer> serverArgument = commandManager.argumentBuilder(RegisteredServer.class, "server")
                .asRequired()
                .withSuggestionsProvider((context, queue) -> {
                    ArrayList<String> suggestions = new ArrayList<>();

                    for (RegisteredServer server : proxyServer.getAllServers()) {
                        if (context.getSender().hasPermission("osmc.swap." + server.getServerInfo().getName())) suggestions.add(server.getServerInfo().getName());
                    }

                    return suggestions;
                })
                .withParser((context, inputQueue) -> {
                            String name = inputQueue.peek();
                            Optional<RegisteredServer> server = proxyServer.getServer(name);

                            if (name == null) {
                                return ArgumentParseResult.failure(new InvalidObjectException("No argument provided."));
                            }

                            if (server.isPresent()) {
                                if (context.getSender().hasPermission("osmc.swap." + server.get().getServerInfo().getName())) { // We handle permissions here as it is impossible to get context from Permission Checker
                                    inputQueue.remove();
                                    return ArgumentParseResult.success(server.get());
                                } else {
                                    return ArgumentParseResult.failure(new InvalidObjectException("You have no permission to join server " + name + "."));
                                }
                            } else {
                                return ArgumentParseResult.failure(new InvalidObjectException("Could not find that server."));
                            }
                        });

        commandManager.command(serverCommand
                .argument(serverArgument, ArgumentDescription.of("The server to join"))
                .handler(context -> {
                    ((Player) context.getSender()).createConnectionRequest(context.get("server")).fireAndForget();
                    sent.incrementAndGet();
                })
        );
    }

    private void metrics(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("servers_switched", () -> {
            int sentInt = sent.get();
            sent.set(0);
            return sentInt;
        }));
    }
}
