package org.jboss.weld.examples.numberguess;

public interface Game {

    void reset();

    void check();

    int getRemainingGuesses();

    int getBiggest();

    int getSmallest();

    void setGuess(int guess);

    int getGuess();

    int getNumber();

}
