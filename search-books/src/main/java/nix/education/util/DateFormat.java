package nix.education.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.SneakyThrows;

public class DateFormat {
    public static final SimpleDateFormat DATE_FORMAT_BOOK_REQUEST
        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final SimpleDateFormat DATE_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat ELASTICSEARCH_DATE_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @SneakyThrows
    public static Date getStartSearchBookDate() {
        return DATE_FORMAT.parse("1899-12-31 22:00:00");
    }
}
