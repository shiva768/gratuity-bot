package jp.vcoin.gratuitybot.domain;

import com.google.common.base.CaseFormat;
import jp.vcoin.gratuitybot.StreamHelper;
import lombok.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkageBotUser implements DynamicSettingDomain {

    private static Map<String, Field> fieldMap;

    static {
        fieldMap = Arrays.stream(LinkageBotUser.class.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toMap(Field::getName, f -> f));
    }

    private DynamicSettingKey key;
    private Long id;
    private Long publicMessageChannelId;
    private boolean forGratuity;
    private boolean forEmojiGratuity;
    private List<String> commands;

    public LinkageBotUser(String value) {
        bind(null, value);
    }

    public LinkageBotUser(Long value) {
        this.id = value;
    }

    private static void setPrimitiveOrWrapper(Object o, Field f, String value) throws IllegalArgumentException, IllegalAccessException {
        if (f.getType() == boolean.class || f.getType() == Boolean.class) {
            f.set(o, Boolean.valueOf(value));
        } else if (f.getType() == byte.class || f.getType() == Byte.class) {
            f.set(o, Byte.valueOf(value));
        } else if (f.getType() == char.class || f.getType() == Character.class) {
            f.set(o, value.charAt(0));
        } else if (f.getType() == short.class || f.getType() == Short.class) {
            f.set(o, Short.valueOf(value));
        } else if (f.getType() == int.class || f.getType() == Integer.class) {
            f.set(o, Integer.valueOf(value));
        } else if (f.getType() == long.class || f.getType() == Long.class) {
            f.set(o, Long.valueOf(value));
        } else if (f.getType() == float.class || f.getType() == Float.class) {
            f.set(o, Float.valueOf(value));
        } else if (f.getType() == double.class || f.getType() == Double.class) {
            f.set(o, Double.valueOf(value));
        }
    }

    @Override
    public void bind(DynamicSetting dynamicSetting) {
        bind(dynamicSetting.getKeys(), dynamicSetting.getContent());
    }

    @Override
    public String serialize() {
        return fieldMap.keySet().stream().map(StreamHelper.throwingFunction(k -> {
            final Field field = fieldMap.get(k);
            String formattedKey = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, k);
            if (field.getType() == List.class)
                //noinspection unchecked
                return formattedKey + ":" + Optional.ofNullable(field.get(this)).map(v -> ((ArrayList<String>) v).stream().collect(Collectors.joining(","))).orElse(null);
            return formattedKey + ":" + field.get(this);
        })).collect(Collectors.joining(";"));
    }

    @Override
    public String format() {
        return String.join("\n",
                String.format("[id]:<@%d>(%s%d%s)", id, BOLD, id, BOLD),
                Optional.ofNullable(publicMessageChannelId)
                        .map(c -> String.format("[public-message-channel-id]:<#%s>(%s%s%s)", c, BOLD, c, BOLD))
                        .orElse(String.format("[public-message-channel-id]:%s%s%s", BOLD, null, BOLD)),
                String.format("[for-gratuity]:%s%b%s", BOLD, forGratuity, BOLD),
                String.format("[for-emoji-gratuity]:%s%b%s", BOLD, forEmojiGratuity, BOLD),
                String.format("[commands]:%s%s%s", BOLD, commands == null ? null : String.join(",", commands), BOLD)
        );
    }

    private void bind(DynamicSettingKey key, String value) {
        this.key = key;
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException("not empty");
        final String[] values = value.split(";");
        Arrays.stream(values).forEach(v -> {
                    final String[] element = v.split(":");
                    if (element.length < 2) return;
                    final Optional<Field> field = Optional.ofNullable(fieldMap.get(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, element[0].trim())));
                    field.ifPresent(StreamHelper.throwingConsumer(f -> set(this, f, element[1].trim())));
                }
        );
    }

    private void set(Object o, Field f, String value) throws IllegalArgumentException, IllegalAccessException {
        if (value.equals("null")) return;
        if (f.getType() == List.class) {
            f.set(o, Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toList()));
            return;
        } else if (f.getType() == BigDecimal.class) {
            f.set(o, new BigDecimal(value));
            return;
        }
        setPrimitiveOrWrapper(o, f, value);
    }
}
