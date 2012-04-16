package org.jboss.weld.tests.clustering;

import java.net.MalformedURLException;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;

public abstract class ClusterTestBase
{
   protected static final long GRACE_TIME_TO_REPLICATE = 1000;
   protected static final long GRACE_TIME_TO_MEMBERSHIP_CHANGE = 5000;
   
   protected static final String CONTAINER1 = "container1"; 
   protected static final String CONTAINER2 = "container2"; 
   protected static final String DEPLOYMENT1 = "dep.container1";
   protected static final String DEPLOYMENT2 = "dep.container2";
   
   @ArquillianResource
   protected ContainerController controller;

   @ArquillianResource
   protected Deployer deployer;

   @Drone
   protected AjaxSelenium selenium;
   
   protected String contextPath1;
   protected String contextPath2;
   
   @Before
   public void before() throws MalformedURLException {
       // We can't use @ArquillianResource URL here as we are using unmanaged deployments
       contextPath1 = System.getProperty("node1.contextPath");
       contextPath2 = System.getProperty("node2.contextPath");
   }

   public String getAddressForSecondInstance() {
      String loc = selenium.getLocation().toString();
      String[] parsedStrings = loc.split("/");
      StringBuilder sb = new StringBuilder();
      for (int i = 4; i < parsedStrings.length; i++) {
          sb.append("/").append(parsedStrings[i]);
      }

      String newAddress = sb.toString();
      String firstPart = "";
      String sid = "";         
      
      //if (selenium.isCookiePresent("JSESSIONID")) {
      if (!newAddress.contains(";")) {
          sid = selenium.getCookieByName("JSESSIONID").getValue();
          firstPart = newAddress;
      } else {
          // get sessionid directly from browser URL if JSESSIONID cookie is not
          // present
          firstPart = newAddress.substring(0, newAddress.indexOf(";"));
          sid = loc.substring(loc.indexOf("jsessionid=") + "jsessionid=".length(), loc.length());
      }

      newAddress = firstPart + ";jsessionid=" + sid;
              
      selenium.deleteAllVisibleCookies();

      return newAddress;
  }
}
