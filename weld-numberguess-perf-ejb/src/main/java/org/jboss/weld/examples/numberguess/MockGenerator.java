package org.jboss.weld.examples.numberguess;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

@ApplicationScoped
@Alternative
public class MockGenerator {
    @Produces
    @Random
    int next() {
        return 13;
    }

    @Produces
    @MaxNumber
    int getMaxNumber() {
        return 100;
    }
}
