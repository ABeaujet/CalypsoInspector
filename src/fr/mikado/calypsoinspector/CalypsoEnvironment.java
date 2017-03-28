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
        this.init();
    }

    public CalypsoEnvironment(int countryCode, int networkId) throws JDOMParseException {
        this.init();
        this.loadEnvironment(countryCode, networkId);
    }

    public CalypsoEnvironment(String networkName) throws JDOMParseException {
        init();
        this.loadEnvironment(networkName);
    }

    private void init(){
        this.routes = new HashMap<>();
        this.stops = new HashMap<>();
        this.cardStructure = new ArrayList<>();
        this.topologyConfigured = false;
        this.cardStructureConfigured = false;
    }

    public void loadEnvironment(int countryCode, int networkId) throws JDOMParseException {
        // countryCode/networkID/cardstruct.xml
        // countryCode/networkID/topology.xml
        this.countryId = countryCode;
        this.networkId = networkId;

        String dir = "networks/"+countryCode+"/"+networkId+"/";
        System.out.println("Loading network topology...");
        this.setNetworkTopology(dir+"topology.xml");
        System.out.println("Loading card strcutre...");
        this.setCardStructure(dir+"cardstruct.xml");
    }

    public void loadEnvironment(String networkName) throws JDOMParseException {
        networkName = networkName.toLowerCase();
        // get countryCode & networkId from networks/networks.xml
        Document networks = openDocument("networks/networks.xml");
        if(networks == null){
            System.out.println("You broke my shit nigga. Where my networks/networks.xml ?");
            return;
        }
        Element root = networks.getRootElement();
        if(root == null){
            System.out.println("You broke my shit nigga. Where my networks/networks.xml:countries node ?");
            return;
        }
        for(Element country : root.getChildren())
            for(Element network : country.getChildren())
                if(network.getAttributeValue("name").toLowerCase().equals(networkName)){
                    int countryCode = Integer.parseInt(network.getParentElement().getAttributeValue("id"));
                    int networkId   = Integer.parseInt(network.getAttributeValue("id"));
                    System.out.println("Loading network "+countryCode+":"+networkId);
                    this.loadEnvironment(countryCode, networkId);
                    return;
                }
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
     * @throws JDOMParseException
     */
    public boolean setNetworkTopology(String topologyFile) throws JDOMParseException {
        Document doc = openDocument(topologyFile);
        if(doc == null)
            return false;

        try {
            this.topologyConfigured = this.parseTopologyTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
        }

        if(this.topologyConfigured)
            System.out.println("Topology loaded");
        else
            System.out.println("Topology NOT loaded !");

        return this.topologyConfigured;
    }

    private boolean parseTopologyTree(Document doc) throws JDOMParseException {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be a calypsoEnvironment.", new Throwable("throwable"));
        }

        if(!this.checkCoherentEnvironment(root)){
            System.out.println("Incoherent network/country/region IDs.");
            return false;
        }

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
        return true;
    }

    /**
     * Loads the card structure (File and their structures) from an XML file.
     * @param cardStructureFile Filename of the XML file containing card structure information.
     * @return Card structure information properly loaded.
     * @throws JDOMParseException
     */
    public boolean setCardStructure(String cardStructureFile) throws JDOMParseException {
        this.cardStructure = new ArrayList<>();
        Document doc = openDocument(cardStructureFile);
        if(doc == null)
            return false;

        try {
            this.cardStructureConfigured = this.parseStructureTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
        }

        if(this.cardStructureConfigured)
            System.out.println("Card strucutre loaded");
        else
            System.out.println("Card strucutre NOT loaded !");
        return this.cardStructureConfigured;
    }

    // on vérifie que le fichier de topo correspond au fichier de carte.
    private boolean checkCoherentEnvironment(Element envElement) {
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
        return coherent;

    }

    private boolean parseStructureTree(Document doc) throws JDOMParseException {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be calypsoEnvironment.", new Throwable("throwable"));
        }

        if(!this.checkCoherentEnvironment(root)){
            System.out.println("Incoherent network/country/region IDs.");
            return false;
        }

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

        return true;
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
