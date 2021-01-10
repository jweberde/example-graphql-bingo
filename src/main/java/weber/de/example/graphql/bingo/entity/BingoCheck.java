package weber.de.example.graphql.bingo.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "bingo_check_unique", columnNames = {
                "TERM_ID", "CARD_ID"
        })
)
@Data
@ToString(exclude = {"term", "card"})
public class BingoCheck {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    public UUID id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "TERM_ID", updatable = false)
    private BingoTerm term;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", updatable = false)
    private BingoCard card;

    @Column(name = "CARD_ID", updatable = false, insertable = false)
    private UUID cardId;

    @Column(name = "TERM_ID", updatable = false, insertable = false)
    private UUID termId;

    private boolean checked = false;
}