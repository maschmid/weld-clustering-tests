package org.jboss.weld.examples.numberguess;

import javax.ejb.Local;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

@Local
public interface Game {

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
