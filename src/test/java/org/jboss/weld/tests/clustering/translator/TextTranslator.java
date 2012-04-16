package org.jboss.weld.tests.clustering.translator;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;

public class TextTranslator implements Serializable {
    private SentenceParser sentenceParser;

    @EJB
    Translator translator;
    
    int sentences = 0;

    @Inject
    public TextTranslator(SentenceParser sentenceParser) {
        this.sentenceParser = sentenceParser;
    }

    public String translate(String text) {
        StringBuilder sb = new StringBuilder();
        for (String sentence : sentenceParser.parse(text)) {
            sb.append(translator.translate(sentence)).append(". ");
            ++sentences;
        }
        return sb.toString().trim();
    }
    
    public int getTransaltedSentenced() {
       return sentences;
    }

}
