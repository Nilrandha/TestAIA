package aiatest.function;

public class ResponseEntity {
     private int statusCode;
     private String message;
     private String firstName;
     private String lastName;

    public ResponseEntity() {}

    public ResponseEntity(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ResponseEntity(int statusCode, String message, String firstName, String lastName) {
        this.statusCode = statusCode;
        this.message = message;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
