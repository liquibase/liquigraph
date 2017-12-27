/*
 * Copyright 2014-2018 the original author or authors.
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
package org.liquigraph.examples.dagger2;

import java.util.Objects;

class Neo4jVersion implements Comparable<Neo4jVersion> {

    private final int major;
    private final int minor;
    private final int patch;

    public static Neo4jVersion parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 2 && parts.length != 3) {
            throw new IllegalArgumentException(String.format("Unsupported version: %s. Expected MAJOR.MINOR or MAJOR.MINOR.PATCH format", version));
        }
        return new Neo4jVersion(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            parts.length == 2 ? 0 : Integer.parseInt(parts[2])
        );
    }

    public Neo4jVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Neo4jVersion other = (Neo4jVersion) obj;
        return Objects.equals(this.major, other.major)
            && Objects.equals(this.minor, other.minor)
            && Objects.equals(this.patch, other.patch);
    }

    @Override
    public int compareTo(Neo4jVersion version) {
        int majorDelta = major - version.major;
        if (majorDelta != 0) return majorDelta;
        int minorDelta = minor - version.minor;
        if (minorDelta != 0) return minorDelta;
        return patch - version.patch;
    }
}
