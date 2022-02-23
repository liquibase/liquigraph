/*
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.liquigraph.core.exception.Throwables.propagate;

public class Checksums {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public static String checksum(Collection<String> queries) {
        MessageDigest messageDigest = sha1MessageDigest();
        for (String query : queries) {
            messageDigest.update(query.getBytes(UTF_8));
        }
        return buildHexadecimalRepresentation(messageDigest.digest());
    }

    // adapted from com.google.common.hash.HashCode#toString
    private static String buildHexadecimalRepresentation(byte[] bytes) {
        StringBuilder builder = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            builder.append(HEX_DIGITS[b >> 4 & 15]).append(HEX_DIGITS[b & 15]);
        }
        return builder.toString();
    }

    private static MessageDigest sha1MessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw propagate(e);
        }
    }
}
