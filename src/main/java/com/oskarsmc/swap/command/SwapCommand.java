package com.oskarsmc.swap.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import com.google.inject.Inject;
import com.oskarsmc.swap.Swap;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Optional;

public class SwapCommand {
    @Inject
    private ProxyServer proxyServer;

    public SwapCommand(Swap plugin) {
        Command.Builder<CommandSource> serverCommand = plugin.commandManager.commandBuilder("server", "swap")
                .senderType(Player.class);

        CommandArgument.Builder<CommandSource, RegisteredServer> serverArgument = plugin.commandManager.argumentBuilder(RegisteredServer.class, "server")
                .asRequired()
                .withSuggestionsProvider((context, queue) -> {
                    ArrayList<String> suggestions = new ArrayList<String>();

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
                                    return ArgumentParseResult.success(server.get());
                                } else {
                                    return ArgumentParseResult.failure(new InvalidObjectException("You have no permission to join server " + name + "."));
                                }
                            } else {
                                return ArgumentParseResult.failure(new InvalidObjectException("Could not find that server"));
                            }
                        });

        plugin.commandManager.command(serverCommand
                .argument(serverArgument, ArgumentDescription.of("The server to join"))
                .handler(context -> {
                    ((Player) context.getSender()).createConnectionRequest(context.get("server")).connect();
                })
        );
    }
}
