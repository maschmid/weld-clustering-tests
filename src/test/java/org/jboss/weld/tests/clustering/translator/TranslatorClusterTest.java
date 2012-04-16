package org.jboss.weld.tests.clustering.translator;

import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.ajocado.locator.IdLocator;
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
public class TranslatorClusterTest extends ClusterTestBase
{
   protected String MAIN_PAGE = "home.jsf";

   protected IdLocator INPUT_AREA = id("TranslatorMain:text");
   protected IdLocator TRANSLATE_BUTTON = id("TranslatorMain:button");
   protected String ONE_SENTENCE = "This is only one sentence.";
   protected String MORE_SENTENCES = "First sentence. Second and last sentence.";
   protected String ONE_SENTENCE_TRANSLATED = "Lorem ipsum dolor sit amet.";
   protected String MORE_SENTENCES_TRANSLATED = "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.";

   public static WebArchive createTestDeployment() {
      return ShrinkWrap.create(WebArchive.class, "weld-clustering-tests.war")
              .addClasses(SentenceParser.class, SentenceTranslator.class, TextTranslator.class, Translator.class, TranslatorController.class, TranslatorControllerBean.class)
              .addAsWebResource("translator/home.xhtml", "home.xhtml")
              .addAsWebResource("translator/index.html", "index.html")
              .addAsWebResource("translator/template.xhtml", "template.xhtml")
              .addAsWebInfResource("translator/WEB-INF/beans.xml", "beans.xml")
              .addAsWebInfResource("translator/WEB-INF/faces-config.xml", "faces-config.xml")
              .addAsWebInfResource("translator/WEB-INF/web.xml", "web.xml");
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
   public void translatorTest() throws MalformedURLException, InterruptedException {
      controller.start(CONTAINER1);
      deployer.deploy(DEPLOYMENT1);

      controller.start(CONTAINER2);
      deployer.deploy(DEPLOYMENT2);

      selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));
      
      selenium.type(INPUT_AREA, ONE_SENTENCE);
      waitForHttp(selenium).click(TRANSLATE_BUTTON);
      assertTrue("One sentence translated into latin expected.", selenium.isTextPresent(ONE_SENTENCE_TRANSLATED));
      selenium.type(INPUT_AREA, MORE_SENTENCES);
      waitForHttp(selenium).click(TRANSLATE_BUTTON);
      assertTrue("More sentences translated into latin expected.", selenium.isTextPresent(MORE_SENTENCES_TRANSLATED));
      
      assertTrue("Expected 3 sentences translated", selenium.isTextPresent("3 sentences translated."));

      deployer.undeploy(DEPLOYMENT1);
      controller.stop(CONTAINER1);

      Thread.sleep(GRACE_TIME_TO_REPLICATE);

      String address = getAddressForSecondInstance();
      selenium.open(new URL(contextPath2 + "/" + address));

      assertTrue("Expected 3 sentences translated", selenium.isTextPresent("3 sentences translated."));
      
      selenium.type(INPUT_AREA, ONE_SENTENCE);
      waitForHttp(selenium).click(TRANSLATE_BUTTON);
      assertTrue("One sentence translated into latin expected.", selenium.isTextPresent(ONE_SENTENCE_TRANSLATED));
      selenium.type(INPUT_AREA, MORE_SENTENCES);
      waitForHttp(selenium).click(TRANSLATE_BUTTON);
      assertTrue("More sentences translated into latin expected.", selenium.isTextPresent(MORE_SENTENCES_TRANSLATED));
      
      assertTrue("Expected 6 sentences translated", selenium.isTextPresent("6 sentences translated."));

      deployer.undeploy(DEPLOYMENT2);
      controller.stop(CONTAINER2);
   }
}
