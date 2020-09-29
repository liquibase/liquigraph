package org.liquigraph.examples.springbootjpamultidb.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Sentence {

    @Id @GeneratedValue private Long id;

    private String text;

    private Sentence() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Sentence(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Sentence{");
        sb.append("id=").append(id);
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
