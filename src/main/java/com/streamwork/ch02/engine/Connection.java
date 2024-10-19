package com.streamwork.ch02.engine;

/**
 * A util data class for connections between components.
 */
record Connection (ComponentExecutor from, OperatorExecutor to) {}
