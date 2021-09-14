package cc.ioctl.nauthbot;

public class PrivateKeys {
    public static void init() {
        String key = System.getenv("TG_BOT_API_KEY");
        if (key == null) {
            throw new RuntimeException("'TG_BOT_API_KEY' not set in env, please set before running");
        }
        API_KEY = key;
    }

    public static final String USERNAME = "NAuthBot";
    public static String API_KEY;
}
