package fr.mikado.calypsofilefinder;

import fr.mikado.calypsoinspector.CalypsoEnvironment;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.smartcardio.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static fr.mikado.calypsoinspector.Main.getDefaultCard;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner s = new Scanner(System.in);

        System.out.println("\nCalypso File Finder\n");
        System.out.println("1. Find all files, save output to fileList");
        System.out.println("2. Find in files using fileList.xml");

        boolean invalidChoice;
        do {
            invalidChoice = false;
            System.out.print("\nChoice : ");
            int choice = Integer.parseInt(s.nextLine());
            switch (choice) {
                case 1:
                    findAllFiles(getDefaultCard());
                    break;
                case 2:
                    System.out.print("Haystack (must be a long as hex) : 0x");
                    long needle = Long.parseLong(s.nextLine(), 16);
                    System.out.print("\nHaystack size : ");
                    int needleSize = Integer.parseInt(s.nextLine());

                    CalypsoEnvironment cardEnv = new CalypsoEnvironment();
                    cardEnv.setCardStructure("fileList.xml");
                    CalypsoCardSearch search = new CalypsoCardSearch(getDefaultCard(), cardEnv, needle, needleSize);
                    System.out.println(search.search());
                    break;
                default:
                    System.out.println("Invalid input");
                    invalidChoice = true;
            }
        }while(invalidChoice);
    }

    public static void findAllFiles(Card c) throws CardException, IOException {
        Element root = new Element("calypsoEnvironment");
        Element card = new Element("card");
        root.addContent(card);
        for(int i =0;i<0xffff;i++)
            if(selectFile(c, i)) {
                System.out.println("\nFound file : " + i);
                Element file = new Element("file");
                file.setAttribute("identifier", ""+i);
                file.setAttribute("description", "unknown");
                file.setAttribute("type", "EF");
                card.addContent(file);
            }
            else if(i%100 == 0)
                System.out.println("\n"+i);
            else
                System.out.print(".");

        Document doc = new Document();
        doc.addContent(new Comment("If you are happy with those results, the rename this file as fileList.xml, and rerun (option 2) to search known values on the card."));
        doc.addContent(new Comment("Tweak this file so the structure doesn't generate any errors (no DF selected, security status...)"));
        doc.setRootElement(root);
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(doc, new FileWriter(new File("fileList.out.xml")));
    }

    public static boolean selectFile(Card c, int id) throws CardException {

        CommandAPDU cAPDU = new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, (byte) ((id>>8)&0xff), (byte) (id&0xff), 0x00});

        return c.getBasicChannel().transmit(cAPDU).getSW() == 0x9000;
    }
}





















