package com.mygame.server.domain.ports;

/** Abstraction over wall-clock time — makes time-dependent code unit-testable. */
public interface ClockPort {
    long currentTimeMs();

    /** Default implementation backed by {@link System#currentTimeMillis()}. */
    static ClockPort system() {
        return System::currentTimeMillis;
    }
}
