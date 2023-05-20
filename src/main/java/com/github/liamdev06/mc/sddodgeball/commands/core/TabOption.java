package com.github.liamdev06.mc.sddodgeball.commands.core;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A certain tab complete option which stores the position and the options.
 */
public class TabOption {

    private final int positionOnArgument;
    private final @NonNull List<String> options;

    public TabOption(int argPos, @NonNull String... options) {
        this.positionOnArgument = argPos;
        this.options = new ArrayList<>();

        Collections.addAll(this.options, options);
    }

    public int getPositionOnArgument() {
        return this.positionOnArgument;
    }

    public @NonNull List<String> getOptions() {
        return this.options;
    }
}
