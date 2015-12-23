package org.liquigraph.core.model;

import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.util.Collection;

public class Checksums {

    public static String checksum(Collection<String> queries) {
        Hasher hasher = Hashing.sha1().newHasher();
        for (String query : queries) {
            hasher = hasher.putString(query, Charsets.UTF_8);
        }
        return hasher.hash().toString();
    }
}
