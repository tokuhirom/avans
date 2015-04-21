# Session

Avans ではセッションをサポートしています｡

HTTP はステートレスなプロトコルですから､リクエストをまたいでデータを持ち運ぶことはできません｡
しかし､セッション機能を利用するとリクエストをまたいでデータを持ち運ぶことが可能となります｡
セッション機能は､Cookie ヘッダを利用して実現されています｡

## セッション機能のセットアップ

    public class MyBaseController extends ControllerBase
      implements SessionMixin {
    }

のようにして､SessionMixin インターフェースを組み込んでください｡

基本的にはこれだけで利用可能な状態になります｡
(ただし､本番環境での利用には実用的ではありません)

## 利用例

    public class MyController extends MyBaseController {
      @GET("/")
      public WebResponse add() {
        Optional<Long> currentCounter = this.getSession().getLong("counter", Long.class);
        long counter = currentCounter.orElse(1);
        counter++;
        this.getSession().setLong("counter", counter);
        return this.renderTEXT("count:" + counter);
      }
    }

`this.getSession()` でセッションオブジェクトを取得できます｡

## CSRF/XSRF からの防御

TBD
