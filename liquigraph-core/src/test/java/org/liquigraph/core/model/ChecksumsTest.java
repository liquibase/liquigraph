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
package org.liquigraph.core.model;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChecksumsTest {

    @Test
    public void computes_checksum_of_queries() {
        assertThat(checksum(asList("MATCH (n) RETURN n", "MATCH (m) RETURN m")))
            .isEqualTo("9c68d381cf24b9cab5843a506229c5dee1083f8e");
    }
}
