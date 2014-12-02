package org.liquigraph.core.model;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class Checksums {

    public static String checksum(String query) {
        return Hashing.sha1().newHasher().putString(query, Charsets.UTF_8).hash().toString();
    }
}
