package net.unknownuser.fanjcontrol;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.deser.std.*;

import java.io.*;

import static net.unknownuser.fanjcontrol.Common.*;

@JsonDeserialize(using = TemperatureConfiguration.TemperatureConfigurationDeserializer.class)
public class TemperatureConfiguration {
	@JsonIgnore
	public RandomAccessFile input;
	public byte             minimum;
	public byte             maximum;
	
	@JsonIgnore
	public FixedSizeByteList values;
	@JsonIgnore
	public byte              delta;
	
	static class TemperatureConfigurationDeserializer extends StdDeserializer<TemperatureConfiguration> {
		public TemperatureConfigurationDeserializer(final Class<?> vc) {
			super(vc);
		}
		
		public TemperatureConfigurationDeserializer() {
			this(null);
		}
		
		@Override
		public TemperatureConfiguration deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {
			final JsonNode root = parser.getCodec().readTree(parser);
			
			TemperatureConfiguration temperatureConfiguration = new TemperatureConfiguration();
			
			temperatureConfiguration.input   = new RandomAccessFile(resolvePath(root.get("input").asText()), "r");
			temperatureConfiguration.minimum = (byte) root.get("minimum").asInt();
			temperatureConfiguration.maximum = (byte) root.get("maximum").asInt();
			temperatureConfiguration.delta   = (byte) (temperatureConfiguration.maximum - temperatureConfiguration.minimum);
			temperatureConfiguration.values  = new FixedSizeByteList(root.get("average").asInt());
			
			return temperatureConfiguration;
		}
	}
	
	public byte readTemperature() throws IOException {
		final byte content = (byte) (Integer.parseInt(this.input.readLine()) / 1000);
		this.input.seek(0);
		return content;
	}
	
	public void update() throws IOException {
		this.values.addValue(this.readTemperature());
	}
	
	public float getPercentage() {
		return (float) (this.values.average() - this.minimum) / this.delta;
	}
}
