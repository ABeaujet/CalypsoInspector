package fr.mikado.calypsoinspector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class contains all the complementary information about the network and the structure of the card.
 */
public class CalypsoEnvironment {
    private HashMap<Integer, String>    stops;
    private HashMap<Integer, String>    routes;
    private ArrayList<CalypsoFile>      cardStructure;
    private int countryId;
    private int networkId;
    //private int regionId;
    private boolean topologyConfigured;
    private boolean cardStructureConfigured;

    /**
     * Creates an empty CalypsoEnvironment
     */
    public CalypsoEnvironment(){
        this.routes = new HashMap<>();
        this.stops = new HashMap<>();
        this.cardStructure = new ArrayList<>();
        this.topologyConfigured = false;
        this.cardStructureConfigured = false;
    }

    public static Document openDocument(String filename){
        SAXBuilder sax = new SAXBuilder();
        Document doc;
        try {
            doc = sax.build(new File(filename));
        } catch (JDOMException e) {
            Logger.getGlobal().severe("Error while parsing file \""+filename+"\"");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Logger.getGlobal().severe("Cannot read file \""+filename+"\"");
            return null;
        }
        return doc;
    }

    /**
     * Loads the network topology (stops and routes) from an XML file.
     * @param topologyFile Filename of the XML file containing topology information.
     * @return Topology information properly loaded.
     * @throws Exception
     */
    public boolean setNetworkTopology(String topologyFile) throws Exception {
        Document doc = openDocument(topologyFile);
        try {
            this.parseTopologyTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
            return false;
        }
        this.topologyConfigured = true;
        return true;
    }

    private void parseTopologyTree(Document doc) throws Exception {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be a calypsoEnvironment.", new Throwable("throwable"));
        }

        this.checkCoherentEnvironment(root);

        Element topoElement = root.getChildren().get(0);
        if(!topoElement.getName().equals("topology")) {
            throw new JDOMParseException("First first level child element should be topology.", new Throwable("throwable"));
        }

        // stops
        Element stops = topoElement.getChild("stops");
        for(Element e : stops.getChildren()) {
            String name = e.getAttributeValue("name");
            int id = Integer.parseInt(e.getAttributeValue("id"));
            this.stops.put(id, name);
        }

        // routes
        Element routes = topoElement.getChild("routes");
        for(Element e : routes.getChildren()) {
            String name = e.getAttributeValue("name");
            int id = Integer.parseInt(e.getAttributeValue("id"));
            this.routes.put(id, name);
        }
    }

    /**
     * Loads the card structure (File and their structures) from an XML file.
     * @param cardStructureFile Filename of the XML file containing card structure information.
     * @return Card structure information properly loaded.
     * @throws Exception
     */
    public boolean setCardStructure(String cardStructureFile) throws Exception {
        this.cardStructure = new ArrayList<>();
        Document doc = openDocument(cardStructureFile);
        try {
            this.parseStructureTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
            return false;
        }
        this.cardStructureConfigured = true;
        return true;
    }

    // on vérifie que le fichier de topo correspond au fichier de carte.
    private boolean checkCoherentEnvironment(Element envElement) throws Exception {
        int networkId = Integer.parseInt(envElement.getAttributeValue("networkId"));
        int countryId = Integer.parseInt(envElement.getAttributeValue("countryId"));
        //int regionId  = Integer.parseInt(envElement.getAttributeValue("regionId"));

        boolean coherent = true;
        if(this.networkId != 0 && this.networkId != networkId)
            coherent =  false;
        else
            this.networkId = networkId;

        if(this.countryId != 0 && this.countryId != countryId)
            coherent = false;
        else
            this.countryId = countryId;
/*
        if(this.regionId != 0 && this.regionId != regionId)
            coherent = false;
        else
            this.regionId = regionId;
*/
        if(!coherent)
            throw new Exception("Incoherent network/country/region IDs.");

        return true;
    }

    private void parseStructureTree(Document doc) throws Exception {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be calypsoEnvironment.", new Throwable("throwable"));
        }
        //this.checkCoherentEnvironment(root);

        Element cardElement = root.getChildren().get(0);
        if(!cardElement.getName().equals("card")) {
            throw new JDOMParseException("First first level child element should be a card.", new Throwable("throwable"));
        }

        for(Element e : cardElement.getChildren()) {
            CalypsoFile nf = new CalypsoFile(e, this);
            this.cardStructure.add(nf);
            if(e.getAttributeValue("type").equals("DF"))
                for(Element ee : e.getChildren()) {
                    nf.addChild(new CalypsoFile(ee, this));
                }
        }
    }

    public int getCountryId(){
        return this.countryId;
    }
/*
    public int getRegionId(){
        return this.regionId;
    }
*/
    public int getNetworkId(){
        return this.networkId;
    }

    public ArrayList<CalypsoFile> getFiles(){
        return this.cardStructure;
    }

    public String getRouteName(int routeId){
        return (this.routes.containsKey(routeId)) ? this.routes.get(routeId) : null;
    }

    public String getStopName(int stopId){
        return (this.stops.containsKey(stopId)) ? this.stops.get(stopId) : null;
    }

    public boolean isTopologyConfigured() {
        return topologyConfigured;
    }

    public boolean isCardStructureConfigured() {
        return cardStructureConfigured;
    }
}
