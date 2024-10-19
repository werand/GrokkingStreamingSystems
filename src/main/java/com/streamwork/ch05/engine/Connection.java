package com.streamwork.ch05.engine;

/**
 * A util data class for connections between components.
 */
record Connection(ComponentExecutor from, OperatorExecutor to, String channel) {}
