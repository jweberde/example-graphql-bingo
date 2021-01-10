package weber.de.example.graphql.bingo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

@EnableScheduling
@SpringBootApplication
public class BingoApplication {

    static {
        Locale.setDefault(Locale.US);
    }

    public static void main(String[] args) {
        SpringApplication.run(BingoApplication.class, args);
    }

}
