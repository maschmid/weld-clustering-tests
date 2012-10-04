package org.jboss.weld.tests.clustering.numberguess.plain;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@SessionScoped
public class Win implements Serializable {
	public void win() {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Correct!"));
	}
}
