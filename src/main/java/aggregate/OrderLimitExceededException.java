package aggregate;

public class OrderLimitExceededException extends Exception {
    public String toString(){
        return ("OrderLimitExceededException Occured");
    }
}
