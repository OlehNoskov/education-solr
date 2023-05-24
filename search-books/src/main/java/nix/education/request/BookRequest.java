package nix.education.request;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import nix.education.util.DateFormat;

@Data
public class BookRequest {

    private String author;
    private String title;
    private String description;
    private List<String> tags;
    private String publication_date;

    @SneakyThrows
    public Date getPublication_date() {
        return DateFormat.DATE_FORMAT_BOOK_REQUEST.parse(publication_date);
    }
}
