package org.liquigraph.core.model;

import static com.google.common.base.Charsets.UTF_16;
import static com.google.common.hash.Hashing.sha1;

public class Checksums {

    public static String checksum(String query) {
        return new String(sha1().hashString(query, UTF_16).asBytes(), UTF_16);
    }
}
