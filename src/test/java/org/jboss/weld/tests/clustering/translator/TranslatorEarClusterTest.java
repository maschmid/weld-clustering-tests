package org.jboss.weld.tests.clustering.translator;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.clustering.ClusterTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;

@RunWith(Arquillian.class)
@RunAsClient
public class TranslatorEarClusterTest extends ClusterTestBase
{
   private static final String MAIN_PAGE = "home.jsf";

   private static final By INPUT_AREA = By.id("TranslatorMain:text");
   private static final By TRANSLATE_BUTTON = By.id("TranslatorMain:button");
   private static final String ONE_SENTENCE = "This is only one sentence.";
   private static final String MORE_SENTENCES = "First sentence. Second and last sentence.";
   private static final String ONE_SENTENCE_TRANSLATED = "Lorem ipsum dolor sit amet.";
   private static final String MORE_SENTENCES_TRANSLATED = "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.";

   public static EnterpriseArchive createTestDeployment() {
	   
       WebArchive war = ShrinkWrap
    		  .create(WebArchive.class, "weld-translator.war")
    		  .addAsWebResource("translator/home.xhtml", "home.xhtml")
              .addAsWebResource("translator/index.html", "index.html")
              .addAsWebResource("translator/template.xhtml", "template.xhtml")
              .addAsWebInfResource("translator/WEB-INF/beans.xml", "beans.xml")
              .addAsWebInfResource("translator/WEB-INF/faces-config.xml", "faces-config.xml")
              .addAsWebInfResource("translator/WEB-INF/web.xml", "web.xml");
       
       JavaArchive ejbJar = ShrinkWrap
    	      .create(JavaArchive.class, "weld-translator.jar")
    	      .addClasses(SentenceParser.class, SentenceTranslator.class, TextTranslator.class, Translator.class, TranslatorController.class, TranslatorControllerBean.class)
    	      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");


       EnterpriseArchive ear = ShrinkWrap
       .create(EnterpriseArchive.class, "translator.ear")
       .addAsApplicationResource("translator/application.xml", "application.xml");

       ear.addAsModule(war);
       ear.addAsModule(ejbJar);
       
       return ear;
   }

   @Deployment(name = DEPLOYMENT1, managed=false, testable=false)
   @TargetsContainer(CONTAINER1)
   public static EnterpriseArchive createTestDeployment1() {
      return createTestDeployment();
   }

   @Deployment(name = DEPLOYMENT2, managed=false, testable=false)
   @TargetsContainer(CONTAINER2)
   public static EnterpriseArchive createTestDeployment2() {
      return createTestDeployment()
           .addAsManifestResource(EmptyAsset.INSTANCE, "force-hashcode-change.txt");
   }

   @Test
   public void translatorTest() throws MalformedURLException, InterruptedException {
      controller.start(CONTAINER1);
      deployer.deploy(DEPLOYMENT1);

      controller.start(CONTAINER2);
      deployer.deploy(DEPLOYMENT2);

      driver.navigate().to(new URL(contextPath1 + "/" + MAIN_PAGE));
      
      driver.findElement(INPUT_AREA).clear();
      driver.findElement(INPUT_AREA).sendKeys(ONE_SENTENCE);
      driver.findElement(TRANSLATE_BUTTON).click();
      assertTrue("One sentence translated into latin expected.", driver.getPageSource().contains(ONE_SENTENCE_TRANSLATED));
      
      driver.findElement(INPUT_AREA).clear();
      driver.findElement(INPUT_AREA).sendKeys(MORE_SENTENCES);
      driver.findElement(TRANSLATE_BUTTON).click();
      assertTrue("More sentences translated into latin expected.", driver.getPageSource().contains(MORE_SENTENCES_TRANSLATED));
      
      assertTrue("Expected 3 sentences translated", driver.getPageSource().contains("3 sentences translated."));

      deployer.undeploy(DEPLOYMENT1);
      controller.stop(CONTAINER1);

      Thread.sleep(GRACE_TIME_TO_REPLICATE);

      String address = getAddressForSecondInstance();
      driver.navigate().to(new URL(contextPath2 + "/" + address));

      assertTrue("Expected 3 sentences translated", driver.getPageSource().contains("3 sentences translated."));
      
      driver.findElement(INPUT_AREA).clear();
      driver.findElement(INPUT_AREA).sendKeys(ONE_SENTENCE);
      driver.findElement(TRANSLATE_BUTTON).click();
      assertTrue("One sentence translated into latin expected.", driver.getPageSource().contains(ONE_SENTENCE_TRANSLATED));
      
      driver.findElement(INPUT_AREA).clear();
      driver.findElement(INPUT_AREA).sendKeys(MORE_SENTENCES);
      driver.findElement(TRANSLATE_BUTTON).click();
      assertTrue("More sentences translated into latin expected.", driver.getPageSource().contains(MORE_SENTENCES_TRANSLATED));
      
      assertTrue("Expected 6 sentences translated", driver.getPageSource().contains("6 sentences translated."));

      deployer.undeploy(DEPLOYMENT2);
      controller.stop(CONTAINER2);
   }
}
