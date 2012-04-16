package org.jboss.weld.tests.clustering.translator;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.ejb3.annotation.Clustered;

@Stateful
@SessionScoped
@Named("translator")
@Clustered
public class TranslatorControllerBean implements TranslatorController {

    @Inject
    private TextTranslator translator;

    private String inputText;

    private String translatedText;

    public String getText() {
        return inputText;
    }

    public void setText(String text) {
        this.inputText = text;
    }

    public void translate() {
        translatedText = translator.translate(inputText);
    }

    public String getTranslatedText() {
        return translatedText;
    }

    @Remove
    public void remove() {

    }

    public int getTranslatedSentences() {
       return translator.getTransaltedSentenced();
    }
}
