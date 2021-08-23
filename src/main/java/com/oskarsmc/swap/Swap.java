package com.oskarsmc.swap;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.CloudInjectionModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.oskarsmc.swap.command.SwapCommand;
import com.oskarsmc.swap.util.SwapModule;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;

import java.util.function.Function;

public class Swap {
    @Inject
    private Injector injector;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final Injector childInjector = injector.createChildInjector(
                new CloudInjectionModule<>(
                        CommandSource.class,
                        CommandExecutionCoordinator.simpleCoordinator(),
                        Function.identity(),
                        Function.identity()
                ),
                injector.getInstance(SwapModule.class)
        );

        childInjector.getInstance(SwapCommand.class);
    }
}
