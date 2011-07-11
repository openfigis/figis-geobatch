/*
 */

package it.geosolutions.ie.stats;

import javax.jbi.messaging.NormalizedMessage;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class ParamParser {

    public static long getLongParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);
        if (value == null) {
            throw new ParamException("Missing " + propName + " param");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException numberFormatException) {
            throw new ParamException("Bad " + propName + " param");
        }
    }

    public static double getDoubleParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);
        if (value == null) {
            throw new ParamException("Missing " + propName + " param");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException numberFormatException) {
            throw new ParamException("Bad " + propName + " param");
        }
    }

    public static String getStringParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);
        if (value == null) {
            throw new ParamException("Missing " + propName + " param");

        }
        return value;
    }

    public static String getOptionalStringParam(NormalizedMessage in, String propName)
    throws ParamException {
			String value = (String) in.getProperty(propName);
			return value;
}
    
    public static <T extends Enum<T>> T getEnumParam(NormalizedMessage in, String propName, Class<T> enumClass, T[] values)
            throws ParamException {
        String propVal = (String) in.getProperty(propName);
        if (propVal == null) {
            throw new ParamException("Missing " + propName + " param");
        }

        try {
            return T.valueOf(enumClass, propVal);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Valid values are [");
            for (Enum<T> value : values) {
                sb.append(value.name()).append(" ");
            }
            sb.append("]");

            throw new ParamException("Bad " + propName + " param. " + sb);
        }
    }

    public static <T extends Enum<T>> T getOptionalEnumParam(NormalizedMessage in, String propName, Class<T> enumClass, T[] values)
            throws ParamException {
        String propVal = (String) in.getProperty(propName);
        if (propVal == null) {
            return null;
        }

        try {
            return T.valueOf(enumClass, propVal);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Valid values are [");
            for (Enum<T> value : values) {
                sb.append(value.name()).append(" ");
            }
            sb.append("]");

            throw new ParamException("Bad " + propName + " param. " + sb);
        }
    }

    public static Integer getOptionalIntParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);
        if (value == null) {
            return null;
        }

        try {
            return new Integer(value);
        } catch (NumberFormatException numberFormatException) {
            throw new ParamException("Bad " + propName + " param");
        }
    }

    public static Double getOptionalDoubleParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);
        if (value == null) {
            return null;
        }

        try {
            return new Double(value);
        } catch (NumberFormatException numberFormatException) {
            throw new ParamException("Bad " + propName + " param");
        }
    }

    public static Boolean getOptionalBoolParam(NormalizedMessage in, String propName)
            throws ParamException {
        String value = (String) in.getProperty(propName);

        if (value == null) {
            return null;
        } else if (value.equals("1") || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (value.equals("0") || value.equalsIgnoreCase("n") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        } else {
            throw new ParamException("Bad " + propName + " param");
        }
    }
}
