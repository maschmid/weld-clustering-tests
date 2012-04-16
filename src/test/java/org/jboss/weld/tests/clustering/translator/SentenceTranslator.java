package org.jboss.weld.tests.clustering.translator;

import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Clustered;

@Stateless
@Clustered
public class SentenceTranslator implements Translator {

    public String translate(String sentence) {
        return "Lorem ipsum dolor sit amet";
    }

}
