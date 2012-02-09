package org.jboss.weld.deploy.test;

import java.io.File;
import java.io.IOException;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DeployTest {
	
	private final static String DEPLOYMENT_NAME="weld-numberguess";
	private final static String ARCHIVE_NAME="weld-numberguess.war";
	private final static String BUILD_DIRECTORY="target";

	@ArquillianResource
	private Deployer deployer;
	
	@Deployment(name=DEPLOYMENT_NAME,testable=false,managed=false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(ZipImporter.class, ARCHIVE_NAME).importFrom(new File(BUILD_DIRECTORY + '/' + ARCHIVE_NAME))
                .as(WebArchive.class);
    }
	
	@Test
	public void doDeployUndeploy() throws InterruptedException, IOException{
		deployer.deploy(DEPLOYMENT_NAME);
		deployer.undeploy(DEPLOYMENT_NAME);		
	}
		
}
