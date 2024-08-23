package net.unknownuser.fanjcontrol;

import java.io.*;

import static java.lang.Math.*;

public class Common {
	public static final String PATH_AUTO_RESOLVE_SYMBOL = "::";
	public static final int    MANUAL_MODE              = 1;
	
	public static String resolvePath(String path) {
		if (!path.contains(PATH_AUTO_RESOLVE_SYMBOL)) {
			System.out.printf(
				"Path '%s' is missing auto resolve symbol ('%s'). This means the given path is most likely an absolute path and it probably will break on next reboot.%n",
				path,
				PATH_AUTO_RESOLVE_SYMBOL
			);
			return path;
		}
		
		final String[] split = path.split(PATH_AUTO_RESOLVE_SYMBOL);
		
		return String.format("%s%s%s", split[0], new File(split[0]).listFiles()[0].getName(), split[1]);
	}
	
	public static byte clampedLinearInterpolation(byte minimum, byte maximum, float percentage) {
		return (byte) Math.round(clamp(linearInterpolation(minimum, maximum, percentage), minimum, maximum));
	}
	
	public static float linearInterpolation(byte minimum, byte maximum, float percentage) {
		return minimum + ((maximum - minimum) * percentage);
	}
}
