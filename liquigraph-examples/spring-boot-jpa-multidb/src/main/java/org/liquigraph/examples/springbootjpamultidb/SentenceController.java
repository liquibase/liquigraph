package org.liquigraph.examples.springbootjpamultidb;

import org.liquigraph.examples.springbootjpamultidb.model.Sentence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentenceController {

    Logger logger = LoggerFactory.getLogger(SentenceController.class);

    private final SentenceRepository sentenceRepository;

    public SentenceController(SentenceRepository sentenceRepository) {
        this.sentenceRepository = sentenceRepository;
    }

    @GetMapping("/")
    public Iterable<Sentence> getSentences() {
        return this.sentenceRepository.findAll();
    }

}
