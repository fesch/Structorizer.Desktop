// Generated by Structorizer 3.32-26 

// Copyright (C) 2017-09-18 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

/**
 * Several declaration and initialisation variants for test of Analyser, Executor, and Generators
 */
public class DateTests563 {

	private static boolean initDone_CommonTypes423 = false;
	private class Date{
		public int	year;
		public short	month;
		public short	day;
		public Date(int p_year, short p_month, short p_day)
		{
			year = p_year;
			month = p_month;
			day = p_day;
		}
	};
	private static Date today;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initialize_CommonTypes423();
		
		// TODO: Check and accomplish variable declarations: 
		int[] values;
		int nDays;

		Date someDay = new Date(2017, 2, 24);
		nDays = daysInMonth423(someDay);
		today = new Date(2018, 7, 20);
		class Person{
			public String	name;
			public Date	birth;
			public int[]	test;
			public Person(String p_name, Date p_birth, int[] p_test)
			{
				name = p_name;
				birth = p_birth;
				test = p_test;
			}
		};
		Person me = new Person("roger", new Date(1985, 3, 6), new int[]{0, 8, 15});
		double[] declArray = new double[]{9.0, 7.5, -6.4, 1.7, 0.0};
		double[] explArray = new double[]{7.1, 0.5, -1.5};
		double[] doof = new double[]{0.4};
		double[] dull = new double[]{-12.7, 96.03};
		values = new int[]{47, 11};
	}

	/**
	 * Automatically created initialization procedure for CommonTypes423
	 */
	private static void initialize_CommonTypes423() {
		if (! initDone_CommonTypes423) {
			initDone_CommonTypes423 = true;
		}
	}

	/**
	 * Detects whether the given year is a leap year in the Gregorian calendar
	 * (extrapolated backwards beyonds its inauguration)
	 * @param year
	 * @return 
	 */
	private static boolean isLeapYear(??? year) {
		// TODO: Check and accomplish variable declarations: 
		boolean isLeapYear;

		// Most years aren't leap years... 
		isLeapYear = false;
		if ((year % 4 == 0) && (year % 100 != 0)) {
			// This is a standard leap year 
			isLeapYear = true;
		}
		else if (year % 400 == 0) {
			// One of the rare leap years 
			// occurring every 400 years 
			isLeapYear = true;
		}

		return isLeapYear;
	}

	/**
	 * Computes the number of days the given month (1..12)
	 * has in the the given year
	 * @param aDate
	 * @return 
	 */
	private static int daysInMonth423(Date aDate) {
		initialize_CommonTypes423();
		
		// TODO: Check and accomplish variable declarations: 
		boolean isLeap;
		int days;

		// select the case where illegal values are also considered 
		switch (aDate.month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			days = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			days = 30;
			break;
		case 2:
			// Default value for February 
			days = 28;
			// To make the call work it has to be done in 
			// a separate element (cannot be performed 
			// as part of the condition of an Alternative) 
			isLeap = isLeapYear(aDate.year);
			if (isLeap) {
				days = 29;
			}
			break;
		default:
			// This is the return value for illegal months. 
			// It is easy to check 
			days = 0;
		}
		return days;
	}

// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 


}
