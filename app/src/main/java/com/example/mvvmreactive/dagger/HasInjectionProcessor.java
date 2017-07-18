package com.example.mvvmreactive.dagger;

/**
 * Interface for allowing injections during Espresso testing.
 */
public interface HasInjectionProcessor {
    InjectionProcessor injectionProcessor();
}
