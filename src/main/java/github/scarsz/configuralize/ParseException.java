package github.scarsz.configuralize;

public class ParseException extends Exception {

    public ParseException(Source source, Throwable cause) {
        super("Error parsing config file " + source.getFile().getName() + ": " + cause.getMessage(), cause);
    }

}
