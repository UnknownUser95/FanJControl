package net.unknownuser.fanjcontrol;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.deser.std.*;

import java.io.*;

@JsonDeserialize(using = PWMConfiguration.PWMConfigurationDeserializer.class)
public class PWMConfiguration {
	public byte minimum;
	public byte maximum;
	public byte fan_start;
	public byte fan_stop;
	
	static class PWMConfigurationDeserializer extends StdDeserializer<PWMConfiguration> {
		public PWMConfigurationDeserializer(final Class<?> vc) {
			super(vc);
		}
		
		public PWMConfigurationDeserializer() {
			this(null);
		}
		
		@Override
		public PWMConfiguration deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {
			final JsonNode root = parser.getCodec().readTree(parser);
			
			PWMConfiguration pwmConfiguration = new PWMConfiguration();
			
			pwmConfiguration.minimum   = (byte) root.get("minimum").asInt();
			pwmConfiguration.maximum   = (byte) root.get("maximum").asInt();
			pwmConfiguration.fan_start = (byte) root.get("fan_start").asInt();
			pwmConfiguration.fan_stop  = (byte) root.get("fan_stop").asInt();
			
			return pwmConfiguration;
		}
	}
}
