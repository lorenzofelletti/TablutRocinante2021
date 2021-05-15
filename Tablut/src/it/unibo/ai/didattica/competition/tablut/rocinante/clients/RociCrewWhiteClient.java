package it.unibo.ai.didattica.competition.tablut.rocinante.clients;

import java.io.IOException;
import java.net.UnknownHostException;

public class RociCrewWhiteClient {
	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] array = new String[]{"WHITE", "10", "localhost", "debug"};
        if (args.length>0){
            array = new String[]{"WHITE", args[0]};
        }
        RociCrewClient.main(array);
    }
}
