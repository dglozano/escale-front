package com.dglozano.escale.ble;

import com.dglozano.escale.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import timber.log.Timber;

public class CommunicationHelper {

    private static SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT);

    private CommunicationHelper() {
        // Utility class.
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexRepresentation) {
        if (hexRepresentation.length() % 2 == 1) {
            throw new IllegalArgumentException("hexToBytes requires an even-length String parameter");
        }

        int len = hexRepresentation.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexRepresentation.charAt(i), 16) << 4)
                    + Character.digit(hexRepresentation.charAt(i + 1), 16));
        }

        return data;
    }

    public static String getCurrentTimeHex() {
        String dateHex = Constants.DATE_SERVICE_FORMAT;
        Calendar calendar = Calendar.getInstance();
        String year = flipBytes(toHex(calendar.get(Calendar.YEAR)));
        String month = toHex(calendar.get(Calendar.MONTH));
        String day = toHex(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = toHex(calendar.get(Calendar.HOUR_OF_DAY));
        String min = toHex(calendar.get(Calendar.MINUTE));
        String sec = toHex(calendar.get(Calendar.SECOND));
        return String.format(dateHex, year, month, day, hour, min, sec);
    }

    public static String parseDateFromHex(String hexDate) {
        int year = Integer.parseInt(flipBytes(hexDate.substring(0, 4)), 16);
        int month = Integer.parseInt(hexDate.substring(4, 6), 16);
        int day = Integer.parseInt(hexDate.substring(6, 8), 16);
        int hour = Integer.parseInt(hexDate.substring(8, 10), 16);
        int min = Integer.parseInt(hexDate.substring(10, 12), 16);
        int sec = Integer.parseInt(hexDate.substring(12, 14), 16);
        Calendar cal = new GregorianCalendar(year, month, day, hour, min, sec);
        return sdf.format(cal.getTime());
    }

    public static boolean isSetToKilo(String fff1) {
        return fff1.equals(Constants.BYTES_SET_KG);
    }

    public static String flipBytes(String twoBytes) {
        Timber.d("Two Bytes: %1$s , lenght(): %2$d", twoBytes, twoBytes.length());
        if (twoBytes.length() != 4) {
            throw new IllegalArgumentException("flipBytes requires a 2 bytes long String parameter");
        }
        String firstByte = twoBytes.substring(0, 2);
        String secondByte = twoBytes.substring(2, 4);
        return secondByte + firstByte;
    }

    // Converts Dec to Hex and add 0 if there is an odd amount of digits
    public static String toHex(int dec) {

        String hex = Integer.toHexString(dec);

        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        Timber.d("DEC %1$d -> HEX %2$s", dec, hex);
        return hex;
    }
}
