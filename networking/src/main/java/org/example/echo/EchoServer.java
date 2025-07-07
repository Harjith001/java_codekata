package org.example.echo;

import java.io.IOException;

/**
 *  This interface defines the contract for echo server implementation
 *  An echo server listens for client connections and sends back any messages it receives.
 *  Contract to start and stop the server methods.
 */
public interface EchoServer {
    /**
     * Starts the echo server on the specified port.
     * <p>
     * This method should bind a socket, initialize resources,
     * and begin accepting and handling client connections.
     * </p>
     *
     * @param port port in which server would listen for request
     * @throws IOException if the server fails to bind or accept connections
     */

    void start(int port) throws IOException;

    /**
     * Stops the echo server and releases any associated resources.
     * <p>
     * This may involve closing sockets, stopping threads, and cleaning up I/O.
     * </p>
     *
     * @throws IOException if an I/O error occurs during shutdown
     */
    void stop() throws IOException;
}
