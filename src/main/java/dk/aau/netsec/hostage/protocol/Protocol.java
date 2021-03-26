package dk.aau.netsec.hostage.protocol;

import java.util.List;

import dk.aau.netsec.hostage.wrapper.Packet;

/**
 * Protocol interface.
 * 
 * @author Mihai Plasoianu
 * @author Wulf Pfeiffer
 */
public interface Protocol {

	enum TALK_FIRST {
		SERVER, CLIENT
	}

    /**
	 * Returns the port on which the protocol is running.
	 * 
	 * @return The port used by the protocol (1-65535)
	 */
	int getPort();

	/**
	 * Sets the port on  the protocol.
	 *@param port The new port
	 */
	void setPort(int port);

	/**
	 * Returns whether the communication is ended and the connection should be
	 * closed or not.
	 * 
	 * @return true if the connection should be closed, false otherwise.
	 */
	boolean isClosed();

	/**
	 * Returns whether the protocol uses a secure connection or not.
	 * 
	 * @return true if SSL/TLS is used, false otherwise.
	 */
	boolean isSecure();

	/**
	 * Determines the next response.
	 * 
	 * @param requestPacket
	 *            Last message received from the client.
	 * @return Message to be sent to the client.
	 */
	List<Packet> processMessage(Packet requestPacket);

	/**
	 * Returns the name of the protocol.
	 * 
	 * @return String representation of the protocol.
	 */
	@Override
	String toString();


    /**
	 * Specifies who starts the communication once the connection is
	 * established.
	 * 
	 * @return A value in TALK_FIRST.
	 */
	TALK_FIRST whoTalksFirst();

}