package fr.mikado.calypsofilefinder;

import fr.mikado.calypso.*;
import fr.mikado.isodep.CardException;
import fr.mikado.isodep.IsoDepInterface;

import java.io.*;
import java.util.ArrayList;

public class CalypsoCardSearch {

    public class SearchResult{
        private CalypsoRecordField resultField;
        private CalypsoRecord resultRecord;
        private int offset;

        public SearchResult(){
        }
        public SearchResult(CalypsoRecordField field, int offset){
            this.resultField = field;
            this.offset = offset;
        }
        public SearchResult(CalypsoRecord record, int offset){
            this.resultRecord = record;
            this.offset = offset;
        }
        public String toString() {
            if(this.resultField != null) {
                CalypsoRecord r = this.resultField.getParentRecord();
                CalypsoFile f = r.getParent();
                StringBuilder sb = new StringBuilder();
                sb.append("Result found !");
                sb.append("\n   File : "  + f.getFullPath());
                sb.append("\n   Record #" + r.getId());
                sb.append("\n   Field \"" + this.resultField.getDescription() + "\"");
                sb.append("\n   Bit offset " + this.offset);
                return sb.toString();
            }else if(this.resultRecord != null){
                CalypsoFile f = this.resultRecord.getParent();
                StringBuilder sb = new StringBuilder();
                sb.append("Result found ! \n   LFI : ");
                sb.append(f.getFullPath());
                sb.append("\n   Record #" + this.resultRecord.getId());
                sb.append("\n   Bit offset " + this.offset);
                return sb.toString();
            }
            else{
                return "Lol wut?";
            }
        }
    }

    private final CalypsoCard card;
    private BitArray needle;
    protected ArrayList<SearchResult> results;

    public CalypsoCardSearch(CalypsoCard c, BitArray needle) throws IOException {
        this.needle = needle;
        this.card = c;
        this.results = new ArrayList<>();
    }

    public CalypsoCardSearch(IsoDepInterface c, CalypsoEnvironment cardEnv, BitArray needle) throws IOException {
        this.needle = needle;
        this.card= new CalypsoCard(c, cardEnv);
        this.results = new ArrayList<>();
    }

    private void findBitsInRecordField(CalypsoRecordField rf, BitArray bits){
        if(rf.getSubfields().size() > 0) { // if no children, search in the field bits.
            int offset = rf.getBits().find(bits);
            if (offset >= 0) // if the pattern is found
                this.results.add(new SearchResult(rf, offset));
        }else // if children, search through these
            for(CalypsoRecordField child : rf.getSubfields())
                findBitsInRecordField(child, bits);
    }

    private void findBitsInFile(CalypsoFile f, BitArray bits){
        if(f.getType() == CalypsoFile.CalypsoFileType.DF) // If file.type == DF, search in the files it contains
            for(CalypsoFile child : f.getChildren())
               findBitsInFile(child, bits);
        else
            for(CalypsoRecord r : f.getRecords())
                if(r.getFields().size() != 0) { // search for bits in the record fields if any
                    for (CalypsoRecordField rf : r.getFields())
                        this.findBitsInRecordField(rf, bits);
                }else{ // If no fields in the record, search directly in the record bits.
                    int offset = r.getBits().find(bits);
                    if(offset >= 0){
                        System.out.println("FOUND !");
                        this.results.add(new SearchResult(r, offset));
                    }
                }
    }

    public void search() throws CardException {
        System.out.println("Reading card...");
        this.card.read();
        System.out.println("Searching...");
        for(CalypsoFile f : this.card.getEnvironment().getFiles())
            findBitsInFile(f, needle);
        this.card.dump(true);
    }

    public void dumpResults(){
        if(this.results.size() > 0)
            this.results.forEach(System.out::println);
        else
            System.out.println("No results found :(");
    }

}
