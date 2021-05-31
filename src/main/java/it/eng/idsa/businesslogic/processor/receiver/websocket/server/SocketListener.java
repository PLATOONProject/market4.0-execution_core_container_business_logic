package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import org.eclipse.jetty.websocket.api.Session;

public interface SocketListener {

	void onMessage(Session session, byte[] message);

	void notifyClosed(IdscpServerSocket idscpServerSocket);

}
