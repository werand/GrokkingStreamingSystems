package com.streamwork.ch08.engine;

/**
 * A util data class for connections between components.
 */
record Connection (ComponentExecutor from,OperatorExecutor to, String channel, String streamName){} // The name of this connection. Used by JoinOperator.