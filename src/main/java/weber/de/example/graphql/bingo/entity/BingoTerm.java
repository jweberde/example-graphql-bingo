package weber.de.example.graphql.bingo.entity;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(
        uniqueConstraints = @UniqueConstraint(name = "bingo_term_unique", columnNames = {
                "value", "category"
        })
)
@javax.persistence.Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY) //Provide cache strategy.
public class BingoTerm {

    public static final String GENERAL_CATEGORY = "general";
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    public UUID id;

    @NotBlank
    @Column(length = 255, updatable = false)
    private String value;

    @NotBlank
    @Column(length = 20, updatable = false)
    private String category;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "term", cascade = {CascadeType.REMOVE})
    private List<BingoCheck> checks;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }


}