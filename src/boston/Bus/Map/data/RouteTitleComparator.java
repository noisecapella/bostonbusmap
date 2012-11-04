package boston.Bus.Map.data;

import java.util.Comparator;

import boston.Bus.Map.util.LogUtil;

import com.google.common.base.CharMatcher;

/**
 * A case-insensitive comparator which sorts alphabetically, 
 * unless the string begins with a number,
 * in which case it does it numerically, then alphabetically
 * @author schneg
 *
 */
public class RouteTitleComparator implements Comparator<String> {

	@Override
	public int compare(String lhs, String rhs) {
		String lhsDigits = CharMatcher.DIGIT.trimLeadingFrom(lhs);
		String rhsDigits = CharMatcher.DIGIT.trimLeadingFrom(rhs);
		if (lhsDigits.length() != 0 && rhsDigits.length() != 0) {
			try
			{
				int lhsNum = Integer.parseInt(lhsDigits);
				int rhsNum = Integer.parseInt(rhsDigits);
				if (lhsNum < rhsNum) {
					return -1;
				}
				else if (lhsNum > rhsNum) {
					return 1;
				}
			}
			catch (NumberFormatException e) {
				LogUtil.e(e);
			}
		}
		return lhs.compareToIgnoreCase(rhs);
	}
	
}
