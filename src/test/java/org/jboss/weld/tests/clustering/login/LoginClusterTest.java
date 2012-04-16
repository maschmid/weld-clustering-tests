package org.jboss.weld.tests.clustering.login;

import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.clustering.ClusterTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class LoginClusterTest extends ClusterTestBase
{
   protected String MAIN_PAGE = "home.jsf";
   
   protected XPathLocator LOGGED_IN = xp("//li[contains(text(),'Welcome')]");
   protected XPathLocator LOGGED_OUT = xp("//li[contains(text(),'Goodbye')]");
   
   protected IdLocator USERNAME_FIELD = id("loginForm:username");
   protected IdLocator PASSWORD_FIELD = id("loginForm:password");

   protected IdLocator LOGIN_BUTTON = id("loginForm:login");
   protected IdLocator LOGOUT_BUTTON = id("loginForm:logout");

   public static WebArchive createTestDeployment() {
      return ShrinkWrap.create(WebArchive.class, "weld-clustering-tests.war")
              .addClasses(Credentials.class, EJBUserManager.class, LoggedIn.class, Login.class, ManagedBeanUserManager.class, Resources.class, User.class, UserManager.class)
              .addAsWebResource("login/home.xhtml", "home.xhtml")
              .addAsWebResource("login/index.html", "index.html")
              .addAsWebResource("login/template.xhtml", "template.xhtml")
              .addAsWebResource("login/users.xhtml", "users.xhtml")
              .addAsWebInfResource("login/WEB-INF/beans.xml", "beans.xml")
              .addAsWebInfResource("login/WEB-INF/faces-config.xml", "faces-config.xml")
              .addAsWebInfResource("login/WEB-INF/web.xml", "web.xml")
              .addAsWebInfResource("login/import.sql", "classes/import.sql")
              .addAsWebInfResource("login/META-INF/persistence.xml", "classes/META-INF/persistence.xml");
   }

   @Deployment(name = DEPLOYMENT1, managed=false, testable=false)
   @TargetsContainer(CONTAINER1)
   public static WebArchive createTestDeployment1() {
      return createTestDeployment();
   }

   @Deployment(name = DEPLOYMENT2, managed=false, testable=false)
   @TargetsContainer(CONTAINER2)
   public static WebArchive createTestDeployment2() {
      return createTestDeployment()
           .addAsWebInfResource(EmptyAsset.INSTANCE, "force-hashcode-change.txt");
   }

   @Test
   public void loginAndOutTest() throws MalformedURLException, InterruptedException {
      controller.start(CONTAINER1);
      deployer.deploy(DEPLOYMENT1);

      controller.start(CONTAINER2);
      deployer.deploy(DEPLOYMENT2);

      selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));

      waitModel.until(elementPresent.locator(USERNAME_FIELD));
      assertFalse("User should not be logged in!", selenium.isElementPresent(LOGGED_IN));
      selenium.type(USERNAME_FIELD, "demo");
      selenium.type(PASSWORD_FIELD, "demo");
      waitForHttp(selenium).click(LOGIN_BUTTON);
      assertTrue("User should be logged in!", selenium.isElementPresent(LOGGED_IN));

      deployer.undeploy(DEPLOYMENT1);
      controller.stop(CONTAINER1);

      Thread.sleep(GRACE_TIME_TO_REPLICATE);

      String address = getAddressForSecondInstance();
      selenium.open(new URL(contextPath2 + "/" + address));
      
      assertTrue("User should be logged in!", selenium.isElementPresent(LOGOUT_BUTTON));
      waitForHttp(selenium).click(LOGOUT_BUTTON);
      assertTrue("User should not be logged in!", selenium.isElementPresent(LOGGED_OUT));

      deployer.undeploy(DEPLOYMENT2);
      controller.stop(CONTAINER2);
   }
}
