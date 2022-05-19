package jp.vcoin.gratuitybot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicSettingKey implements Serializable {

    @Column(name = "key", nullable = false)
    private String key;
    @Column(name = "second_key")
    private String secondKey;
    @Column(name = "server")
    private long server;

}
