package weber.de.example.graphql.bingo.entity;

import lombok.Data;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(indexes = {
        @Index(name = "idx_card_createdat", columnList = "createdAt")
})
public class BingoCard {

    public static final Comparator<? super BingoCardStatus> BY_PROGRESS = (a, b) -> {
        int compareMissing = Integer.compare(a.getMissingCount(), b.getMissingCount());
        if (compareMissing != 0) {
            return compareMissing;
        }
        int compareChecked = Integer.compare(a.getCheckedCount(), b.getCheckedCount());
        if (compareChecked != 0) {
            return compareChecked;
        }
        // TIE BREAKER
        return a.getCardId().compareTo(b.getCardId());
    };

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    public UUID id;

    @NotBlank
    @Column(length = 255, updatable = false)
    private String owner;

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

    @OneToMany(mappedBy = "card", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<BingoCheck> checks;
}