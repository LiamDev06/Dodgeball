package com.github.liamdev06.mc.sddodgeball.utility.autoregistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto register annotation to mark a class that should be automatically registered
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegister {

    AutoRegistry.Type type();

}
