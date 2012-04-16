package org.jboss.weld.tests.clustering.numberguess.decorator;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Mostly useless Stateless EJB, just so we have a stateless EJB in the test
 * @author maschmid
 *
 */
@Stateless
public class RandomStatelessEjb
{
   @Inject
   @Random
   Instance<Integer> randomNumber;
   
   public int getRandom() {
      return randomNumber.get();
   }
}
