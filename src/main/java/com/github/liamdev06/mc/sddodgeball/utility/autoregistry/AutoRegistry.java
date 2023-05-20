package com.github.liamdev06.mc.sddodgeball.utility.autoregistry;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.reflections.Reflections;

import java.util.*;

/**
 * Auto register certain classes instead of having to manually initialize their constructor.
 */
public class AutoRegistry {

    private static final Map<Class<?>, Object> REGISTRY = new HashMap<>();

    /**
     * Register a class that can be automatically initialized.
     *
     * @param clazz The class to register, indicating the class that will be initialized.
     * @param parameter The class object parameter in the constructor (can be null if not applicable).
     * @param initArg The value of the constructor parameter (can be null if not applicable).
     */
    public static void register(@NonNull Class<?> clazz, @Nullable Class<?> parameter, @Nullable Object initArg) {
        try {
            if (parameter == null && initArg == null) {
                REGISTRY.put(clazz, clazz.getConstructor().newInstance());
            } else {
                REGISTRY.put(clazz, clazz.getConstructor(parameter).newInstance(initArg));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Object getInstance(@NonNull Class<?> clazz) {
        return REGISTRY.get(clazz);
    }

    /**
     * Get an array list of all classes with a certain annotation and annotation value.
     *
     * @param type The type the {@link AutoRegister} annotation needs to have to be included in the list.
     * @return All classes that have the {@link AutoRegister} annotation with the {@link AutoRegistry.Type}.
     */
    public static List<Class<?>> getClassesWithRegisterType(@NonNull Type type) {
        List<Class<?>> classes = new ArrayList<>();
        Reflections reflections = new Reflections(DodgeballPlugin.class.getPackage().getName());
        Set<Class<?>> foundClasses = reflections.getTypesAnnotatedWith(AutoRegister.class);

        for (Class<?> clazz : foundClasses) {
            if (clazz.isAnnotationPresent(AutoRegister.class)) {
                AutoRegister autoRegister = clazz.getAnnotation(AutoRegister.class);

                if (autoRegister.type() == type) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }

    /**
     * Used to categorize the different {@link AutoRegister} types there is.
     */
    public enum Type {
        COMMAND,
        LISTENER,
        RUNNABLE
    }
}