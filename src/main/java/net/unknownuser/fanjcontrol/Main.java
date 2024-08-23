package net.unknownuser.fanjcontrol;

import com.fasterxml.jackson.databind.*;

import java.io.*;

public class Main {
	public static void main(String[] args) throws IOException {
		ObjectMapper        mapper        = new ObjectMapper();
		final Configuration configuration = mapper.readValue(new File("config.json"), Configuration.class);
		
		configuration.start();
		
		System.out.println("started");
		System.out.println(ProcessHandle.current().pid());
	}
}
