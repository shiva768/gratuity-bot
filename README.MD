VIRTUALCOIN チップボット
===============

## 構成  

\----------

### アプリケーションの構成  

- JDK : 1.8
- Framework : Spring Boot 2.0.2
- O/RM : JPA/Hibernate
- logger : logback
- plugin : lombok

\----------

## 環境  
- dev : 開発環境
- dev_prod : 開発環境だけど、logなどがprod同等
- stage : VIRTUALCOIN discordの試験チャンネル用のつもり
- prod : 本番

## 設定
設定が増えてきたので、説明追記
application.yml
```
application:
  version: @project.version@    # build時にmavenがなんかするのでそのまま
  author: @project.author@      # build時にmavenがなんかするのでそのまま
  http:
    connection-timeout-msec: 10000
    read-timeout-msec: 5000

discord:
  max-dispatch-thread-count: 64
  message:
    cache: 200
  support-language:         # 現在未使用
    - ja
    - en
  event:
    command-max-count: 4    # コマンドが最大何分割か ./hoge 1 @hoge 334 <= 4分割
    trigger-prefix: ./      # コマンドの開始文字列
    admin:
      commands:             # discord.event.admin.user.ids に含まれるユーザのみが実行出来る
        - stop
    text:
      color:                # record, helpの文字の色(property名がちょっと実際の使い方とあっていないので、あとでなおす)
        r: 255
        g: 192
        b: 0
      footer-icon: <icon-url>
      command:
        gratuity:
          default-random-min-amount: 100    # gratuityコマンドのrandomターゲット時のdefaultの最小値
        withdraw:
          min-amount: 1             # withdrawコマンドの最小値
      enable-channel-commands:      # チャンネル内での有効コマンド
        - balance
        - deposit
        - record
        - gratuity
        - withdraw
        - help
        - stop
      enable-dm-commands:           # DMでの有効コマンド
        - balance
        - deposit
        - record
        - withdraw
        - help

spring:                             # ←より下はsystem の設定
```

application-xxx.yml
```
application:
  http:
    virtualcoin-url: http://127.0.0.1:51200     # RPCのURL
    rpc-user: <user-id>                         # RPCのユーザ名
    rpc-password: <password>                    # RPCのパスワード

discord:
  bot:
    token: <token>
    server-id: <server-id>
    notification-channel-id: 438667563782897674                         # 起動、終了メッセージ通知チャンネルID
  channel-default-language:                                             # 言語にぶら下がったチャンネルIDはその言語がデフォルトとなる
    en:
      list:
        - 425818190040268811
  event:
    admin:
      user:
        ids:                                                            # 管理者コマンド(discord.event.admin.comannds)が利用できるユーザID
          - 260023416894128130
          - 418273971293650946
    text:
      channel-command-channel-ids:                                      # チャンネルコマンドが有効なチャンネルID
        - 438667563782897674
        - 433998591317508106
    emoji:
      notification-channel-id: 438683201720811520                       # スタンプによる投げ銭の履歴を通知するチャンネルID
      command:
        gratuity:
          emojis:                                                       # 絵文字IDと絵文字IDに対応する投げ銭値
            - id: 438664051954810882
              value: 1
            - id: 438664082917425152
              value: 10
            - id: 438664102307692556
              value: 100
            - id: 438664118589849600
              value: 1000

spring:
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:vcoins.db
```

## ビルド手順  
1. cd {project dir} 
2. `mvn clean package`

## 起動手順  
`java -jar -Dspring.profiles.active=環境名[dev, dev_prod, stage, prod] jar名`  
※ DBはsqliteで、java -jar キック時のディレクトリの./vcoins.dbを参照しようとします。  
  存在しない場合は、新規にvcoins.dbを作成して起動してしまうので、特別な事情がある場合を除いて java -jar キックはjarのあるディレクトリで行ってください。  
  僕はやらかして変な汁出ました。

## 補足
初回起動時のjpaによる自動テーブル生成だけだと、unique indexが作成されないので、以下を実行する必要がある
`create unique index unique_index on dynamic_settings(server, key, second_key);`