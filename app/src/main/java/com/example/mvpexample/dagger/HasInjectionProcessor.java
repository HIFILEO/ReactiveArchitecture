package com.example.mvpexample.dagger;

/**
 * Interface for allowing injections during Espresso testing.
 */
public interface HasInjectionProcessor {
    InjectionProcessor injectionProcessor();
}
