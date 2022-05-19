package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.marker.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;

@Component
@Slf4j
@Event
public class Logout implements IListener<DisconnectedEvent> {

    @Override
    public void handle(DisconnectedEvent event) {
        log.info("disconnected event");
    }
}
