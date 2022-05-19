package jp.vcoin.gratuitybot;

import jp.vcoin.gratuitybot.marker.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import sx.blah.discord.api.IDiscordClient;

@SpringBootApplication
@EnableCaching
@Profile("!test")
public class VCoinsGratuityBotApplication implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final IDiscordClient discordClient;

    @Autowired
    public VCoinsGratuityBotApplication(ApplicationContext applicationContext, IDiscordClient discordClient) {
        this.applicationContext = applicationContext;
        this.discordClient = discordClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(VCoinsGratuityBotApplication.class, args);
    }

    @Override
    public void run(String... args) {
        discordClient.getDispatcher().registerListeners(applicationContext.getBeansWithAnnotation(Event.class).values().toArray());
        discordClient.login();
    }
}
