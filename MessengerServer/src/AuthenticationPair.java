// AuthenticationPair is used returning authentication state in one object.
public class AuthenticationPair {

    private boolean isAuthenticated;
    private String errorMessage;

    public AuthenticationPair(boolean isAuthenticated, String errorMessage) {
        this.isAuthenticated = isAuthenticated;
        this.errorMessage = errorMessage;
    }

    public AuthenticationPair(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
        this.errorMessage = "";
    }

    public String toString() {
        if (isAuthenticated)
            return "Credentials are authenticated";
        else
            return "Credentials are NOT authenticated: " + errorMessage;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
