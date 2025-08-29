package in.danny;

import java.util.concurrent.ConcurrentHashMap;

public class UrlStore {
    public static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    // originalUrl -> shortCode (để check trùng)
    public static ConcurrentHashMap<String, String> reverseMap = new ConcurrentHashMap<>();
}
