package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.command.emoji.ReactionCommand;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.command.text.ShowDynamicSettingCommand;
import jp.vcoin.gratuitybot.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jp.vcoin.gratuitybot.command.CommandType.Emoji;
import static jp.vcoin.gratuitybot.command.CommandType.Text;

@Service
public class CommandServiceImpl implements CommandService {

    private final ApplicationContext applicationContext;

    @Autowired
    public CommandServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Map<String, MessageCommand> getMessageCommandMap() {
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Text.getMarker());
        return beansWithAnnotation.keySet().stream().collect(Collectors.toMap(k ->
                k, k -> (MessageCommand) beansWithAnnotation.get(k)
        ));
    }

    @Cacheable(value = "commands", sync = true)
    @Override
    public Map<Long, ReactionCommand> getReactionCommandMap(long serverId) {
        final Map<Long, ReactionCommand> result = new HashMap<>();
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Emoji.getMarker());
        beansWithAnnotation.keySet().forEach(k -> {
            final ReactionCommand reactionCommand = (ReactionCommand) beansWithAnnotation.get(k);
            final List<Long> triggerEmoji = getTriggerEmoji(reactionCommand, serverId);
            triggerEmoji.forEach(l -> result.put(l, reactionCommand));
        });
        return result;
    }

    @CacheEvict(cacheNames = "commands")
    @Override
    public void evict(long serverId) {
    }

    @Override
    public Map<String, ShowDynamicSettingCommand> getShowDynamicSettingCommandMap() {
        return applicationContext.getBeansOfType(ShowDynamicSettingCommand.class);
    }

    @SuppressWarnings("unchecked")
    private List<Long> getTriggerEmoji(ReactionCommand reactionCommand, long serverId) {
        return reactionCommand.getTriggerEmoji(serverId);
    }
}
