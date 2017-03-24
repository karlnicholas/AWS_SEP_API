package com.amazonaws.lambda.sep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import quote.GetQuote;


public class LambdaFunctionHandler implements RequestStreamHandler {
	//create ObjectMapper instance
	private ObjectMapper objectMapper = new ObjectMapper();
	private GetQuote getQuote = new GetQuote();
	

	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		RequestCommand command;
		try {
			command = objectMapper.readValue(input, RequestCommand.class);
			if ( command.action != null && command.action.toLowerCase().trim().equals("quote")) {
				output.write(getQuote.getEntryFromStanford().preamble.getBytes(StandardCharsets.UTF_8));
			} else {
				objectMapper.writeValue(output, command);
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
		String input = "{\"action\":\"quote\"}";
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 
		new LambdaFunctionHandler().handleRequest( 
			new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), 
			output, 
			null
		);
		System.out.println(output.toString());
	}
	
	public static class RequestCommand {
		public String action;
		public String query;
		@Override
		public String toString() {
			return "[" + action + ", " + query + "]";
		}
	}
		
}
