package com.codeshaper.jello.editor.swing;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;

import org.joml.Math;

public class JNumberField extends JFormattedTextField {

	private static final String regexWholeNumber = "^-?(\\d+)?$";
	private static final String regexDecimal = "^-?[0-9]?\\d*(\\.(\\d+)?)?$";

	private EnumNumberType numberType;
	private boolean hasMinValue;
	private double minValue;
	private boolean hasMaxValue;
	private double maxValue;

	public JNumberField(EnumNumberType type) {
		this.numberType = type;

		boolean isDecimal = type == EnumNumberType.FLOAT || type == EnumNumberType.DOUBLE;

		NumberFormat format;
		if (isDecimal) {
			format = NumberFormat.getNumberInstance();
			format.setMinimumFractionDigits(1);
			format.setMaximumFractionDigits(5); // TODO make this configurable.  Using this Integer.MAX_VALUE = no rounding = bad idea
		} else {
			format = NumberFormat.getIntegerInstance();
		}
		format.setGroupingUsed(false);

		CustomFormatter formatter = new CustomFormatter(format, isDecimal ? regexDecimal : regexWholeNumber);
		if (type == EnumNumberType.BYTE) {
			formatter.setValueClass(Byte.class);
			formatter.setMinimum(Byte.MIN_VALUE);
			formatter.setMaximum(Byte.MAX_VALUE);
		} else if (type == EnumNumberType.INT) {
			formatter.setValueClass(Integer.class);
			formatter.setMinimum(Integer.MIN_VALUE);
			formatter.setMaximum(Integer.MAX_VALUE);
		} else if (type == EnumNumberType.SHORT) {
			formatter.setValueClass(Short.class);
			formatter.setMinimum(Short.MIN_VALUE);
			formatter.setMaximum(Short.MAX_VALUE);
		} else if (type == EnumNumberType.LONG) {
			formatter.setValueClass(Long.class);
			formatter.setMinimum(Long.MIN_VALUE);
			formatter.setMaximum(Long.MAX_VALUE);
		} else if (type == EnumNumberType.FLOAT) {
			formatter.setValueClass(Float.class);
			formatter.setMinimum(-Float.MAX_VALUE);
			formatter.setMaximum(Float.MAX_VALUE);
		} else if (type == EnumNumberType.DOUBLE) {
			formatter.setValueClass(Double.class);
			formatter.setMinimum(-Double.MAX_VALUE);
			formatter.setMaximum(Double.MAX_VALUE);
		}

		// formatter.setAllowsInvalid(true);
		// formatter.setCommitsOnValidEdit(false);

		this.setFormatterFactory(new DefaultFormatterFactory(formatter));
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(40, super.getMinimumSize().height);
	}
	
	public void setMin(double min) {
		this.hasMinValue = true;
		this.minValue = min;
	}

	public void setMax(double max) {
		this.hasMaxValue = true;
		this.maxValue = max;
	}

	public EnumNumberType getNumberType() {
		return this.numberType;
	}
	
	private class CustomFormatter extends NumberFormatter {

		private final String regex;

		public CustomFormatter(NumberFormat format, String regex) {
			super(format);

			this.regex = regex;
		}

		@Override
		protected DocumentFilter getDocumentFilter() {
			return new PatternFilter(this.regex);
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			Object obj = super.stringToValue(text);
			if (obj instanceof Byte b) {
				return (byte) Math.clamp(hasMinValue ? (byte) minValue : Byte.MIN_VALUE,
						hasMaxValue ? (byte) maxValue : Byte.MAX_VALUE, b);
			} else if (obj instanceof Integer i) {
				return (int) Math.clamp(hasMinValue ? (int) minValue : Integer.MIN_VALUE,
						hasMaxValue ? (int) maxValue : Integer.MAX_VALUE, i);
			} else if (obj instanceof Short s) {
				return (short) Math.clamp(hasMinValue ? (short) minValue : Short.MIN_VALUE,
						hasMaxValue ? (short) maxValue : Short.MAX_VALUE, s);
			} else if (obj instanceof Long l) {
				return (long) Math.clamp(hasMinValue ? (long) minValue : Long.MIN_VALUE,
						hasMaxValue ? (long) maxValue : Long.MAX_VALUE, l);
			} else if (obj instanceof Float f) {				
				return (float) Math.clamp(hasMinValue ? (float) minValue : -Float.MAX_VALUE,
						hasMaxValue ? (float) maxValue : Float.MAX_VALUE, f);
			} else if (obj instanceof Double d) {
				return (double) Math.clamp(hasMinValue ? minValue : -Double.MAX_VALUE,
						hasMaxValue ? maxValue : Double.MAX_VALUE, d);
			}

			return obj;
		}
	}
	/*
	 * switch (type) { case BYTE: formatter.setMinimum(Byte.MIN_VALUE);
	 * formatter.setMaximum(10);//Byte.MAX_VALUE); break; case SHORT:
	 * formatter.setMinimum(Short.MIN_VALUE); formatter.setMaximum(Short.MAX_VALUE);
	 * break; case INT: formatter.setMinimum(Integer.MIN_VALUE);
	 * formatter.setMaximum(Integer.MAX_VALUE); break; case LONG:
	 * formatter.setMinimum(Long.MIN_VALUE); formatter.setMaximum(Long.MAX_VALUE);
	 * break; case FLOAT: formatter.setMinimum(Float.MIN_VALUE);
	 * formatter.setMaximum(Float.MAX_VALUE); break; case DOUBLE:
	 * formatter.setMinimum(Double.MIN_VALUE);
	 * formatter.setMaximum(Double.MAX_VALUE); break; }
	 */

	private class PatternFilter extends DocumentFilter {

		private Pattern pattern;

		public PatternFilter(String pat) {
			this.pattern = Pattern.compile(pat);
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			String newStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
			Matcher m = this.pattern.matcher(newStr);
			if (m.matches()) {
				super.insertString(fb, offset, string, attr);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
				throws BadLocationException {
			if (length > 0) {
				fb.remove(offset, length);
			}
			this.insertString(fb, offset, string, attr);
		}
	}

	public enum EnumNumberType {

		BYTE, INT, SHORT, LONG, FLOAT, DOUBLE;

		public static EnumNumberType func(Class<?> type) {
			if (type == byte.class || type == Byte.class) {
				return EnumNumberType.BYTE;
			} else if (type == short.class || type == Short.class) {
				return EnumNumberType.SHORT;
			} else if (type == int.class || type == Integer.class) {
				return EnumNumberType.INT;
			} else if (type == long.class || type == Long.class) {
				return EnumNumberType.LONG;
			} else if (type == float.class || type == Float.class) {
				return EnumNumberType.FLOAT;
			} else if (type == double.class || type == Double.class) {
				return EnumNumberType.DOUBLE;
			}

			throw new IllegalArgumentException(
					"field must have a backing type of a primitive number or its wrapper class.");
		}
	}
}
