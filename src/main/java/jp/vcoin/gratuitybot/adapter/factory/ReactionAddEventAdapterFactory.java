package jp.vcoin.gratuitybot.adapter.factory;

import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import jp.vcoin.gratuitybot.adapter.impl.ReactionAddEventAdapterImpl;
import org.springframework.stereotype.Service;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

@Service
public class ReactionAddEventAdapterFactory {

    public ReactionAddEventAdapter getAdapter(ReactionAddEvent event) {
        return new ReactionAddEventAdapterImpl(event);
    }
}
