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
package org.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import org.liquigraph.core.model.Changeset;

import static com.google.common.base.Preconditions.checkState;

public class ChangesetById implements Predicate<Changeset> {

    private final String id;
    private final String author;

    private ChangesetById(String id, String author) {
        this.id = id;
        this.author = author;
        checkState(id != null);
        checkState(author != null);
    }

    public static Predicate<Changeset> BY_ID(String id, String author) {
        return new ChangesetById(id, author);
    }

    @Override
    public boolean apply(Changeset input) {
        return id.equals(input.getId()) &&
               author.equals(input.getAuthor());
    }
}
