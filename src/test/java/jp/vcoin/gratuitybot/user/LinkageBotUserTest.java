package jp.vcoin.gratuitybot.user;

import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class LinkageBotUserTest {

    @Test
    public void idのみ成功() {
        Assert.assertThat(new LinkageBotUser("id:1234").getId(), is(1234L));
    }

    @Test
    public void forgratuityのみ成功() {
        Assert.assertThat(new LinkageBotUser("for-gratuity:false").isForGratuity(), is(false));
    }

    @Test
    public void foremojigratuityのみ成功() {
        Assert.assertThat(new LinkageBotUser("for-emoji-gratuity:true").isForEmojiGratuity(), is(true));
    }

    @Test
    public void commandsのみ成功() {
        Assert.assertThat(new LinkageBotUser("commands:hoge,fuga").getCommands().size(), is(2));
    }

    @Test
    public void 無効な項目のみ成功() {
        new LinkageBotUser("hogeru:1");
    }

    @Test
    public void 無効な項目とidのみ成功() {
        final LinkageBotUser linkageBotUser = new LinkageBotUser("hogeru:1;id:1234");
        Assert.assertThat(linkageBotUser.getId(), is(1234L));
    }

    @Test
    public void 複合成功() {
        final LinkageBotUser linkageBotUser = new LinkageBotUser("id:1234;for-emoji-gratuity:true;commands:hoge,fuga");
        Assert.assertThat(linkageBotUser.getId(), is(1234L));
        Assert.assertThat(linkageBotUser.isForEmojiGratuity(), is(true));
        Assert.assertThat(linkageBotUser.getCommands().size(), is(2));
    }

    @Test
    public void 成功_値なし() {
        Assert.assertThat(new LinkageBotUser("id:").getId(), is(nullValue()));
    }

    @Test
    public void 成功_値ありなし複合() {
        new LinkageBotUser("id:;for-gratuity:true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 失敗_空() {
        new LinkageBotUser("");
    }
}