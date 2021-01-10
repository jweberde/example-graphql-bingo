package weber.de.example.graphql.bingo.publisher;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import weber.de.example.graphql.bingo.control.BingoRestartEvent;
import weber.de.example.graphql.bingo.entity.BingoCard;
import weber.de.example.graphql.bingo.publisher.event.BingoCardCreated;
import weber.de.example.graphql.bingo.publisher.event.BingoCardStatus;


@Slf4j
@Component
public class BingoCardUpdatePublisher {

    private final Flowable<BingoCardStatus> updatePublisher;
    private ObservableEmitter<BingoCardStatus> updateEmitter;

    private final Flowable<BingoRestartEvent> restartPublisher;
    private ObservableEmitter<BingoRestartEvent> restartEmitter;

    private final Flowable<BingoCardCreated> createPublisher;
    private ObservableEmitter<BingoCardCreated> createEmitter;

    public BingoCardUpdatePublisher() {
        Observable<BingoCardStatus> bingoCardUpdateObservable = Observable.create(emitter -> this.updateEmitter = emitter);
        bingoCardUpdateObservable.doOnSubscribe(c -> {
            log.info("New Subscriber: {}", c);
        });
        ConnectableObservable<BingoCardStatus> bingCardUpdateConnectableObservable = bingoCardUpdateObservable.share().publish();
        bingCardUpdateConnectableObservable.connect();

        updatePublisher = bingCardUpdateConnectableObservable.toFlowable(BackpressureStrategy.BUFFER);

        Observable<BingoCardCreated> bingoCardCreateObservable = Observable.create(emitter -> this.createEmitter = emitter);
        ConnectableObservable<BingoCardCreated> bingCardCreateConnectableObservable = bingoCardCreateObservable.share().publish();
        bingoCardCreateObservable.doOnSubscribe(c -> {
            log.info("New Subscriber: {}", c);

        });
        bingCardCreateConnectableObservable.connect();

        createPublisher = bingCardCreateConnectableObservable.toFlowable(BackpressureStrategy.BUFFER);

        //

        Observable<BingoRestartEvent> bingoRestartedObservable = Observable.create(emitter -> this.restartEmitter = emitter);
        ConnectableObservable<BingoRestartEvent> bingoRestartedObservableConnectable = bingoRestartedObservable.share().publish();
        bingoRestartedObservable.doOnSubscribe(c -> {
            log.info("New Subscriber: {}", c);
        });
        bingoRestartedObservableConnectable.connect();
        restartPublisher = bingoRestartedObservableConnectable.toFlowable(BackpressureStrategy.BUFFER);
    }

    public void publish(BingoRestartEvent bingoRestartEvent) {
        log.info("Publish RestartBingoEvent {}", bingoRestartEvent);
        restartEmitter.onNext(bingoRestartEvent);
    }

    public void publish(BingoCardStatus cardStatus) {
        log.info("Publish CardStatus {}", cardStatus.getCardId());
        updateEmitter.onNext(cardStatus);
    }

    public void publish(final BingoCard card) {
        log.info("Publish Card {}", card.getId());
        createEmitter.onNext(BingoCardCreated.of(card));
    }

    public Flowable<BingoRestartEvent> getRestartPublisher() {
        return restartPublisher;
    }

    public Flowable<BingoCardStatus> getUpdatePublisher() {
        return updatePublisher;
    }

    public Flowable<BingoCardCreated> getCreatePublisher() {
        return createPublisher;
    }


}
