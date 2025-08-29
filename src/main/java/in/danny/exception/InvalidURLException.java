package in.danny.exception;

public class InvalidURLException extends RuntimeException {
    private final String message = "Invalid URL";
    private final String url;
    private final String errorMessage;
    public InvalidURLException(String originalUrl) {
        super();
        this.url = originalUrl;
        this.errorMessage = message;
    }
}
