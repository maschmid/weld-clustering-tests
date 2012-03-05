package org.jboss.weld.tests.clustering.numberguess.decorator;

import javax.ejb.Local;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

@Local
public interface GameLocal extends Game {

    void reset();

    void check();

    int getRemainingGuesses();

    int getBiggest();

    int getSmallest();

    void setGuess(int guess);

    int getGuess();

    int getNumber();

    void validateNumberRange(FacesContext context, UIComponent toValidate, Object value);
}
