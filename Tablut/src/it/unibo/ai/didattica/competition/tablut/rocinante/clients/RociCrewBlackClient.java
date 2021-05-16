package it.unibo.ai.didattica.competition.tablut.rocinante.clients;

import java.io.IOException;
import java.net.UnknownHostException;

public class RociCrewBlackClient {
	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] array = new String[]{"BLACK", "10", "localhost", "debug"};
        if (args.length>0){
            array = new String[]{"BLACK", args[0]};
        }
        RociCrewClient.main(array);
    }
}
