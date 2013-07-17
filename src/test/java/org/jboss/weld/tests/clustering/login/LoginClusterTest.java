package org.jboss.weld.tests.clustering.login;

import java.net.MalformedURLException;
import java.net.URL;

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
import org.openqa.selenium.By;

@RunWith(Arquillian.class)
@RunAsClient
public class LoginClusterTest extends ClusterTestBase
{
   protected String MAIN_PAGE = "home.jsf";
   
   private static final By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
   private static final By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");
   
   private static final By USERNAME_FIELD = By.id("loginForm:username");
   private static final By PASSWORD_FIELD = By.id("loginForm:password");

   private static final By LOGIN_BUTTON = By.id("loginForm:login");
   private static final By LOGOUT_BUTTON = By.id("loginForm:logout");

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
      Thread.sleep(1000);
      deployer.deploy(DEPLOYMENT1);

      controller.start(CONTAINER2);
      Thread.sleep(1000);
      deployer.deploy(DEPLOYMENT2);

      driver.navigate().to(new URL(contextPath1 + "/" + MAIN_PAGE));
      //selenium.open();

      checkElementPresent(driver, USERNAME_FIELD, "username field should be present");
      
      checkElementNotPresent(driver, LOGGED_IN, "User should not be logged in!");
      
      driver.findElement(USERNAME_FIELD).sendKeys("demo");
      driver.findElement(PASSWORD_FIELD).sendKeys("demo");
      driver.findElement(LOGIN_BUTTON).click();

      checkElementPresent(driver, LOGGED_IN, "User should be logged in!");

      deployer.undeploy(DEPLOYMENT1);
      controller.stop(CONTAINER1);

      Thread.sleep(GRACE_TIME_TO_REPLICATE);

      String address = getAddressForSecondInstance();
      driver.navigate().to(new URL(contextPath2 + "/" + address));
      
      checkElementPresent(driver, LOGOUT_BUTTON, "User should be logged in!");
      driver.findElement(LOGOUT_BUTTON).click();
      checkElementPresent(driver, LOGGED_OUT, "User should not be logged in!");

      deployer.undeploy(DEPLOYMENT2);
      controller.stop(CONTAINER2);
   }
}
