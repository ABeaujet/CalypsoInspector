package fr.mikado.isodep;

/**
 * Created by alexis on 05/04/17.
 */
public class CardException extends Exception {

    public CardException(){
    }

    public CardException(String message){
        super(message);
    }

    public CardException(Throwable cause){
        super(cause);
    }

    public CardException(String message, Throwable cause){
        super(message, cause);
    }
}
