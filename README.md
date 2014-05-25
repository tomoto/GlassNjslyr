GlassNjslyr V1.1
===========

Sorry Japanese only.

## Description

習作として作成したGoogle Glass用ハンズフリー[ニンジャスレイヤー](http://wikiwiki.jp/njslyr/?%A5%CB%A5%F3%A5%B8%A5%E3%A5%B9%A5%EC%A5%A4%A5%E4%A1%BC%A4%C8%A4%CF%A1%A9)リーダーです。
(人生初のオリジナルAndroidアプリがこれかよ!)

* "OK Glass, [ゴウランガ](http://wikiwiki.jp/njslyr/?cmd=read&page=%A5%B4%A5%A6%A5%E9%A5%F3%A5%AC)！" で[ワザマエ](http://wikiwiki.jp/njslyr/?%A5%B3%A5%C8%A5%C0%A5%DE%A1%CA%A4%CF%B9%D4%A1%C1%A4%EF%B9%D4%A1%A6%B1%D1%BF%F4%B5%AD%B9%E6%A1%CB#oa0750b6)なスプラッシュとともに起動します。
* 頭を水平に回転してストーリーを選択し、隙の無い[オジギ](http://wikiwiki.jp/njslyr/?%A5%B3%A5%C8%A5%C0%A5%DE%A1%CA%A4%A2%B9%D4%A1%C1%A4%CA%B9%D4%A1%CB#e71fa9c8)を決めて読み始めます。
  * タッチでも操作できます。
* ~~謎のガイジンによる~~読み上げが行なわれます。(V1.1で日本語に対応しました)
  * ~~TTS(Text-to-speech)が英語のみ対応なので、テキストをkakasiで無理やりローマ字に変換し、英語版TTSに放り込むという[ジツ](http://wikiwiki.jp/njslyr/?%A5%B3%A5%C8%A5%C0%A5%DE%A1%CA%A4%A2%B9%D4%A1%C1%A4%CA%B9%D4%A1%CB#xf627878)を用いています。~~
  * [実際](http://wikiwiki.jp/njslyr/?%A5%B3%A5%C8%A5%C0%A5%DE%A1%CA%A4%A2%B9%D4%A1%C1%A4%CA%B9%D4%A1%CB#gba1566d)かなり熱くなる。
* ストーリーはWikiの[ヘッズのおすすめエピソード](http://wikiwiki.jp/njslyr/?%A4%E8%A4%AF%A4%A2%A4%EB%BC%C1%CC%E4#w8ef9162)より5本を決め打ちしてあります。

## Installation

* Gouranga.apk (デバッグ版apk)をダウンロードして adb install Gouranga.apk すればたぶんインストールできるでしょう。
* もしくはリポジトリを丸ごと取得してadt(上のEclipse)にインポートしてください。

## Video (on YouTube)

<a href="http://www.youtube.com/watch?feature=player_embedded&v=voAUlmiJwls
" target="_blank"><img src="http://img.youtube.com/vi/voAUlmiJwls/0.jpg" 
alt="video" width="240" height="180" border="10" /></a>

## Screenshots

![voice command](https://raw.github.com/tomoto/GlassNjslyr/master/img/Screenshot1.png) &nbsp;
![story selection](https://raw.github.com/tomoto/GlassNjslyr/master/img/Screenshot2.png) &nbsp;
![contents](https://raw.github.com/tomoto/GlassNjslyr/master/img/Screenshot3.png) &nbsp;

## Other TODOs
  * contextual voice command (not supported)
  * オジギアクション、ハイクアクション
  * ストーリー選択時のセンサー精度の改善、タッチ後disableされたセンサーの一定時間後の復活
  * ~~日本語TTS対応~~ (XE16からサポートされた日本語TTSを使うようにしました)
  * ストーリーのネットワークからのダウンロード
  * Launch Wiki, Togetter, etc.

## Change Log

* V1.1 XE16対応。謎のガイジンによる読み上げを日本語による読み上げに修正。
* V1.0 First release

