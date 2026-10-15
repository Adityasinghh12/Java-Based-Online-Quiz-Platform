/**
 * Alert.java
 * A system notification shown on the Admin dashboard - e.g. a new
 * quiz was submitted for approval, or a quiz was rejected.
 */
public class Alert {
    private int id;
    private String message;
    private String date;

    public Alert(int id, String message, String date) {
        this.id = id;
        this.message = message;
        this.date = date;
    }

    public int getId() { return id; }
    public String getMessage() { return message; }
    public String getDate() { return date; }

    public String toFileString() {
        return id + "|" + message + "|" + date;
    }

    public static Alert fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Alert(Integer.parseInt(parts[0]), parts[1], parts[2]);
    }
}
