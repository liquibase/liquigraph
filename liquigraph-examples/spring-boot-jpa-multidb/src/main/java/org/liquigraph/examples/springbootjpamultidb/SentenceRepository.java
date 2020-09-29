package org.liquigraph.examples.springbootjpamultidb;

import org.liquigraph.examples.springbootjpamultidb.model.Sentence;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SentenceRepository extends Neo4jRepository<Sentence, Long> {

    Sentence findByText(String text);

}
