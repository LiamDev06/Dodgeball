package com.github.liamdev06.mc.sddodgeball.commands.core;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the tab complete options for a command.
 */
public class TabCompleteData {

    public static final @NonNull TabCompleteData EMPTY = new TabCompleteData();

    private final @NonNull List<TabOption> options;

    public TabCompleteData(@NonNull TabOption... options) {
        this.options = new ArrayList<>();

        Collections.addAll(this.options, options);
    }

    public @NonNull List<TabOption> getOptions() {
        return this.options;
    }
}
