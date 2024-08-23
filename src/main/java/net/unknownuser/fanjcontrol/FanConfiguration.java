package net.unknownuser.fanjcontrol;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.deser.std.*;

import java.io.*;

import static net.unknownuser.fanjcontrol.Common.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = FanConfiguration.FanConfigurationDeserializer.class)
public class FanConfiguration {
	protected RandomAccessFile         pwmFile;
	protected RandomAccessFile         fanInput;
	protected TemperatureConfiguration temperature;
	protected PWMConfiguration         pwm;
	
	@JsonIgnore
	protected RandomAccessFile modeFile;
	@JsonIgnore
	private   boolean          isRunning = true;
	@JsonIgnore
	protected byte             originalMode;
	
	static class FanConfigurationDeserializer extends StdDeserializer<FanConfiguration> {
		public FanConfigurationDeserializer(final Class<?> vc) {
			super(vc);
		}
		
		public FanConfigurationDeserializer() {
			this(null);
		}
		
		@Override
		public FanConfiguration deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode    root  = codec.readTree(parser);
			
			FanConfiguration fanConfiguration = new FanConfiguration();
			
			final String pwmInputPath = resolvePath(root.get("pwm_input").asText());
			
			fanConfiguration.pwmFile = new RandomAccessFile(pwmInputPath, "rw");
			if (root.has("fan_input")) {
				fanConfiguration.fanInput = new RandomAccessFile(resolvePath(root.get("fan_input").asText()), "r");
			} else {
				fanConfiguration.fanInput = null;
			}
			fanConfiguration.modeFile     = new RandomAccessFile(pwmInputPath + "_enable", "rwd");
			fanConfiguration.originalMode = fanConfiguration.readMode();
			fanConfiguration.setMode((byte) MANUAL_MODE);
			
			fanConfiguration.temperature = context.readTreeAsValue(root.get("temperature"),
				TemperatureConfiguration.class
			);
			fanConfiguration.pwm         = context.readTreeAsValue(root.get("pwm"), PWMConfiguration.class);
			
			return fanConfiguration;
		}
	}
	
	public Byte readMode() throws IOException {
		final byte content = Byte.parseByte(this.modeFile.readLine());
		this.modeFile.seek(0);
		return content;
	}
	
	public void setMode(byte state) throws IOException {
		this.modeFile.write(String.valueOf(state).charAt(0));
		this.modeFile.seek(0);
	}
	
	public void setPWM(byte value) throws IOException {
		this.pwmFile.write(String.valueOf(value).getBytes());
		this.pwmFile.seek(0);
	}
	
	public void shutdown() throws IOException {
		if (!this.isRunning) {
			return;
		}
		
		this.isRunning = false;
		this.setMode(this.originalMode);
	}
	
	public void update() throws IOException {
		this.temperature.update();
		
		final byte currentPWM = clampedLinearInterpolation(
			this.pwm.minimum,
			this.pwm.maximum,
			this.temperature.getPercentage()
		);
		this.setPWM(currentPWM);
	}
}
