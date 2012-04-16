package org.jboss.weld.tests.clustering.translator;

import javax.ejb.Local;

@Local
public interface Translator {

    String translate(String sentence);

}
