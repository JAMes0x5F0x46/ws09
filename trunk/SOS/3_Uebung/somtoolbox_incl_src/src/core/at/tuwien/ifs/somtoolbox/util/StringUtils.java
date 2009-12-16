package at.tuwien.ifs.somtoolbox.util;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class provides utility methods for String manipulation.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringUtils.java 2874 2009-12-11 16:03:27Z frank $
 */
public class StringUtils {
    private static final String DEFAULT_ELIPSIS = "...";

    // initialize number formats, using locale avoids format troubles on localized OS's (, instead of .)
    public static DecimalFormat formatNoFractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format1FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format1GuaranteedFractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format2FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format3FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format3FractionDigitsWithZeros = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format5FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format5FractionDigitsWithZeros = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format6Digits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format10FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format10FractionDigitsWithZeros = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    public static DecimalFormat format15FractionDigits = (DecimalFormat) NumberFormat.getNumberInstance(java.util.Locale.US);

    static {
        formatNoFractionDigits.applyPattern("#");
        format1FractionDigits.applyPattern("#.#");
        format1GuaranteedFractionDigits.applyPattern("0.0");
        format2FractionDigits.applyPattern("#.##");
        format3FractionDigits.applyPattern("#.###");
        format3FractionDigitsWithZeros.applyPattern("0.000");
        format5FractionDigits.applyPattern("#.#####"); // this precision should be enough for e.g. dwm distances
        format5FractionDigitsWithZeros.applyPattern("0.00000");
        format6Digits.applyPattern("000000");
        format10FractionDigits.applyPattern("#.##########");
        format10FractionDigitsWithZeros.applyPattern("0.0000000000");
        format15FractionDigits.applyPattern("#.###############");
    }

    /**
     * Returns the string until (excluding) the first dot (.)
     * 
     * @return filename without suffices
     */
    public static String stripSuffix(String sMitSuffix) {
        if (sMitSuffix.contains(File.separator)) {
            sMitSuffix = sMitSuffix.substring(sMitSuffix.lastIndexOf(File.separator) + 1);
        }
        int pos = sMitSuffix.indexOf(".");
        return sMitSuffix.substring(0, pos);
    }

    /**
     * Makes sure that the given String ends with the OS-correct File.separator ('/' on Unix, '\\' on Windows)
     */
    public static String makeStringEndWithCorrectFileSeparator(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            // Cut last char and OS-append correct separator
            return path.substring(0, path.length() - 1) + File.separator;
        } else {
            return path + File.separator;
        }
    }

    /**
     * Formats byte size in nice format
     * 
     * @param byteSize the size in bytes to format
     * @return Formatted number of bytes (eg: empty, 15 B, 12kB, 821 kB, 3 MB...)
     */
    public static String readableBytes(long byteSize) {
        if (byteSize < 0l) {
            return "invalid";
        }
        if (byteSize < 1l) {
            return "empty";
        }

        float byteSizeF = (new java.lang.Float(byteSize)).floatValue();
        String unit = "bytes";
        float factor = 1f;
        String[] desc = { "B", "kB", "MB", "GB", "TB" };

        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat();
        decimalFormat.setMaximumFractionDigits(1);
        decimalFormat.setGroupingUsed(true);

        String value = decimalFormat.format(byteSizeF);

        int i = 0;
        while (i + 1 < desc.length && (value.length() > 4 || value.length() > 3 && value.indexOf('.') < 0)) {
            i++;
            factor = factor * 1024l;
            value = decimalFormat.format(byteSizeF / factor);
        }
        if (value.charAt(0) == '0' && i > 0) { // go one back if a too-big scale is used
            value = decimalFormat.format(java.lang.Math.round(1024 * byteSizeF / factor));
            i--;
        }

        if (value.length() > 3 && value.indexOf('.') > 0) {
            value = value.substring(0, value.indexOf('.'));
        }

        unit = desc[i];
        return value + " " + unit;
    }

    public static String escapeString(String s) {
        s = s.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("-", "_").replaceAll("\\(", "_").replaceAll("\\)", "_");
        while (s.indexOf("__") != -1) {
            s = s.replaceAll("__", "_");
        }
        return s;
    }

    public static String toString(Collection<?> collection) {
        return Arrays.toString(collection.toArray(new Object[collection.size()]));
    }

    public static String toString(Collection<?> collection, String start, String end) {
        return new ToStringBuilder(collection, new MyToStringStyle(start, end)).append(collection).toString();
    }

    public static String toString(Object[] array, String start, String end) {
        return new ToStringBuilder(array, new MyToStringStyle(start, end)).append(array).toString();
    }

    public static String toString(int[] array, String start, String end) {
        return new ToStringBuilder(array, new MyToStringStyle(start, end)).append(array).toString();
    }

    public static String beautifyForHTML(String source) {
        return source.replaceAll("_", " ").replaceAll("&", "&amp;").replaceAll("-", " - ").replaceAll("/", " / ").replaceAll("  ", " ");
    }

    public static boolean equals(Object o, String s) {
        return o instanceof String && org.apache.commons.lang.StringUtils.equals((String) o, s);
    }

    /** Checks whether the given String equals any of the given options. */
    public static boolean equalsAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsAny(Object o, String... options) {
        return o instanceof String && equalsAny((String) o, options);
    }

    /** Checks whether the given String starts with any of the given options. */
    public static boolean startsWithAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.startsWith(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean endsWithAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.endsWith(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAny(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.contains(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesAny(String[] regExps, String s) {
        for (int i = 0; i < regExps.length; i++) {
            if (s.matches(regExps[i])) {
                // System.out.println("Match: " + s + " on reg-exp" + regExps[i]);
                return true;
            }
        }
        return false;
    }

    /** Checks whether the given String starts with any of the given options, ignoring the case */
    public static boolean startsWithAnyIgnoreCase(String s, String... options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.toLowerCase().startsWith(element.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /** Prints a double[] with the given number of decimal digits. */
    public static String toStringWithPrecision(double[] a, int digits) {
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) {
            b.append(String.format("%1." + digits + "f ", a[i]));
        }
        b.setCharAt(b.length() - 1, ']');
        return b.toString();

        // DecimalFormat decimalFormat = getDecimalFormat(digits);
        // b.append('[');
        // for (int i = 0;; i++) {
        // String formatted = decimalFormat.format(a[i]);
        // b.append(formatted + getSpaces(digits + 3 - formatted.length()));
        // if (i == iMax) {
        // return b.append(']').toString();
        // }
        // }
    }

    /**
     * Returns a string representation of the contents of the specified array in the same fashion as {@link Arrays#toString(double[])}, but limiting
     * the output to the given maxIndices parameter.
     */
    public static String toString(double[] a, int maxIndices) {
        if (a == null)
            return "null";
        int iMax = Math.min(a.length - 1, maxIndices);
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0;; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    private static DecimalFormat getDecimalFormat(int digits) {
        String precision = "";
        for (int i = 0; i < digits; i++) {
            precision += "#";
        }
        final DecimalFormat decimalFormat = new DecimalFormat("#." + precision);
        return decimalFormat;
    }

    public static String getSpaces(int num) {
        return repeatString(num, " ");
    }

    public static String repeatString(int num, String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static int getLongestStringLength(Iterable<String> c) {
        int max = 0;
        for (String s : c) {
            if (s.length() > max) {
                max = s.length();
            }
        }
        return max;
    }

    public static String formatMaxLengthEllipsis(String s, int maxLen) {
        return formatMaxLengthEllipsis(s, maxLen, DEFAULT_ELIPSIS);
    }

    public static String formatMaxLengthEllipsis(String s, int maxLen, String ellipsis) {
        if (maxLen != -1 && s.length() > maxLen && maxLen > ellipsis.length()) {// content longer than allowed => //shorten the content
            int cutAt = maxLen - ellipsis.length();
            return s.substring(0, cutAt) + ellipsis;
        }
        return s;
    }

    public static String formatEndMaxLengthEllipsis(String s, int maxLen) {
        return formatEndMaxLengthEllipsis(s, maxLen, DEFAULT_ELIPSIS);
    }

    public static String formatEndMaxLengthEllipsis(String s, int maxLen, String ellipsis) {
        if (maxLen != -1 && s.length() > maxLen && maxLen > ellipsis.length()) {// content longer than allowed => //shorten the content
            int cutAt = maxLen - ellipsis.length();
            return ellipsis + s.substring(s.length() - cutAt);
        }
        return s;
    }

    public static String toString(Point2D[] a) {
        int digits = 4;
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        DecimalFormat decimalFormat = getDecimalFormat(digits);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            String formatted = decimalFormat.format(a[i].getX());
            b.append(formatted).append(getSpaces((digits + 3) * 2 - formatted.length()));
        }
        for (int i = 0; i < a.length; i++) {
            String formatted = decimalFormat.format(a[i].getX()) + "/" + decimalFormat.format(a[i].getY());
            b.append(formatted).append(getSpaces((digits + 3) * 2 - formatted.length())).append(" ");
        }
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    public static String printMap(Map m) {
        StringBuffer sb = new StringBuffer("{");
        for (Object key : m.keySet()) {
            Object value = m.get(key);
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(key).append("=");
            sb.append(ArrayUtils.toString(value));
        }
        return sb.append("}").toString();
    }

    public static class MyToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        public MyToStringStyle(String arrayStart, String arrayEnd) {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
            this.setUseFieldNames(false);
            this.setContentStart("");
            this.setContentEnd("");
            this.setArrayStart(arrayStart);
            this.setArrayEnd(arrayEnd);
        }
    }

    public static final String REGEX_SPACE_OR_TAB = "[ \t]+";

    /** Returns the common starting portion of the two Strings, or an empty String if there is no common part. */
    public static String getCommonPrefix(String s1, String s2) {
        int i = 0;
        if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
            return "";
        }
        while (i < s1.length() && i < s2.length() && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return s1.substring(0, i);
    }

    public static String getCommonPrefix(Collection<String> c) {
        if (c.size() == 0) {
            return "";
        }

        Iterator<String> iterator = c.iterator();
        String commonPrefix = iterator.next();
        while (iterator.hasNext()) {
            commonPrefix = getCommonPrefix(commonPrefix, iterator.next());
        }
        return commonPrefix;
    }

    public static String getCommonPrefix(String[] a) {
        if (a.length == 0) {
            return "";
        }

        String commonPrefix = a[0];
        for (int i = 1; i < a.length; i++) {
            commonPrefix = getCommonPrefix(commonPrefix, a[i]);
        }
        return commonPrefix;
    }

    /** Returns the common starting portion of the two Strings, or an empty String if there is no common part. */
    public static String getCommonSuffix(String s1, String s2) {
        int i = 0;
        if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
            return "";
        }
        while (i < s1.length() && i < s2.length() && s1.charAt(s1.length() - 1 - i) == s2.charAt(s2.length() - 1 - i)) {
            i++;
        }
        return s1.substring(s1.length() - i);
    }

    public static String getCommonSuffix(Collection<String> c) {
        if (c.size() == 0) {
            return "";
        }

        Iterator<String> iterator = c.iterator();
        String commonSuffix = iterator.next();
        while (iterator.hasNext()) {
            commonSuffix = getCommonSuffix(commonSuffix, iterator.next());
        }
        return commonSuffix;
    }

    public static String getCommonSuffix(String[] a) {
        if (a.length == 0) {
            return "";
        }

        String commonSuffix = a[0];
        for (int i = 1; i < a.length; i++) {
            commonSuffix = getCommonSuffix(commonSuffix, a[i]);
        }
        return commonSuffix;
    }

    public static String[] getDifferences(String[] a) {
        String[] res = new String[a.length];
        String commonPrefix = getCommonPrefix(a);
        String commonSuffix = getCommonSuffix(a);
        for (int i = 0; i < res.length; i++) {
            res[i] = a[i].substring(commonPrefix.length(), a[i].length() - commonSuffix.length());
        }
        return res;
    }

    /**
     * Calculate the Levenshtein Distance between the two given Strings.
     * 
     * @param s
     * @param t
     * @return the Levenshtein Distance between the two strings
     * @see {@link http://www.javalobby.org/java/forums/t15908.html}
     */
    public static int levenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();

        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }

        int[][] d = new int[n + 1][m + 1];

        for (int i = 0; i <= n; d[i][0] = i++)
            ;
        for (int j = 1; j <= m; d[0][j] = j++)
            ;

        for (int i = 1; i <= n; i++) {
            char sc = s.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int v = d[i - 1][j - 1];
                if (t.charAt(j - 1) != sc)
                    v++;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v);
            }
        }
        return d[n][m];
    }

    public static String[] concat(String[] array1, String[] array2) {
        if (array2 == null) {
            return array1;
        } else if (array1 == null) {
            return array2;
        }
        int lengthFirst = array1.length;
        int lengthSecond = array2.length;
        String[] ret = new String[lengthFirst + lengthSecond];
        System.arraycopy(array1, 0, ret, 0, lengthFirst);
        System.arraycopy(array2, 0, ret, lengthFirst, lengthSecond);
        return ret;
    }

    public static String[] trim(String[] split) {
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public static void main(String[] args) {
        String[] strings = { "partition_0_1370_converted", "partition_1_1696_converted", "partition_2_1729_converted", "partition_3_1937_converted",
                "partition_4_1669_converted", "partition_5_1146_converted" };
        System.out.println(Arrays.toString(strings));
        System.out.println(getCommonPrefix(strings));
        System.out.println(getCommonSuffix(strings));
        System.out.println(Arrays.toString(getDifferences(strings)));
    }

    public static final String[][] STRING_URLENCODE_REPLACEMENTS = { { "/", "_" }, { "?", "_" }, { "&", "_" }, { "=", "_" }, { "%3A", ":" },
            { "%2C", "," }, { "%25", "_" }, { "%C3%A8", "e" }, { "%C3%A9", "e" }, { "%C3%89", "E" }, { "%C3%A1", "a" }, { "%C3%B6", "ö" },
            { "%C3%96", "Ö" }, { "%C3%AB", "e" }, { "%C3%B3", "o" }, { "%C2%A1", "j" }, { "%C3%BC", "ü" }, { "%C3%9C", "Ü" }, { "%C2%BD", ",5" },
            { "%C3%A0", "a" }, { "%C3%A4", "a" }, { "%C3%AF", "i" }, { "%60", "'" }, { "%C3%BA", "u" }, { "%C3%B1", "n" }, { "%C3%81", "A" },
            { "%C3%A6", "ae" }, { "%C2%B0", "" }, { "%C3%AA", "e" }, { "%C3%B8", "o" }, { "%C3%A9", "o" }, { "%C3%AA", "e" }, { "%C3%A7", "c" },
            { "%C3%B4", "o" }, { "%C3%AD", "i" }, { "%C3%80", "A" }, { "%C3%A3", "a" }, { "%C3%BF", "y" }, { "%C2%93", "" }, { "%C2%94", "" },
            { "%C2%B2", "2" }, { "%C3%83%C2%AD", "i" }, { "%C3%9F", "ss" }, { "%C3%B2", "o" }, { "%C3%A2", "a" }, { "%C3%87", "C" },
            { "%C3%AE", "i" }, { "%C3%88", "e" }, { "%C2%B7", "-" }, { "%C3%83%C2%BC", "ü" }, { "%C3%BB", "u" }, { "%C3%93", "O" },
            { "%C3%A5", "a" }, { "%C3%B9", "u" }, { "%C3%BD", "y" }, { "%C3%83%C2%A3", "A" }, { "%C3%A3", "a" }, { "%C3%83%C2%A9", "e" },
            { "%C3%86", "Ae" }, { "%C3%84", "Ä" }, { "%C3%AC", "i" }, { "%C2%92", "'" }, { "%C3%B0", "o" }, { "%C3%82%C2%92", "'" },
            { "%C3%82", "A" }, { "%C3%98", "O" }, { "%C3%9A", "U" }, { "%C2%B5", "mu" }, { "%C2%AA", "a" }, { "%C3%8D", "I" },
            { "%C3%83%C2%B1", "n" }, { "%C3%B5", "o" }, { "%C3%83%C2%B3", "o" }, { "%C3%83", "A" }, { "%C3%83%C2%B8", "a" }, { "A%C2%B8", "a" },
            { "%C2%A4", "a" }, { "%C2%AE", "(R)" }, { "%C3%85", "A" } };

    public static String replaceURLEncode(String s) {
        for (String[] strings : STRING_URLENCODE_REPLACEMENTS) {
            if (s.contains(strings[0])) {
                s = s.replace(strings[0], strings[1]);
            }
        }
        return s;
    }

    /**
     * formats a boolean value according to the given type returns the needed string representation of a boolean value that can be printed to the
     * report.<br/>
     * At the moment, only one transtormation is supported:
     * <ul>
     * <li>type: 0: true -> yes, false->no (default)</li>
     * </ul>
     * 
     * @param type the type of conversion/strings wished (see above)
     * @param value the boolean value
     * @return a string that encodes the boolean value in a way that it can be printed
     */
    public static String formatBooleanValue(int type, boolean value) {
        switch (type) {
            default:
                return value ? "yes" : "no";
        }
    }

    /**
     * creates an representation of a color that can be used in html or css takes an int - array of size 3 and transforms it to a hex String of
     * structure #rrggbb The array must have at least length 3 and must not contain null values.
     * 
     * @param rgb an array specifying the red, green and blue parts of the color
     * @return a hex string with structure #rrggbb according to the values in the given array
     */
    public static String getRGBString(int[] rgb) {
        return "#" + Integer.toHexString(rgb[0]) + Integer.toHexString(rgb[1]) + Integer.toHexString(rgb[2]);
    }

    /**
     * creates an representation of a color that can be used in html or css takes an int - array of size 3 and transforms it to a hex String of
     * structure #rrggbb The array must have at least length 3 and must not contain null values.
     * 
     * @param rgb an array specifying the red, green and blue parts of the color
     * @return a hex string with structure #rrggbb according to the values in the given array
     */
    public static String getLatexRGBString(int[] rgb) {
        double r = (double) (Math.round((float) ((double) rgb[0] / (double) 255 * 100))) / 100d;
        double g = (double) (Math.round((float) ((double) rgb[1] / (double) 255 * 100))) / 100d;
        double b = (double) (Math.round((float) ((double) rgb[2] / (double) 255 * 100))) / 100d;
        return (r + "," + g + "," + b);
    }

    /**
     * returns the correct format of the given String needed for displaying it in the comparison table this means, that if the String is null or
     * empty, "-" is returned instead of the String
     * 
     * @param value the string that shall be formatted
     * @return the input string or "-" if the input is inadequate
     */
    public static String formatString(String value) {
        if (value == null || value.length() <= 0) {
            return "-";
        } else {
            return value;
        }
    }

    /**
     * returns the correct format of a double needed for displaying it in the comparison table this means, that if the value is smaller than 0, the
     * string "-" is returned instead of the value
     * 
     * @param value a double value for which a String representation is needed
     * @return a string containing the value of the double up to 2 values behind the ., or "-" if the value is smaller 0
     */
    public static String formatDouble(double value) {
        if (value < 0) {
            return "-";
        } else {
            return String.format("%.2f", value);
        }
    }

    /**
     * This extra method had to be written because URLEncoder.encode 1) performs an encoding of a slash (/), 2) encodes a space as a +, but should
     * encode it as %20. Note that URLEncoder.encode is originally meant for encoding HTML form data only, not arbitrary URLs. However, no other
     * methods of encoding special characters in URLs has been found (the URI methods did not work).
     * 
     * @param url a non-encoded URL
     * @return encoded URL
     */
    public static String URLencode(String url) {
        String parts[] = url.split("/");
        StringBuffer encodedURL = new StringBuffer();

        // leave part 0, which is the protocol (http:) unchanged
        encodedURL.append(parts[0]);

        // and start with part 1
        for (int i = 1; i < parts.length; i++) {
            try {
                // if (i > 0)
                encodedURL.append("/");
                encodedURL.append(URLEncoder.encode(parts[i], "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
                System.err.println(uee.getMessage());
                return null;
            }
        }

        // Spaces are replaced by + but should be replaced by %20
        // + that existed before have already been replace by %2B in the previous step

        return encodedURL.toString().replace("+", "%20");
    }

}
