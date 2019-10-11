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
package org.liquigraph.extensions;

import org.liquigraph.core.model.Changeset;

import java.util.ArrayList;
import java.util.List;

public class ChangesetRecord {

    public final String id;
    public final String author;
    public final List<String> queries;

    public ChangesetRecord(Changeset changeset) {
        this.id = changeset.getId();
        this.author = changeset.getAuthor();
        this.queries = new ArrayList<>(changeset.getQueries());
    }

    @Override
    public String toString() {
        return "ChangesetRecord{" +
          "id='" + id + '\'' +
          ", author='" + author + '\'' +
          ", queries=" + queries +
          '}';
    }
}
