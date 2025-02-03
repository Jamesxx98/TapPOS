package com.example.tappos.GlobalFunctions;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class Custom {

    public static String formatAmount(String amount) {
        if (amount == null) {
            return "0";
        }
        if (amount.contains(".")) {
            String str = amount.replace(".", "");
            return str.length() < 1 ? "000000000000" : String.format(Locale.getDefault(), "%012d", Integer.parseInt(str));
        }
        return amount.length() < 1 ? "000000000000" : String.format(Locale.getDefault(), "%012d", Integer.parseInt(amount));
    }

    public static String[] getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd-hhmmss");

        String datetime = simpleDateFormat.format(c.getTime());

        String date = datetime.substring(0, datetime.indexOf("-"));
        String time = datetime.substring(datetime.length() - 6);

        return new String[]{date, time};
    }

    public static String convertToMMYY(String date) {
        LocalDate localDate = LocalDate.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyy");
        return localDate.format(formatter);
    }
}
