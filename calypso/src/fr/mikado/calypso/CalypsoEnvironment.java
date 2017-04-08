package fr.mikado.calypso;

import fr.mikado.xmlio.XMLIOImpl;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class contains all the complementary information about the network and the structure of the card.
 */
public class CalypsoEnvironment {

    private class NetworkListItem{
        public String networkName;
        public int countryCode;
        public int networkId;
        public NetworkListItem(String networkName, int countryCode, int networkId){
            this.networkName = networkName;
            this.countryCode = countryCode;
            this.networkId = networkId;
        }
    }

    private HashMap<Integer, String>    fares;
    private HashMap<Integer, String>    profiles;
    private HashMap<Integer, String>    stops;
    private HashMap<Integer, String>    routes;
    private ArrayList<CalypsoFile>      cardStructure;
    private int countryId;
    private int networkId;
    //private int regionId;
    private boolean faresConfigured;
    private boolean profilesConfigured;
    private boolean topologyConfigured;
    private boolean cardStructureConfigured;
    private HashMap<String, NetworkListItem> networkList;

    // dirty and messy
    public ArrayList<Integer> contractPointers;

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

    public CalypsoEnvironment(String networkName) throws Exception {
        init();
        this.buildEnvironmentList();
        this.loadEnvironment(networkName);
    }

    private void init(){
        this.fares = new HashMap<>();
        this.profiles = new HashMap<>();
        this.routes = new HashMap<>();
        this.stops = new HashMap<>();
        this.cardStructure = new ArrayList<>();
        this.topologyConfigured = false;
        this.cardStructureConfigured = false;
        this.contractPointers = new ArrayList<>();
    }

    public void buildEnvironmentList(){
        this.networkList = new HashMap<>();
        Document networks = this.loadDocument("networks/networks.xml");
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
            for(Element network : country.getChildren()) {
                String networkName = network.getAttributeValue("name").toLowerCase();
                int countryCode = Integer.parseInt(network.getParentElement().getAttributeValue("id"));
                int networkId = Integer.parseInt(network.getAttributeValue("id"));

                this.networkList.put(networkName, new NetworkListItem(networkName, countryCode, networkId));
            }
    }

    public ArrayList<String> getAvailableNetworks(){
        if(this.networkList == null)
            return null;
        return new ArrayList<>(this.networkList.keySet());
    }

    public NetworkListItem getNetworkInfo(String networkName){
        return this.networkList.get(networkName);
    }

    public void loadEnvironment(int countryCode, int networkId) throws JDOMParseException {
        // countryCode/networkID/cardstruct.xml
        // countryCode/networkID/topology.xml
        // countryCode/networkID/fares.xml
        // countryCode/networkID/profiles.xml
        this.countryId = countryCode;
        this.networkId = networkId;

        String dir = "networks/"+countryCode+"/"+networkId+"/";
        System.out.println("Loading network profiles...");
        this.setNetworkProfiles(dir+"profiles.xml");
        System.out.println("Loading network fares...");
        this.setNetworkFares(dir+"fares.xml");
        System.out.println("Loading network topology...");
        this.setNetworkTopology(dir+"topology.xml");
        System.out.println("Loading card strucutre...");
        this.setCardStructure(dir+"cardstruct.xml");
    }

    public void loadEnvironment(String networkName) throws Exception {
        networkName = networkName.toLowerCase();
        NetworkListItem network = this.networkList.get(networkName);
        if(network == null)
            throw new Exception("Network not in networks.xml");
        this.loadEnvironment(network.countryCode, network.networkId);
    }

    public static Document openDocument(String filename){
        return new XMLIOImpl().loadDocument(filename);
    }

    // Yeah... Two names for the same method, right? Well I both need this to be static (be reusable outside of CalypsoEnv, and be overridable on Android... Ugly.
    public Document loadDocument(String filename){
        return openDocument(filename);
    }

    public boolean setNetworkProfiles(String faresFile) throws JDOMParseException {
        Document doc = this.loadDocument(faresFile);
        if(doc == null)
            return false;
        return setNetworkProfiles(doc);
    }

    /**
     * Loads the network profiles from an XML file.
     * @param doc XML Document containing profiles information.
     * @return Profile information properly loaded.
     * @throws JDOMParseException
     */
    public boolean setNetworkProfiles(Document doc) throws JDOMParseException {
        if(doc == null)
            return false;

        try {
            this.profilesConfigured = this.parseProfilesTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
        }

        if(this.profilesConfigured)
            System.out.println("Profiles loaded");
        else
            System.out.println("Profiles NOT loaded !");

        return this.profilesConfigured;
    }

    private boolean parseProfilesTree(Document doc) throws JDOMParseException {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be a calypsoEnvironment.", new Throwable("throwable"));
        }

        if(!this.checkCoherentEnvironment(root)){
            System.out.println("Incoherent network/country/region IDs.");
            return false;
        }

        Element profilesElement = root.getChildren().get(0);
        if(!profilesElement.getName().equals("profiles")) {
            throw new JDOMParseException("First first level child element should be profiles.", new Throwable("throwable"));
        }

        for(Element e : profilesElement.getChildren()) {
            String name = e.getAttributeValue("name");
            int id = Integer.parseInt(e.getAttributeValue("id"));
            this.profiles.put(id, name);
        }
        return true;
    }

    public boolean setNetworkFares(String faresFile) throws JDOMParseException {
        Document doc = this.loadDocument(faresFile);
        if(doc == null)
            return false;
        return setNetworkFares(doc);
    }

    /**
     * Loads the network fares from an XML file.
     * @param doc  XML Document containing fares information.
     * @return Network fare information properly loaded.
     * @throws JDOMParseException
     */
    public boolean setNetworkFares(Document doc) throws JDOMParseException {
        if(doc == null)
            return false;

        try {
            this.faresConfigured = this.parseFaresTree(doc);
        } catch (JDOMParseException e) {
            Logger.getGlobal().severe(e.getMessage());
        }

        if(this.faresConfigured)
            System.out.println("Fares loaded");
        else
            System.out.println("Fares NOT loaded !");

        return this.faresConfigured;
    }

    private boolean parseFaresTree(Document doc) throws JDOMParseException {
        Element root = doc.getRootElement();
        if(!root.getName().equals("calypsoEnvironment")) {
            throw new JDOMParseException("Root element should be a calypsoEnvironment.", new Throwable("throwable"));
        }

        if(!this.checkCoherentEnvironment(root)){
            System.out.println("Incoherent network/country/region IDs.");
            return false;
        }

        Element faresElement = root.getChildren().get(0);
        if(!faresElement.getName().equals("fares")) {
            throw new JDOMParseException("First first level child element should be fares.", new Throwable("throwable"));
        }

        for(Element e : faresElement.getChildren()) {
            String name = e.getAttributeValue("description");
            int id = Integer.parseInt(e.getAttributeValue("id"));
            this.fares.put(id, name);
        }
        return true;
    }

    public boolean setNetworkTopology(String topologyFile) throws JDOMParseException {
        Document doc = this.loadDocument(topologyFile);
        if(doc == null)
            return false;
        return setNetworkTopology(doc);
    }

    /**
     * Loads the network topology (stops and routes) from an XML file.
     * @param doc XML Document containing topology information.
     * @return Topology information properly loaded.
     * @throws JDOMParseException
     */
    public boolean setNetworkTopology(Document doc) throws JDOMParseException {
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

    public boolean setCardStructure(String cardStructureFile) throws JDOMParseException {
        Document doc = this.loadDocument(cardStructureFile);
        if(doc == null)
            return false;
        return setCardStructure(doc);
    }

    /**
     * Loads the card structure (File and their structures) from an XML file.
     * @param doc XML Document containing card structure information.
     * @return Card structure information properly loaded.
     * @throws JDOMParseException
     */
    public boolean setCardStructure(Document doc) throws JDOMParseException {
        this.cardStructure = new ArrayList<>();
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
        String networkIdStr = envElement.getAttributeValue("networkId");
        if(networkIdStr != null) {
            int networkId = Integer.parseInt(networkIdStr);
            if (this.networkId != 0 && this.networkId != networkId)
                return false;
            else
                this.networkId = networkId;
        }

        String countryIdStr = envElement.getAttributeValue("countryId");
        if(countryIdStr != null) {
            int countryId = Integer.parseInt(envElement.getAttributeValue("countryId"));
            if (this.countryId != 0 && this.countryId != countryId)
                return false;
            else
                this.countryId = countryId;
        }

        return true;
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

    public String getFareName(int fareId){
        return this.fares.get(fareId);
    }

    public boolean isTopologyConfigured() {
        return topologyConfigured;
    }

    public boolean areFaresConfigured() {
        return faresConfigured;
    }

    public boolean areProfilesConfigured(){
        return this.profilesConfigured;
    }

    public String getProfileName(int profileId){
        return this.profiles.get(profileId);
    }

    public int getContractIndex(int contractPointer){
        for(int i = 0;i<this.contractPointers.size();i++)
            if(this.contractPointers.get(i) == contractPointer)
                return i;
        return -1;
    }

    public ArrayList<Integer> getContractPointers(){
        return this.contractPointers;
    }

    public CalypsoFile getFile(String description){
        for(CalypsoFile f : this.getFiles()) {
            if (f.getDescription().equals(description))
                return f;
            for (CalypsoFile ff : f.getChildren())
                if (ff.getDescription().equals(description))
                    return ff;
        }
        return null;
    }

    public void purgeFilesContents(){
        this.contractPointers.clear();
        for(CalypsoFile f : this.getFiles()) {
            f.deleteRecords();
            for (CalypsoFile ff : f.getChildren())
                f.deleteRecords();
        }
    }

    public void clear(){
        this.routes = new HashMap<>();
        this.stops = new HashMap<>();
        this.profiles = new HashMap<>();
        this.fares = new HashMap<>();
        this.cardStructure = new ArrayList<>();
        this.contractPointers = new ArrayList<>();
    }

    public void loadFileDump(CalypsoRawDump.CalypsoFileDump fd){
        CalypsoFile f = this.getFile(fd.getFilename());
        for(byte[] recBytes : fd.getRecords())
            f.newRecord(recBytes);
        for(CalypsoRawDump.CalypsoFileDump child : fd.getChildren())
            loadFileDump(child);
    }

    public void loadDump(CalypsoRawDump dump){
        this.purgeFilesContents();
        for(CalypsoRawDump.CalypsoFileDump fd : dump.getFiles())
            loadFileDump(fd);
    }
}
