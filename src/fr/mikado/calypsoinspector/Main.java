package fr.mikado.calypsoinspector;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

public class Main {

    public static void main(String[] args) throws Exception {

        CalypsoEnvironment env = new CalypsoEnvironment("Transpole");

        CalypsoCard passPass = new CalypsoCard(getDefaultCard(), env);
        passPass.read();
        passPass.dump();
        passPass.dumpTrips();
        passPass.dumpProfiles();
        // TODO : Contracts
        //passPass.dumpContracts();
        passPass.disconnect();
    }

    public static Card getDefaultCard() throws CardException {
        CardTerminal term = null;
        try {
            term = TerminalFactory.getDefault().terminals().list().get(0);
        } catch (CardException e) {
            System.out.println("No terminal plugged... : " + e.getMessage());
        }
        if(term == null)
            throw new CardException("No terminal plugged.");

        System.out.println("Waiting for card...");
        term.waitForCardPresent(0);
        System.out.println("Card found !");

        return term.connect("T=1");
    }
}
