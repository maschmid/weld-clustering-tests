/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.clustering.numberguess;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.junit.InSequence;
import org.jboss.weld.tests.clustering.ClusterTestBase;
import org.junit.Test;
import org.junit.Ignore;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author mgencur
 * @author kpiwko
 * @author maschmid
 *
 */
public abstract class NumberGuessClusterTest extends ClusterTestBase {
   
    protected String MAIN_PAGE = "home.jsf";
    
    public static final long GRACE_TIME_TO_REPLICATE = 1000;
    
    private static final By GUESS_MESSAGES = By.id("numberGuess:messages");
    private static final By GUESS_STATUS = By.xpath("//div[contains(text(),'I'm thinking of ')]");
    
    private static final By GUESS_FIELD = By.id("numberGuess:inputGuess");
    private static final By GUESS_FIELD_WITH_VALUE = By.xpath("//input[@id='numberGuess:inputGuess'][@value=3]");

    private static final  By GUESS_SUBMIT = By.id("numberGuess:guessButton");
    private static final  By GUESS_RESTART = By.id("numberGuess:restartButton");
    private static final  By GUESS_SMALLEST = By.id("numberGuess:smallest");
    private static final  By GUESS_BIGGEST = By.id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    protected String HIGHER_MSG = "Higher!";
    protected String LOWER_MSG = "Lower!";

    protected Pattern guessesNumberPattern = Pattern.compile("You have (\\d+) guesses remaining."); 

    private GameState gameState;

    boolean browsersSwitched = false;

    protected void resetForm() {
        driver.findElement(GUESS_RESTART).click();
        gameState = null;
    }

    protected void enterGuess(int guess) throws InterruptedException {
        gameState.setGuess(guess);
        driver.findElement(GUESS_FIELD).clear();
        driver.findElement(GUESS_FIELD).sendKeys(String.valueOf(guess));
        driver.findElement(GUESS_SUBMIT).click();
    }

    protected boolean isOnGuessPage() {
        return !(isOnWinPage() || isOnLosePage());
    }

    protected boolean isOnWinPage() {
        String text = driver.findElement(GUESS_MESSAGES).getText();
        return WIN_MSG.equals(text);
    }

    protected boolean isOnLosePage() {
        String text = driver.findElement(GUESS_MESSAGES).getText();
        return LOSE_MSG.equals(text);
    }

    private Integer getRemainingGuesses() {
        Matcher m = guessesNumberPattern.matcher(driver.getPageSource());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        else {
            return null;
        }
    }

    /**
     * Asserts the game state matches what the page displays
     */
    private void updateGameState() {

        GameState nextState = new GameState();
        nextState.setRemainingGuesses(getRemainingGuesses());
        nextState.setLargest(Integer.parseInt(driver.findElement(GUESS_BIGGEST).getText()));
        nextState.setSmallest(Integer.parseInt(driver.findElement(GUESS_SMALLEST).getText()));

        if (gameState == null) {
            // Initial state
            assertEquals("Remaining guesses dosn't match", 10, nextState.getRemainingGuesses());
            assertEquals("Smallest numbers dosn't match", 0, nextState.getSmallest());
            assertEquals("Largest numbers dosn't match", 100, nextState.getLargest());
        }
        else {
            nextState.setPreviousGuess(gameState.getGuess());
            assertEquals("Remaining guesses dosn't match", gameState.getRemainingGuesses() - 1, nextState.getRemainingGuesses());

            boolean higher = driver.getPageSource().contains(HIGHER_MSG);
            boolean lower = driver.getPageSource().contains(LOWER_MSG);
            
            assertEquals(lower, (nextState.getLargest() < gameState.getLargest()));
            if (gameState.getGuess() != 0) {
                // Bug in numberguess, doesn't display "higher" for zero guess.
                assertEquals(higher, (nextState.getSmallest() > gameState.getSmallest()));
            }
            assertTrue(!lower || !higher);
        }
 
        gameState = nextState;
    }
    
    private void naiveStep() throws InterruptedException {
        updateGameState();
        enterGuess(gameState.getSmallest());
    }
    
    private void smartStep() throws InterruptedException {
        updateGameState();
        enterGuess(gameState.getSmallest() + ((gameState.getLargest() - gameState.getSmallest()) / 2));
    }
    
    private void switchBrowsers() throws MalformedURLException {
        String address = getAddressForSecondInstance();
        String contextPath = browsersSwitched ? contextPath1 : contextPath2;
        driver.navigate().to(new URL(contextPath + "/" + address));
        
        browsersSwitched = !browsersSwitched;
    }
    
    @Test
    @InSequence(1)
    public void guessingWithFailoverTest() throws MalformedURLException, InterruptedException {
              
        controller.start(CONTAINER1);
        deployer.deploy(DEPLOYMENT1);
                
        controller.start(CONTAINER2);        
        deployer.deploy(DEPLOYMENT2);
        
        driver.navigate().to(new URL(contextPath1 + "/" + MAIN_PAGE));
        
        // we always want to enter at least 3 guesses so that we can continue
        // in the other browser window with expected results
        do {
            resetForm();
            for (int i = 0; i < 3 && isOnGuessPage(); ++i) {
                naiveStep();
            }
        } while(!isOnGuessPage());
        
        deployer.undeploy(DEPLOYMENT1);
        controller.stop(CONTAINER1);
        
        Thread.sleep(GRACE_TIME_TO_REPLICATE);
        
        switchBrowsers();
        
        while(isOnGuessPage()) {
            smartStep();
        }
        
        assertTrue("Win page expected after playing smart.", isOnWinPage());
        
        deployer.undeploy(DEPLOYMENT2);
        controller.stop(CONTAINER2);
    }
    
    @Test
    @InSequence(2)
    public void guessingWithInterleavingTest() throws MalformedURLException, InterruptedException {
        controller.start(CONTAINER1);
        deployer.deploy(DEPLOYMENT1);
        
        driver.navigate().to(new URL(contextPath1 + "/" + MAIN_PAGE));
         
        for(;;) {
            
            smartStep();
            
            if (!isOnGuessPage()) {
                break;
            }
            
            if (browsersSwitched) {
                controller.start(CONTAINER1);
                deployer.deploy(DEPLOYMENT1);
                
                Thread.sleep(GRACE_TIME_TO_MEMBERSHIP_CHANGE);
                
                deployer.undeploy(DEPLOYMENT2);
                controller.stop(CONTAINER2);
            }
            else {
                controller.start(CONTAINER2);
                deployer.deploy(DEPLOYMENT2);
                
                Thread.sleep(GRACE_TIME_TO_MEMBERSHIP_CHANGE);
                
                deployer.undeploy(DEPLOYMENT1);
                controller.stop(CONTAINER1);
            }
            
            Thread.sleep(GRACE_TIME_TO_REPLICATE);
            
            switchBrowsers();
        }
        
        assertTrue("Win page expected after playing smart.", isOnWinPage());
        
        if (browsersSwitched) {
            deployer.undeploy(DEPLOYMENT2);
            controller.stop(CONTAINER2);
        }
        else {
            deployer.undeploy(DEPLOYMENT1);
            controller.stop(CONTAINER1);
        }
    }
}
