package org.jboss.weld.tests.clustering.numberguess.plain;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.clustering.numberguess.NumberGuessClusterTest;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class PlainNumberGuessClusterTest extends NumberGuessClusterTest {
	
	 public static WebArchive createTestDeployment() {
		 return ShrinkWrap.create(WebArchive.class, "weld-clustering-tests.war")
				 .addPackage(Game.class.getPackage())
				 .addAsWebResource("home.xhtml")
				 .addAsWebResource("index.html")
				 .addAsWebResource("template.xhtml")
				 .addAsWebInfResource("WEB-INF/beans-plain.xml", "beans.xml")
				 .addAsWebInfResource("WEB-INF/faces-config.xml")
				 .addAsWebInfResource("WEB-INF/web.xml");
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
}
