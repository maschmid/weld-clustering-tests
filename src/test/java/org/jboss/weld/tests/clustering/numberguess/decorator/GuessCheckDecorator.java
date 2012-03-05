package org.jboss.weld.tests.clustering.numberguess.decorator;

import java.io.Serializable;
import java.text.MessageFormat;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public abstract class GuessCheckDecorator implements Game, Serializable {

    private static final long serialVersionUID = -7412173263436052423L;

    @Inject
    @Delegate
    private Game delegate;

    public void setGuess(int guess) {
        if (guess < delegate.getSmallest() || guess > delegate.getBiggest()) {
            throw new IllegalArgumentException(MessageFormat.format("Invalid guess. {0} is not within {1} and {2}.", guess,
                    delegate.getSmallest(), delegate.getBiggest()));
        }
        delegate.setGuess(guess);
    }

}
