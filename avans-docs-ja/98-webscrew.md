# webscrew について

Avans は webscrew というライブラリの上に成り立っている｡

## なぜ今 Servlet API なのか?

Author が所属している会社では､Java のソフトウェア資産があり､Servlet Filter の導入を要請される可能性があります｡
まあ､そういうことです｡

そして､他の選択肢もあまりありえませんよね｡

## webscrew とはなんなのか?

とは言え､サーブレットAPIはそのままでは非常に使いづらいです｡

サーブレットAPIは､いわば PSGI と同程度の機能しかもっておらず multipart/form-data のハンドリングすらしてくれません｡
このあたりをフレームワークの中にゴリゴリと書くよりは､Plack 的なレイヤーを用意してやったほうがいいよね~的なノリです｡

要するに Servlet API 用の Plack::Request/Plack::Response 的なやつです｡
