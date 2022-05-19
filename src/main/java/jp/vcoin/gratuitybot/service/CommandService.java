package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.command.emoji.ReactionCommand;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.command.text.ShowDynamicSettingCommand;

import java.util.Map;

public interface CommandService {

    Map<String, MessageCommand> getMessageCommandMap();

    Map<Long, ReactionCommand> getReactionCommandMap(long serverId);

    Map<String, ShowDynamicSettingCommand> getShowDynamicSettingCommandMap();

    void evict(long serverId);
}
