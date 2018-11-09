package com.radutopor.viewmodelfactory.annotations;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;

/**
 * An annotation to be applied to ViewModels for which a ViewModelProvider.Factory should be automatically generated.
 */
@Target({TYPE, CONSTRUCTOR})
public @interface ViewModelFactory {
}
