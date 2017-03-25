package fr.mikado.calypsofilefinder;

import fr.mikado.calypsoinspector.*;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import java.io.*;

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
            }else
                return "Bit pattern not found :(";
        }
    }

    private final Card c;
    private final CalypsoEnvironment cardEnv;
    private final CalypsoCard card;
    private BitArray needle;
    private SearchResult result;

    public CalypsoCardSearch(Card c, CalypsoEnvironment cardEnv, long needle, int needleSize) throws IOException {
        this.c = c;
        this.needle = new BitArray(needle, needleSize);
        this.cardEnv = cardEnv;
        this.card= new CalypsoCard(c, cardEnv);
    }

    private SearchResult findBitsInRecordField(CalypsoRecordField rf, BitArray bits){
        int offset = rf.getBits().find(bits);
        if(offset >= 0)
            return new SearchResult(rf, offset);
        for(CalypsoRecordField child : rf.getSubfields()) {
            SearchResult result;
            if ( (result = findBitsInRecordField(child, bits)) != null)
                return result;
        }
        return null;
    }

    private SearchResult findBitsInFile(CalypsoFile f, BitArray bits){
        if(f.getType() == CalypsoFile.CalypsoFileType.DF)
            for(CalypsoFile child : f.getChildren()) {
                SearchResult result;
                if ( (result = findBitsInFile(child, bits)) != null)
                    return result;
            }
        else
            for(CalypsoRecord r : f.getRecords())
                if(r.getFields().size() != 0) {
                    for (CalypsoRecordField rf : r.getFields()) {
                        SearchResult result;
                        if ((result = this.findBitsInRecordField(rf, this.needle)) != null) {
                            System.out.println("FOUND !");
                            return result;
                        }
                    }
                }else{
                    int offset = r.getBits().find(this.needle);
                    if(offset >= 0){
                        System.out.println("FOUND !");
                        return new SearchResult(r, offset);
                    }
                }
        return null;
    }

    public SearchResult search() throws CardException {
        System.out.println("Reading card...");
        this.card.read();
        System.out.println("Searching...");
        this.result = null;
        for(CalypsoFile f : this.cardEnv.getFiles()){
            SearchResult result;
            if( (result = findBitsInFile(f, needle)) != null) {
                this.result = result;
                break;
            }
        }
        this.card.dump();
        if(this.result == null)
            this.result = new SearchResult();
        return this.result;
    }

}
