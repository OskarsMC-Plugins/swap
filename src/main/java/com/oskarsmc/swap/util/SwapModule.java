package com.oskarsmc.swap.util;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.oskarsmc.swap.Swap;
import org.bstats.velocity.Metrics;

public class SwapModule extends AbstractModule {
    private final Metrics metrics;

    @Inject
    public SwapModule(Swap swap, Metrics.Factory factory) {
        metrics = factory.make(swap, StatsUtils.PLUGIN_ID);
    }

    @Provides
    public Metrics provideMetrics() {
        return metrics;
    }
}
