package net.unknownuser.fanjcontrol;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.node.*;
import sun.misc.*;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

@JsonDeserialize(using = Configuration.ConfigurationDeserializer.class)
public class Configuration {
	protected int                interval;
	protected FanConfiguration[] controls;
	
	@JsonIgnore
	protected boolean isShutdown = false;
	
	static class ConfigurationDeserializer extends StdDeserializer<Configuration> {
		public ConfigurationDeserializer(final Class<?> vc) {
			super(vc);
		}
		
		public ConfigurationDeserializer() {
			this(null);
		}
		
		@Override
		public Configuration deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {
			ObjectCodec codec = parser.getCodec();
			JsonNode    root  = codec.readTree(parser);
			
			Configuration configuration = new Configuration();
			
			// s to ms
			configuration.interval = (int) round(root.get("interval").asDouble() * 1000);
			
			int len = root.get("controls").size();
			configuration.controls = new FanConfiguration[len];
			int i = 0;
			
			try (JsonParser p = root.get("controls").traverse(codec)) {
				p.nextToken();
				final JsonNode           jsonNode   = context.readTree(p);
				final Iterator<JsonNode> elements   = jsonNode.elements();
				final Iterator<String>   fieldNames = jsonNode.fieldNames();
				
				while (elements.hasNext()) {
					JsonNode node = elements.next();
					
					// JsonNode is final, ObjectNode is not
					((ObjectNode) node).put("pwm_input", fieldNames.next());
					
					final FanConfiguration fanConfiguration = context.readTreeAsValue(node, FanConfiguration.class);
					configuration.controls[i] = fanConfiguration;
					i++;
				}
			}
			
			return configuration;
		}
	}
	
	/**
	 * Starts all necessary threads and registers the shutdown hook.
	 * Returns control back.
	 */
	public void start() {
		Timer timer = new Timer();
		
		// this cannot be a lambda, TimerTask has more methods than just run
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				updateControls();
			}
		};
		
		timer.schedule(task, 0, this.interval);
		
		Signal.handle(new Signal("QUIT"), signal -> this.shutdown());
		Signal.handle(new Signal("TERM"), signal -> this.shutdown());
		Signal.handle(new Signal("HUP"), signal -> this.shutdown());
		Signal.handle(new Signal("INT"), signal -> this.shutdown());
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}
	
	private void updateControls() {
		for (FanConfiguration control : this.controls) {
			try {
				control.update();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void shutdown() {
		System.out.println("Starting shutdown...");
		this.isShutdown = true;
		for (FanConfiguration control : this.controls) {
			try {
				control.shutdown();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
