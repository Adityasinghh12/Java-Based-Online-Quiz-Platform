/**
 * Settings.java
 * A single system-wide configuration record managed by the Admin.
 */
public class Settings {
    private String platformName;
    private int defaultTimeLimitMinutes;
    private int passingPercentage;

    public Settings(String platformName, int defaultTimeLimitMinutes, int passingPercentage) {
        this.platformName = platformName;
        this.defaultTimeLimitMinutes = defaultTimeLimitMinutes;
        this.passingPercentage = passingPercentage;
    }

    public String getPlatformName() { return platformName; }
    public int getDefaultTimeLimitMinutes() { return defaultTimeLimitMinutes; }
    public int getPassingPercentage() { return passingPercentage; }

    public String toFileString() {
        return platformName + "|" + defaultTimeLimitMinutes + "|" + passingPercentage;
    }

    public static Settings fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        return new Settings(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
