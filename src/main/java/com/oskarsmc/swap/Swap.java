package com.oskarsmc.swap;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.CloudInjectionModule;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.oskarsmc.swap.command.SwapCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.function.Function;

public class Swap {
    @Inject
    private Injector injector;

    public VelocityCommandManager<CommandSource> commandManager;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final Injector childInjector = injector.createChildInjector(
                new CloudInjectionModule<>(
                        CommandSource.class,
                        CommandExecutionCoordinator.simpleCoordinator(),
                        Function.identity(),
                        Function.identity()
                )
        );

        this.commandManager = childInjector.getInstance(
                Key.get(new TypeLiteral<VelocityCommandManager<CommandSource>>() {
                })
        );

        injector.injectMembers(new SwapCommand(this));
    }
}
