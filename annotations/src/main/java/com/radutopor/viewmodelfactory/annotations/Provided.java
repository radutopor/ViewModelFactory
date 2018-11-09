package com.radutopor.viewmodelfactory.annotations;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * An annotation to be applied to parameters that should be provided by dependency injection in a generated factory.
 */
@Target(PARAMETER)
public @interface Provided {
}
