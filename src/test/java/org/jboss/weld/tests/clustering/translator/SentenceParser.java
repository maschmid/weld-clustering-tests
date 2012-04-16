package org.jboss.weld.tests.clustering.translator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SentenceParser implements Serializable {

    public List<String> parse(String text) {
        return Arrays.asList(text.split("[.?]"));
    }

}
