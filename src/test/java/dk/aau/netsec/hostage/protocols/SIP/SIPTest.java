package dk.aau.netsec.hostage.protocols.SIP;

import org.junit.Test;


import dk.aau.netsec.hostage.protocol.SIP;
import dk.aau.netsec.hostage.wrapper.Packet;

import static org.junit.Assert.assertEquals;

public class SIPTest {
    @Test
    public void testRegister(){
        SIP protocol = new SIP();
        String payload = "SIP/2.0"+"\n"+"REGISTER";
        Packet packet = new Packet(payload,"SIP");
        String actualPacket = protocol.processMessage(packet).get(0).toString();
        assertEquals(protocol.processMessage(packet).size(),1);
        assertEquals(protocol.processMessage(packet).get(0).toString(),actualPacket);
    }
}
