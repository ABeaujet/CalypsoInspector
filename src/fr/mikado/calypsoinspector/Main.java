package fr.mikado.calypsoinspector;

import fr.mikado.calypso.CalypsoCard;
import fr.mikado.calypso.CalypsoEnvironment;
import fr.mikado.calypso.CalypsoRawDump;
import fr.mikado.isodepimpl.IsoDepImpl;
import fr.mikado.xmlio.XMLIOImpl;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {

        CalypsoEnvironment env = new CalypsoEnvironment("Transpole");

        CalypsoCard passPass = new CalypsoCard(IsoDepImpl.getDefaultCard(), env);
        passPass.read();
        passPass.disconnect();
        passPass.dump(false);
        CalypsoDump.dumpProfiles(env);
        CalypsoDump.dumpContracts(env);
        CalypsoDump.dumpTrips(env);

        // save the card contents to a raw dump file :
        new CalypsoRawDump(env).writeXML(new XMLIOImpl(), "rawDumpTest.xml");

        // say you want to load a card dump :
        // loadDump()
    }

    public static void loadDump() throws Exception {
        CalypsoRawDump rawImport = new CalypsoRawDump(new XMLIOImpl(), "rawDumpTest.xml");
        CalypsoEnvironment envImport = new CalypsoEnvironment("Transpole");
        envImport.loadDump(rawImport);

        // now print the contents :
        CalypsoDump.dumpProfiles(envImport);
        CalypsoDump.dumpContracts(envImport);
        CalypsoDump.dumpTrips(envImport);
    }
}
