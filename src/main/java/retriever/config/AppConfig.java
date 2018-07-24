package retriever.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

@Configuration
public class AppConfig {
    @Value("${mongo.address}")
    private String mongoAddress;

    @Value("${mongo.port}")
    private int mongoPort;

    @Bean
    public Mongo mongo() throws UnknownHostException {
        Mongo mongo = new MongoClient(mongoAddress, mongoPort);
        return mongo;
    }
}
