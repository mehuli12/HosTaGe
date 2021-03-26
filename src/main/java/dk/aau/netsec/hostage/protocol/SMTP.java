package dk.aau.netsec.hostage.protocol;

import java.util.ArrayList;
import java.util.List;

import dk.aau.netsec.hostage.protocol.utils.smptUtils.SmtpActionType;
import dk.aau.netsec.hostage.protocol.utils.smptUtils.SmtpRequest;
import dk.aau.netsec.hostage.protocol.utils.smptUtils.SmtpResponse;
import dk.aau.netsec.hostage.protocol.utils.smptUtils.SmtpState;
import dk.aau.netsec.hostage.wrapper.Packet;

/**
 * simple mail transfer protocol
 */
public class SMTP implements  Protocol {
    private int port = 25;

    @Override
    public int getPort() { return port; }

    @Override
    public void setPort(int port){ this.port = port;}

    public boolean isClosed() {
        return false;
    }

    public boolean isSecure() {
        return false;
    }

    public List<Packet> processMessage(Packet requestPacket) {
        List<Packet> packets = new ArrayList<>();

        SmtpState smtpState = SmtpState.CONNECT;
        SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "", smtpState);
        // Execute the connection request
        SmtpResponse smtpResponse = smtpRequest.execute();
        packets.add(sendResponse(smtpResponse));
        smtpState = smtpResponse.getNextState();
        Packet packet = prepareResponse(smtpState,requestPacket);
        // Move to next internal state
        if(packet!=null)
            packets.add(packet);

        return packets;
    }

    public TALK_FIRST whoTalksFirst() {
        return TALK_FIRST.CLIENT;
    }

    @Override
    public String toString() {
        return "SMTP";
    }

    /**
     * Prepare response from the server.
     *
     * @param smtpState the smtp state
     * @param requestPacket the packet from the client.
     * @return the Packet.
     */
    private Packet prepareResponse(SmtpState smtpState,Packet requestPacket){
        if (smtpState != SmtpState.CONNECT) {
            // Create request from client input and current state
            if(requestPacket!=null) {
                SmtpRequest request = SmtpRequest.createRequest(requestPacket.toString(), smtpState);
                // Execute request and create response object
                SmtpResponse response = request.execute();
                // Move to next internal state
                return sendResponse(response);
            }
        }
        return null;
    }

    /**
     * Send response to client.
     *
     * @param smtpResponse response object
     */
    private Packet sendResponse(SmtpResponse smtpResponse) {
        String message = smtpResponse.getMessage();

        return new Packet(message,toString());
    }


}
