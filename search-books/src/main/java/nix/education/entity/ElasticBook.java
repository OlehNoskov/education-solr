package nix.education.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;

@Data
public class ElasticBook {

    private Long id;

    private String author;

    private String title;

    private String description;

    private List<String> tags;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publicationDate;
}
