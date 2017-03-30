package com.amazonaws.lambda.sep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerTest {

    private static final String EXPECTED_OUTPUT_STRING = "{\"path\":{},\"querystring\":{\"action\":\"test\"},\"header\":{}}";
    private static final String EXPECTED_OUTPUT_SEARCH_VALID = 
    		"{\"subject\":\"Results for frege.\",\"preamble\":\"Friedrich Ludwig Gottlob Frege (b. 1848, d. 1925) was a German mathematician, logician, and philosopher who worked at the University of Jena. Frege essentially reconceived the discipline of logic by constructing a formal system which, in effect, constituted the first ‘predicate calculus’. \",\"url\":\"https://plato.stanford.edu/archives/win2016/entries/frege/\"}";
    private static final String EXPECTED_OUTPUT_SEARCH_INVALID = "{\"subject\":\"Search for xxx.\",\"preamble\":\"Sorry, nothing found for xxx.\",\"url\":null}";
    private LambdaFunctionHandler handler = new LambdaFunctionHandler();


    @Test
    public void testTest() throws IOException, URISyntaxException {

        InputStream input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(LambdaFunctionHandlerTest.class.getResource("/inputTest.txt").toURI())));
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, null);

        String sampleOutputString = output.toString();
        Assert.assertEquals(EXPECTED_OUTPUT_STRING, sampleOutputString);
    }
    @Test
    public void testSearchValid() throws IOException, URISyntaxException {

        InputStream input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(LambdaFunctionHandlerTest.class.getResource("/inputSearchValid.txt").toURI())));
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, null);

        String sampleOutputString = output.toString();
        Assert.assertEquals(EXPECTED_OUTPUT_SEARCH_VALID, sampleOutputString);
    }
    @Test
    public void testSearchInvalid() throws IOException, URISyntaxException {

        InputStream input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(LambdaFunctionHandlerTest.class.getResource("/inputSearchInvalid.txt").toURI())));
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, null);

        String sampleOutputString = output.toString();
        Assert.assertEquals(EXPECTED_OUTPUT_SEARCH_INVALID, sampleOutputString);
    }
    @Test
    public void testQuote() throws IOException, URISyntaxException {

        InputStream input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(LambdaFunctionHandlerTest.class.getResource("/inputQuote.txt").toURI())));
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, null);

        String sampleOutputString = output.toString();
        Assert.assertTrue("Excepted Subject", sampleOutputString.contains("subject"));
        Assert.assertTrue("Excepted Preamble", sampleOutputString.contains("preamble"));
        Assert.assertTrue("Excepted URL", sampleOutputString.contains("url"));
    }

}
