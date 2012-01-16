package skylight1.opengl.files;

/**
 * Parses strings to numbers quickly.
 * Arguments not checked or trimmed.
 * Limited formats accepted.
 * 
 */
public class QuickParseUtil {

        /**
         * Parse string containing a float.
         * Assumes input trimmed, not null, not infinity, not NaN, not using exponent representation.
         * Handles only: [-+]?\d+(.\d+)?
         * <p>
         * Current implementation took 12ms to parse 1000 test strings,
         * Float.parseFloat took 465ms to parse the same strings.
         * 
         * @param aInput
         *            String representation of a float number
         * @return parsed float
         */
        public static float parseFloat(final String aInput) {
                // XXX Might be cleaner to use Float.intBitsToFloat with a researched algorithm to find inputs:
                // http://portal.acm.org/citation.cfm?doid=93542.93557

                // Read sign.
                int index = 0;
                boolean isNegative = false;
                switch (aInput.charAt(0)) {
                        case '-':
                                isNegative = true;
                                // Fall through.
                        case '+':
                                index++;
                                // No default action.
                }

                // Read integer before the decimal.
                final int length = aInput.length();
                int integer = 0;
                for (; index < length; index++) {
                        char character = aInput.charAt(index);
                        if (character == '.') {
                                index++;
                                break;
                        }

                        // Pre-calculated power values in an array doesn't seem to help speed
                        // when using integer math. It did previously when using float math.
                        // Maybe Java's array bounds checking slows it down.
                        integer *= 10;
                        integer += character - '0';
                }

                // Read fraction after the decimal.
                int numerator = 0;
                int denominator = 1;
                for (; index < length; index++) {
                        char character = aInput.charAt(index);

                        denominator *= 10;
                        numerator *= 10;
                        numerator += character - '0';
                }

                // Calculate and return result.
                float result = integer + numerator / (float) denominator;
                return isNegative ? -result : result;
        }
        

        private final static int[][] INTEGER_DECIMAL_VALUES = new int[4][10];

        static {
                for (int decimalPlace = 0; decimalPlace < INTEGER_DECIMAL_VALUES.length; decimalPlace++) {
                        for (int decimalValue = 0; decimalValue < 10; decimalValue++) {
                                INTEGER_DECIMAL_VALUES[decimalPlace][decimalValue] = (int) (Math.pow(10d, decimalPlace) * decimalValue);
                        }
                }
        }

        public static int parseInteger(final String aStringRepresentationOfAnInteger) {
                final int startOfDigits;
                final int sign;
                if (aStringRepresentationOfAnInteger.charAt(0) == '-') {
                        startOfDigits = 1;
                        sign = -1;
                } else {
                        startOfDigits = 0;
                        sign = 1;
                }
                int result = 0;
                int decimalPlace = -1;
                final int stringLength = aStringRepresentationOfAnInteger.length();
                for (int i = stringLength - 1; i >= startOfDigits; i--) {
                        decimalPlace++;
                        result += INTEGER_DECIMAL_VALUES[decimalPlace][aStringRepresentationOfAnInteger.charAt(i) - '0'];
                }
                return sign * result;
        }
        
}
