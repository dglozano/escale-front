package com.dglozano.escale.ble;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import timber.log.Timber;

class CommunicationHelper {

    private static SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT);

    private CommunicationHelper() {
        // Utility class.
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    static byte[] hexToBytes(String hexRepresentation) {
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

    static String getCurrentTimeHex() {
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

    static String parseDateStringFromHex(String hexDate) {
        return sdf.format(parseDateFromHex(hexDate));
    }

    static Date parseDateFromHex(String hexDate) {
        if(hexDate.length() > 14) {
            hexDate = hexDate.substring(0, 14);
        } else {
            throw new IllegalArgumentException("parseDateFromHex requires a minimum 14 char long (7 bytes) String parameter");
        }
        int year = toDec(flipBytes(hexDate.substring(0, 4)));
        int month = toDec(hexDate.substring(4, 6));
        int day = toDec(hexDate.substring(6, 8));
        int hour = toDec(hexDate.substring(8, 10));
        int min = toDec(hexDate.substring(10, 12));
        int sec = toDec(hexDate.substring(12, 14));
        Calendar cal = new GregorianCalendar(year, month, day, hour, min, sec);
        return cal.getTime();
    }

    //TODO: See what happens if bits in flag for timeunit, bmi and user index are off
    static BodyMeasurement parseWeightMeasurementFromHex(String hexWeight) {
        //TODO Read flags
        float weight = toDec(flipBytes(hexWeight.substring(2, 6 ))) * 0.005f;
        Date date = parseDateFromHex(hexWeight.substring(6,20));
        //TODO Read user index;
        float bmi = toDec(flipBytes(hexWeight.substring(22,24))) * 0.1f;
        //TODO Read user height;
        BodyMeasurement bodyMeasurement = new BodyMeasurement();
        bodyMeasurement.setWeight(weight);
        bodyMeasurement.setDate(date);
        bodyMeasurement.setBmi(bmi);
        return bodyMeasurement;
    }

    static boolean isSetToKilo(String fff1) {
        return fff1.equals(Constants.BYTES_SET_KG);
    }

    static String flipBytes(String twoBytes) {
        if (twoBytes.length() != 4) {
            throw new IllegalArgumentException("flipBytes requires a 2 char long (1 byte) String parameter");
        }
        String firstByte = twoBytes.substring(0, 2);
        String secondByte = twoBytes.substring(2, 4);
        String flipped = secondByte + firstByte;
        Timber.d("Flipped bytes %1%s -> %2$s", twoBytes, flipped);
        return flipped;
    }

    static String generatePIN() {
        Random rand = new Random();
        int myRandomNumber = rand.nextInt(9999) + 1; //150
        String hex = toHex(myRandomNumber); //96
        hex = hex.length() == 2 ? "00" + hex : hex; //0096
        return hex;
    }

    // Converts Dec to Hex and add 0 if there is an odd amount of digits
    static String toHex(int dec) {

        String hex = Integer.toHexString(dec);

        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        Timber.d("DEC %1$d -> HEX %2$s", dec, hex);
        return hex;
    }

    static int toDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    static String lastNBytes(String hexString, int n) {
        int from = hexString.length() - n < 0 ? 0 : hexString.length() - n;
        return hexString.substring(from);
    }

    static class PinIndex {
        private String pin;
        private String index;

        PinIndex(String index, String pin) {
            this.index = index;
            this.pin = pin;
        }

        public String pin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public String index() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }
    }

    static class ScaleUserLimitExcedded extends Exception {

    }
}
