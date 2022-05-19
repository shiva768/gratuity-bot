package jp.vcoin.gratuitybot.command;

import lombok.Getter;

import java.lang.annotation.Annotation;

public enum CommandType {

    Text(jp.vcoin.gratuitybot.marker.Text.class), Emoji(jp.vcoin.gratuitybot.marker.Emoji.class);

    @Getter
    private Class<? extends Annotation> marker;

    CommandType(Class<? extends Annotation> clazz) {
        this.marker = clazz;
    }
}
