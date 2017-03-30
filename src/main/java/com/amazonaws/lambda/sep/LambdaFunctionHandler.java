package com.amazonaws.lambda.sep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import quote.GetQuote;
import sep.lucene.SearchFiles;
import sep.lucene.SearchResult;


public class LambdaFunctionHandler implements RequestStreamHandler {
	//create ObjectMapper instance
	private ObjectMapper objectMapper;
	private GetQuote getQuote = new GetQuote();
    private SearchFiles searchFiles = new SearchFiles();
	
	public LambdaFunctionHandler() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
	}
	
    @SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		try {
            Map<String, Object> request = objectMapper.readValue(input, new TypeReference<Map<String, Object>>() {});

			Map<String, Object> params = (Map<String, Object>)request.get("params");
            Map<String, Object> querystring = (Map<String, Object>)params.get("querystring");
            String action = (String)querystring.get("action");
			QuoteResponse response = new QuoteResponse();
			if ( action != null && action.toLowerCase().trim().equals("quote")) {
				SearchResult searchResult = getQuote.getEntryFromStanford();
				response.subject = searchResult.subject; 
				response.preamble = searchResult.preamble; 
				response.url = searchResult.url; 
				if (response.preamble.length() > 290 ) {
					response.preamble = response.preamble.substring(0,  290); 					
				}
				objectMapper.writeValue(output, response);
			} else if ( action != null && action.toLowerCase().trim().equals("search")) {
	            String phrase = (String)querystring.get("phrase");
		        if (phrase == null || phrase.isEmpty()) {
					response.subject = "No Search Phrase"; 
					response.preamble = "No Search Phrase Found"; 
					response.url = ""; 
		        } else {
					try {
						List<SearchResult> searchResults = searchFiles.query(phrase);
						if ( searchResults.size() > 0 ) {
							// Create the plain text output
							SearchResult searchResult = searchResults.get(0);
//						    PingSEP pingSEP = new PingSEP(searchResult.url);  
//							new Thread(pingSEP).start();
							
							response.subject = "Results for " + phrase + ".";
							response.preamble = searchResult.preamble;
							response.url = "https://plato.stanford.edu/archives/win2016/"+searchResult.url;
						} else {
							response.subject = "Search for " + phrase + ".";
							response.preamble = "Sorry, nothing found for " + phrase + ".";
//							log.info("No results for " + searchPhrase);
						}
					} catch (ParseException | IOException e) {
						response.subject = "Results for " + phrase + ".";
						response.preamble = e.getLocalizedMessage();
						response.url = "";
					}
		        }
				
				if (response.preamble.length() > 290 ) {
					response.preamble = response.preamble.substring(0,  290); 					
				}
				objectMapper.writeValue(output, response);

			} else {
				objectMapper.writeValue(output, params);
			}		
		} catch (JsonParseException e) {
			try {
				output.write(e.getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ignored) {}
		} catch (JsonMappingException e) {
			try {
				output.write(e.getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ignored) {}
		} catch (IOException e) {
			try {
				output.write(e.getLocalizedMessage().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ignored) {}
		}
	}
	
	public static void main(String... strings) throws Exception {
//		String input = "{\"action\":\"quote\", \"query\":\"frege\"}";
//		String input = "{\"action\":\"quote\"}";
//		String input = "{'action':'quote'}";
//		String input = new String(Files.readAllBytes(Paths.get(LambdaFunctionHandler.class.getResource("/input.txt").toURI())));
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 
		new LambdaFunctionHandler().handleRequest( 
			new ByteArrayInputStream(Files.readAllBytes(Paths.get(LambdaFunctionHandler.class.getResource("/input.txt").toURI()))), 
			output, 
			null
		);
		System.out.println(output.toString());
	}
/*	
	public static class RequestCommand {
		public String action;
		public String query;
		@Override
		public String toString() {
			return "[" + action + ", " + query + "]";
		}
	}
*/		
	public static class QuoteResponse {
		public String subject;
		public String preamble;
		public String url;
		@Override
		public String toString() {
			return "[" + subject + ", " + preamble + ", " + url + "]";
		}
	}
}
