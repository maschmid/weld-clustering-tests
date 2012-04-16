package org.jboss.weld.tests.clustering.translator;

public interface TranslatorController {

    String getText();

    void setText(String text);

    void translate();

    String getTranslatedText();

    void remove();

    int getTranslatedSentences();

}
