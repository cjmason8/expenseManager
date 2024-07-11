package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.builder.ToStringExclude;

public class Test {

    public static void main(String[] args) {
        String date = "11 Jul 2024";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");

        LocalDate localDate = LocalDate.parse(date, formatter);

        System.out.println(localDate);
    }
}
