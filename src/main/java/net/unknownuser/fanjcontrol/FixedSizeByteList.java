package net.unknownuser.fanjcontrol;

import java.util.*;

import static java.lang.Math.*;

public class FixedSizeByteList {
	private final byte[] values;
	private int pointer = 0;
	
	public FixedSizeByteList(int size) {
		super();
		this.values = new byte[size];
	}
	
	public void addValue(byte value) {
		values[pointer] = value;
		
		if(++pointer == values.length) {
			pointer = 0;
		}
	}
	
	public byte average() {
		long average = 0;
		for (byte value : values) {
			average += value;
		}
		
		// Using simply `values.length` causes the first {length} average calculations to be way too low.
		return (byte) round((double) average / values.length);
	}
}
