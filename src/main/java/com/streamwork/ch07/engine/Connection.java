package com.streamwork.ch07.engine;

/**
 * A util data class for connections between components.
 */
record Connection (ComponentExecutor from, OperatorExecutor to, String channel) {}
