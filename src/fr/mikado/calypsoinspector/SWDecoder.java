package fr.mikado.calypsoinspector;

import java.util.HashMap;

import static fr.mikado.calypsoinspector.SWDecoder.*;

/**
 * Contains information from there, so some SWs may be EMV related : https://www.eftlab.co.uk/index.php/site-map/knowledge-base/118-apdu-response-list
 */

public class SWDecoder {

    private static class ISO78164Result{
        private int sw;
        private String description;
        private String level;

        public ISO78164Result(int sw, String level, String description){
            this.sw = sw;
            this.level = level;
            this.description = description;
        }
        public String getDescription() {  return description;   }
        public String getLevel() {  return level;   }
    }

    public static String decode(int SW){
        if(swDescriptions.containsKey(SW)) {
            ISO78164Result r = swDescriptions.get(SW);
            return r.getLevel() + " : " + r.getDescription();
        }else
            return "Unknown 7816-4 SW : 0x" + Integer.toHexString(SW);
    }

    private static HashMap<Integer, ISO78164Result> swDescriptions;
    static{
        swDescriptions = new HashMap<>();
        swDescriptions.put(0x6200, new ISO78164Result(0x6200, "Warning", "No information given (NV-Ram not changed)"));
        swDescriptions.put(0x6201, new ISO78164Result(0x6201, "Warning", "NV-Ram not changed 1."));
        swDescriptions.put(0x6281, new ISO78164Result(0x6281, "Warning", "Part of returned data may be corrupted"));
        swDescriptions.put(0x6282, new ISO78164Result(0x6282, "Warning", "End of file/record reached before reading Le bytes"));
        swDescriptions.put(0x6283, new ISO78164Result(0x6283, "Warning", "Selected file invalidated"));
        swDescriptions.put(0x6284, new ISO78164Result(0x6284, "Warning", "Selected file is not valid. FCI not formated according to ISO"));
        swDescriptions.put(0x6285, new ISO78164Result(0x6285, "Warning", "No input data available from a sensor on the card. No Purse Engine enslaved for R3bc"));
        swDescriptions.put(0x62A2, new ISO78164Result(0x62A2, "Warning", "Wrong R-MAC"));
        swDescriptions.put(0x62A4, new ISO78164Result(0x62A4, "Warning", "Card locked (during reset( ))"));
        swDescriptions.put(0x62F1, new ISO78164Result(0x62F1, "Warning", "Wrong C-MAC"));
        swDescriptions.put(0x62F3, new ISO78164Result(0x62F3, "Warning", "Internal reset"));
        swDescriptions.put(0x62F5, new ISO78164Result(0x62F5, "Warning", "Default agent locked"));
        swDescriptions.put(0x62F7, new ISO78164Result(0x62F7, "Warning", "Cardholder locked"));
        swDescriptions.put(0x62F8, new ISO78164Result(0x62F8, "Warning", "Basement is current agent"));
        swDescriptions.put(0x62F9, new ISO78164Result(0x62F9, "Warning", "CALC Key Set not unblocked"));
        swDescriptions.put(0x6300, new ISO78164Result(0x6300, "Warning", "No information given (NV-Ram changed)"));
        swDescriptions.put(0x6381, new ISO78164Result(0x6381, "Warning", "File filled up by the last write. Loading/updating is not allowed."));
        swDescriptions.put(0x6382, new ISO78164Result(0x6382, "Warning", "Card key not supported."));
        swDescriptions.put(0x6383, new ISO78164Result(0x6383, "Warning", "Reader key not supported."));
        swDescriptions.put(0x6384, new ISO78164Result(0x6384, "Warning", "Plaintext transmission not supported."));
        swDescriptions.put(0x6385, new ISO78164Result(0x6385, "Warning", "Secured transmission not supported."));
        swDescriptions.put(0x6386, new ISO78164Result(0x6386, "Warning", "Volatile memory is not available."));
        swDescriptions.put(0x6387, new ISO78164Result(0x6387, "Warning", "Non-volatile memory is not available."));
        swDescriptions.put(0x6388, new ISO78164Result(0x6388, "Warning", "Key number not valid."));
        swDescriptions.put(0x6389, new ISO78164Result(0x6389, "Warning", "Key length is not correct."));
        swDescriptions.put(0x63C0, new ISO78164Result(0x63C0, "Warning", "Verify fail, no try left."));
        swDescriptions.put(0x63C1, new ISO78164Result(0x63C1, "Warning", "Verify fail, 1 try left."));
        swDescriptions.put(0x63C2, new ISO78164Result(0x63C2, "Warning", "Verify fail, 2 tries left."));
        swDescriptions.put(0x63C3, new ISO78164Result(0x63C3, "Warning", "Verify fail, 3 tries left."));
        swDescriptions.put(0x63F1, new ISO78164Result(0x63F1, "Warning", "More data expected."));
        swDescriptions.put(0x63F2, new ISO78164Result(0x63F2, "Warning", "More data expected and proactive command pending."));
        swDescriptions.put(0x6400, new ISO78164Result(0x6400, "Error",   "No information given (NV-Ram not changed)"));
        swDescriptions.put(0x6401, new ISO78164Result(0x6401, "Error",   "Command timeout. Immediate response required by the card."));
        swDescriptions.put(0x6500, new ISO78164Result(0x6500, "Error",   "No information given"));
        swDescriptions.put(0x6501, new ISO78164Result(0x6501, "Error",   "Write error. Memory failure. There have been problems in writing or reading the EEPROM. Other hardware problems may also bring this error."));
        swDescriptions.put(0x6581, new ISO78164Result(0x6581, "Error",   "Memory failure"));
        swDescriptions.put(0x6600, new ISO78164Result(0x6600, "S",       "Error while receiving (timeout)"));
        swDescriptions.put(0x6601, new ISO78164Result(0x6601, "S",       "Error while receiving (character parity error)"));
        swDescriptions.put(0x6602, new ISO78164Result(0x6602, "S",       "Wrong checksum"));
        swDescriptions.put(0x6603, new ISO78164Result(0x6603, "S",       "The current DF file without FCI"));
        swDescriptions.put(0x6604, new ISO78164Result(0x6604, "S",       "No SF or KF under the current DF"));
        swDescriptions.put(0x6669, new ISO78164Result(0x6669, "S",       "Incorrect Encryption/Decryption Padding"));
        swDescriptions.put(0x6700, new ISO78164Result(0x6700, "Error",   "Wrong length"));
        swDescriptions.put(0x6800, new ISO78164Result(0x6800, "Error",   "No information given (The request function is not supported by the card)"));
        swDescriptions.put(0x6881, new ISO78164Result(0x6881, "Error",   "Logical channel not supported"));
        swDescriptions.put(0x6882, new ISO78164Result(0x6882, "Error",   "Secure messaging not supported"));
        swDescriptions.put(0x6883, new ISO78164Result(0x6883, "Error",   "Last command of the chain expected"));
        swDescriptions.put(0x6884, new ISO78164Result(0x6884, "Error",   "Command chaining not supported"));
        swDescriptions.put(0x6900, new ISO78164Result(0x6900, "Error",   "No information given (Command not allowed)"));
        swDescriptions.put(0x6901, new ISO78164Result(0x6901, "Error",   "Command not accepted (inactive state)"));
        swDescriptions.put(0x6981, new ISO78164Result(0x6981, "Error",   "Command incompatible with file structure"));
        swDescriptions.put(0x6982, new ISO78164Result(0x6982, "Error",   "Security condition not satisfied."));
        swDescriptions.put(0x6983, new ISO78164Result(0x6983, "Error",   "Authentication method blocked"));
        swDescriptions.put(0x6984, new ISO78164Result(0x6984, "Error",   "Referenced data reversibly blocked (invalidated)"));
        swDescriptions.put(0x6985, new ISO78164Result(0x6985, "Error",   "Conditions of use not satisfied."));
        swDescriptions.put(0x6986, new ISO78164Result(0x6986, "Error",   "Command not allowed (no current EF)"));
        swDescriptions.put(0x6987, new ISO78164Result(0x6987, "Error",   "Expected secure messaging (SM) object missing"));
        swDescriptions.put(0x6988, new ISO78164Result(0x6988, "Error",   "Incorrect secure messaging (SM) data object"));
        swDescriptions.put(0x698D, new ISO78164Result(0x698D, " ",       "Reserved"));
        swDescriptions.put(0x6996, new ISO78164Result(0x6996, "Error",   "Data must be updated again"));
        swDescriptions.put(0x69E1, new ISO78164Result(0x69E1, "Error",   "POL1 of the currently Enabled Profile prevents this action."));
        swDescriptions.put(0x69F0, new ISO78164Result(0x69F0, "Error",   "Permission Denied"));
        swDescriptions.put(0x69F1, new ISO78164Result(0x69F1, "Error",   "Permission Denied - Missing Privilege"));
        swDescriptions.put(0x6A00, new ISO78164Result(0x6A00, "Error",   "No information given (Bytes P1 and/or P2 are incorrect)"));
        swDescriptions.put(0x6A80, new ISO78164Result(0x6A80, "Error",   "The parameters in the data field are incorrect."));
        swDescriptions.put(0x6A81, new ISO78164Result(0x6A81, "Error",   "Function not supported"));
        swDescriptions.put(0x6A82, new ISO78164Result(0x6A82, "Error",   "File not found"));
        swDescriptions.put(0x6A83, new ISO78164Result(0x6A83, "Error",   "Record not found"));
        swDescriptions.put(0x6A84, new ISO78164Result(0x6A84, "Error",   "There is insufficient memory space in record or file"));
        swDescriptions.put(0x6A85, new ISO78164Result(0x6A85, "Error",   "Lc inconsistent with TLV structure"));
        swDescriptions.put(0x6A86, new ISO78164Result(0x6A86, "Error",   "Incorrect P1 or P2 parameter."));
        swDescriptions.put(0x6A87, new ISO78164Result(0x6A87, "Error",   "Lc inconsistent with P1-P2"));
        swDescriptions.put(0x6A88, new ISO78164Result(0x6A88, "Error",   "Referenced data not found"));
        swDescriptions.put(0x6A89, new ISO78164Result(0x6A89, "Error",   "File already exists"));
        swDescriptions.put(0x6A8A, new ISO78164Result(0x6A8A, "Error",   "DF name already exists."));
        swDescriptions.put(0x6AF0, new ISO78164Result(0x6AF0, "Error",   "Wrong parameter value"));
        swDescriptions.put(0x6B00, new ISO78164Result(0x6B00, "Error",   "Wrong parameter(s) P1-P2"));
        swDescriptions.put(0x6C00, new ISO78164Result(0x6C00, "Error",   "Incorrect P3 length."));
        swDescriptions.put(0x6D00, new ISO78164Result(0x6D00, "Error",   "Instruction code not supported or invalid"));
        swDescriptions.put(0x6E00, new ISO78164Result(0x6E00, "Error",   "Class not supported"));
        swDescriptions.put(0x6F00, new ISO78164Result(0x6F00, "Error",   "Command aborted - more exact diagnosis not possible (e.g., operating system error)."));
        swDescriptions.put(0x6FFF, new ISO78164Result(0x6FFF, "Error",   "Card dead (overuse, …)"));
        swDescriptions.put(0x9000, new ISO78164Result(0x9000, "I",       "Command successfully executed (OK)."));
        swDescriptions.put(0x9004, new ISO78164Result(0x9004, "Warning", "PIN not successfully verified, 3 or more PIN tries left"));
        swDescriptions.put(0x9008, new ISO78164Result(0x9008, " ",       "Key/file not found"));
        swDescriptions.put(0x9080, new ISO78164Result(0x9080, "Warning", "Unblock Try Counter has reached zero"));
        swDescriptions.put(0x9100, new ISO78164Result(0x9100, " ",       "OK"));
        swDescriptions.put(0x9101, new ISO78164Result(0x9101, " ",       "States.activity, States.lock Status or States.lockable has wrong value"));
        swDescriptions.put(0x9102, new ISO78164Result(0x9102, " ",       "Transaction number reached its limit"));
        swDescriptions.put(0x910C, new ISO78164Result(0x910C, " ",       "No changes"));
        swDescriptions.put(0x910E, new ISO78164Result(0x910E, " ",       "Insufficient NV-Memory to complete command"));
        swDescriptions.put(0x911C, new ISO78164Result(0x911C, " ",       "Command code not supported"));
        swDescriptions.put(0x911E, new ISO78164Result(0x911E, " ",       "CRC or MAC does not match data"));
        swDescriptions.put(0x9140, new ISO78164Result(0x9140, " ",       "Invalid key number specified"));
        swDescriptions.put(0x917E, new ISO78164Result(0x917E, " ",       "Length of command string invalid"));
        swDescriptions.put(0x919D, new ISO78164Result(0x919D, " ",       "Not allow the requested command"));
        swDescriptions.put(0x919E, new ISO78164Result(0x919E, " ",       "Value of the parameter invalid"));
        swDescriptions.put(0x91A0, new ISO78164Result(0x91A0, " ",       "Requested AID not present on PICC"));
        swDescriptions.put(0x91A1, new ISO78164Result(0x91A1, " ",       "Unrecoverable error within application"));
        swDescriptions.put(0x91AE, new ISO78164Result(0x91AE, " ",       "Authentication status does not allow the requested command"));
        swDescriptions.put(0x91AF, new ISO78164Result(0x91AF, " ",       "Additional data frame is expected to be sent"));
        swDescriptions.put(0x91BE, new ISO78164Result(0x91BE, " ",       "Out of boundary"));
        swDescriptions.put(0x91C1, new ISO78164Result(0x91C1, " ",       "Unrecoverable error within PICC"));
        swDescriptions.put(0x91CA, new ISO78164Result(0x91CA, " ",       "Previous Command was not fully completed"));
        swDescriptions.put(0x91CD, new ISO78164Result(0x91CD, " ",       "PICC was disabled by an unrecoverable error"));
        swDescriptions.put(0x91CE, new ISO78164Result(0x91CE, " ",       "Number of Applications limited to 28"));
        swDescriptions.put(0x91DE, new ISO78164Result(0x91DE, " ",       "File or application already exists"));
        swDescriptions.put(0x91EE, new ISO78164Result(0x91EE, " ",       "Could not complete NV-write operation due to loss of power"));
        swDescriptions.put(0x91F0, new ISO78164Result(0x91F0, " ",       "Specified file number does not exist"));
        swDescriptions.put(0x91F1, new ISO78164Result(0x91F1, " ",       "Unrecoverable error within file"));
        swDescriptions.put(0x9210, new ISO78164Result(0x9210, "Error",   "Insufficient memory. No more storage available."));
        swDescriptions.put(0x9240, new ISO78164Result(0x9240, "Error",   "Writing to EEPROM not successful."));
        swDescriptions.put(0x9301, new ISO78164Result(0x9301, " ",       "Integrity error"));
        swDescriptions.put(0x9302, new ISO78164Result(0x9302, " ",       "Candidate S2 invalid"));
        swDescriptions.put(0x9303, new ISO78164Result(0x9303, "Error",   "Application is permanently locked"));
        swDescriptions.put(0x9400, new ISO78164Result(0x9400, "Error",   "No EF selected."));
        swDescriptions.put(0x9401, new ISO78164Result(0x9401, " ",       "Candidate currency code does not match purse currency"));
        swDescriptions.put(0x9402, new ISO78164Result(0x9402, " ",       "Candidate amount too high"));
        swDescriptions.put(0x9402, new ISO78164Result(0x9402, "Error",   "Address range exceeded."));
        swDescriptions.put(0x9403, new ISO78164Result(0x9403, " ",       "Candidate amount too low"));
        swDescriptions.put(0x9404, new ISO78164Result(0x9404, "Error",   "FID not found, record not found or comparison pattern not found."));
        swDescriptions.put(0x9405, new ISO78164Result(0x9405, " ",       "Problems in the data field"));
        swDescriptions.put(0x9406, new ISO78164Result(0x9406, "Error",   "Required MAC unavailable"));
        swDescriptions.put(0x9407, new ISO78164Result(0x9407, " ",       "Bad currency : purse engine has no slot with R3bc currency"));
        swDescriptions.put(0x9408, new ISO78164Result(0x9408, " ",       "R3bc currency not supported in purse engine"));
        swDescriptions.put(0x9408, new ISO78164Result(0x9408, "Error",   "Selected file type does not match command."));
        swDescriptions.put(0x9580, new ISO78164Result(0x9580, " ",       "Bad sequence"));
        swDescriptions.put(0x9681, new ISO78164Result(0x9681, " ",       "Slave not found"));
        swDescriptions.put(0x9700, new ISO78164Result(0x9700, " ",       "PIN blocked and Unblock Try Counter is 1 or 2"));
        swDescriptions.put(0x9702, new ISO78164Result(0x9702, " ",       "Main keys are blocked"));
        swDescriptions.put(0x9704, new ISO78164Result(0x9704, " ",       "PIN not successfully verified, 3 or more PIN tries left"));
        swDescriptions.put(0x9784, new ISO78164Result(0x9784, " ",       "Base key"));
        swDescriptions.put(0x9785, new ISO78164Result(0x9785, " ",       "Limit exceeded - C-MAC key"));
        swDescriptions.put(0x9786, new ISO78164Result(0x9786, " ",       "SM error - Limit exceeded - R-MAC key"));
        swDescriptions.put(0x9787, new ISO78164Result(0x9787, " ",       "Limit exceeded - sequence counter"));
        swDescriptions.put(0x9788, new ISO78164Result(0x9788, " ",       "Limit exceeded - R-MAC length"));
        swDescriptions.put(0x9789, new ISO78164Result(0x9789, " ",       "Service not available"));
        swDescriptions.put(0x9802, new ISO78164Result(0x9802, "Error",   "No PIN defined."));
        swDescriptions.put(0x9804, new ISO78164Result(0x9804, "Error",   "Access conditions not satisfied, authentication failed."));
        swDescriptions.put(0x9835, new ISO78164Result(0x9835, "Error",   "ASK RANDOM or GIVE RANDOM not executed."));
        swDescriptions.put(0x9840, new ISO78164Result(0x9840, "Error",   "PIN verification not successful."));
        swDescriptions.put(0x9850, new ISO78164Result(0x9850, "Error",   "INCREASE or DECREASE could not be executed because a limit has been reached."));
        swDescriptions.put(0x9862, new ISO78164Result(0x9862, "Error",   "Authentication Error, application specific (incorrect MAC)"));
        swDescriptions.put(0x9900, new ISO78164Result(0x9900, " ",       "1 PIN try left"));
        swDescriptions.put(0x9904, new ISO78164Result(0x9904, " ",       "PIN not successfully verified, 1 PIN try left"));
        swDescriptions.put(0x9985, new ISO78164Result(0x9985, " ",       "Wrong status - Cardholder lock"));
        swDescriptions.put(0x9986, new ISO78164Result(0x9986, "Error",   "Missing privilege"));
        swDescriptions.put(0x9987, new ISO78164Result(0x9987, " ",       "PIN is not installed"));
        swDescriptions.put(0x9988, new ISO78164Result(0x9988, " ",       "Wrong status - R-MAC state"));
        swDescriptions.put(0x9A00, new ISO78164Result(0x9A00, " ",       "2 PIN try left"));
        swDescriptions.put(0x9A04, new ISO78164Result(0x9A04, " ",       "PIN not successfully verified, 2 PIN try left"));
        swDescriptions.put(0x9A71, new ISO78164Result(0x9A71, " ",       "Wrong parameter value - Double agent AID"));
        swDescriptions.put(0x9A72, new ISO78164Result(0x9A72, " ",       "Wrong parameter value - Double agent Type"));
        swDescriptions.put(0x9D05, new ISO78164Result(0x9D05, "Error",   "Incorrect certificate type"));
        swDescriptions.put(0x9D07, new ISO78164Result(0x9D07, "Error",   "Incorrect session data size"));
        swDescriptions.put(0x9D08, new ISO78164Result(0x9D08, "Error",   "Incorrect DIR file record size"));
        swDescriptions.put(0x9D09, new ISO78164Result(0x9D09, "Error",   "Incorrect FCI record size"));
        swDescriptions.put(0x9D0A, new ISO78164Result(0x9D0A, "Error",   "Incorrect code size"));
        swDescriptions.put(0x9D10, new ISO78164Result(0x9D10, "Error",   "Insufficient memory to load application"));
        swDescriptions.put(0x9D11, new ISO78164Result(0x9D11, "Error",   "Invalid AID"));
        swDescriptions.put(0x9D12, new ISO78164Result(0x9D12, "Error",   "Duplicate AID"));
        swDescriptions.put(0x9D13, new ISO78164Result(0x9D13, "Error",   "Application previously loaded"));
        swDescriptions.put(0x9D14, new ISO78164Result(0x9D14, "Error",   "Application history list full"));
        swDescriptions.put(0x9D15, new ISO78164Result(0x9D15, "Error",   "Application not open"));
        swDescriptions.put(0x9D17, new ISO78164Result(0x9D17, "Error",   "Invalid offset"));
        swDescriptions.put(0x9D18, new ISO78164Result(0x9D18, "Error",   "Application already loaded"));
        swDescriptions.put(0x9D19, new ISO78164Result(0x9D19, "Error",   "Invalid certificate"));
        swDescriptions.put(0x9D1A, new ISO78164Result(0x9D1A, "Error",   "Invalid signature"));
        swDescriptions.put(0x9D1B, new ISO78164Result(0x9D1B, "Error",   "Invalid KTU"));
        swDescriptions.put(0x9D1D, new ISO78164Result(0x9D1D, "Error",   "MSM controls not set"));
        swDescriptions.put(0x9D1E, new ISO78164Result(0x9D1E, "Error",   "Application signature does not exist"));
        swDescriptions.put(0x9D1F, new ISO78164Result(0x9D1F, "Error",   "KTU does not exist"));
        swDescriptions.put(0x9D20, new ISO78164Result(0x9D20, "Error",   "Application not loaded"));
        swDescriptions.put(0x9D21, new ISO78164Result(0x9D21, "Error",   "Invalid Open command data length"));
        swDescriptions.put(0x9D30, new ISO78164Result(0x9D30, "Error",   "Check data parameter is incorrect (invalid start address)"));
        swDescriptions.put(0x9D31, new ISO78164Result(0x9D31, "Error",   "Check data parameter is incorrect (invalid length)"));
        swDescriptions.put(0x9D32, new ISO78164Result(0x9D32, "Error",   "Check data parameter is incorrect (illegal memory check area)"));
        swDescriptions.put(0x9D40, new ISO78164Result(0x9D40, "Error",   "Invalid MSM Controls ciphertext"));
        swDescriptions.put(0x9D41, new ISO78164Result(0x9D41, "Error",   "MSM controls already set"));
        swDescriptions.put(0x9D42, new ISO78164Result(0x9D42, "Error",   "Set MSM Controls data length less than 2 bytes"));
        swDescriptions.put(0x9D43, new ISO78164Result(0x9D43, "Error",   "Invalid MSM Controls data length"));
        swDescriptions.put(0x9D44, new ISO78164Result(0x9D44, "Error",   "Excess MSM Controls ciphertext"));
        swDescriptions.put(0x9D45, new ISO78164Result(0x9D45, "Error",   "Verification of MSM Controls data failed"));
        swDescriptions.put(0x9D50, new ISO78164Result(0x9D50, "Error",   "Invalid MCD Issuer production ID"));
        swDescriptions.put(0x9D51, new ISO78164Result(0x9D51, "Error",   "Invalid MCD Issuer ID"));
        swDescriptions.put(0x9D52, new ISO78164Result(0x9D52, "Error",   "Invalid set MSM controls data date"));
        swDescriptions.put(0x9D53, new ISO78164Result(0x9D53, "Error",   "Invalid MCD number"));
        swDescriptions.put(0x9D54, new ISO78164Result(0x9D54, "Error",   "Reserved field error"));
        swDescriptions.put(0x9D55, new ISO78164Result(0x9D55, "Error",   "Reserved field error"));
        swDescriptions.put(0x9D56, new ISO78164Result(0x9D56, "Error",   "Reserved field error"));
        swDescriptions.put(0x9D57, new ISO78164Result(0x9D57, "Error",   "Reserved field error"));
        swDescriptions.put(0x9D60, new ISO78164Result(0x9D60, "Error",   "MAC verification failed"));
        swDescriptions.put(0x9D61, new ISO78164Result(0x9D61, "Error",   "Maximum number of unblocks reached"));
        swDescriptions.put(0x9D62, new ISO78164Result(0x9D62, "Error",   "Card was not blocked"));
        swDescriptions.put(0x9D63, new ISO78164Result(0x9D63, "Error",   "Crypto functions not available"));
        swDescriptions.put(0x9D64, new ISO78164Result(0x9D64, "Error",   "No application loaded"));
        swDescriptions.put(0x9E00, new ISO78164Result(0x9E00, " ",       "PIN not installed"));
        swDescriptions.put(0x9E04, new ISO78164Result(0x9E04, " ",       "PIN not successfully verified, PIN not installed"));
        swDescriptions.put(0x9F00, new ISO78164Result(0x9F00, " ",       "PIN blocked and Unblock Try Counter is 3"));
        swDescriptions.put(0x9F04, new ISO78164Result(0x9F04, " ",       "PIN not successfully verified, PIN blocked and Unblock Try Counter is 3"));
    }
}
