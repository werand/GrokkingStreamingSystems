package com.streamwork.ch03.engine;

/**
 * A util data class for connections between components.
 */
record Connection(ComponentExecutor from, OperatorExecutor to) {}
