package exceptions;

public class BytesControlException extends Exception {
//Comprobamos que termine con los bytes de control
    public BytesControlException(String msg) {
        super(msg);
    }

}
