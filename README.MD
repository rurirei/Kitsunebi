## Rewrite: Kitsunebi
![debug](https://github.com/rurirei/kitsunebi/workflows/debug/badge.svg?branch=debug) ![release](https://github.com/rurirei/kitsunebi/workflows/release/badge.svg?branch=build)

client on Android 8.0 up to 11, simply based and rewrite on [eycorsican/kitsunebi-android](https://github.com/eycorsican/kitsunebi-android). shorten name to `Re:Kit` (undefined).

### Features
- Compatibility for v2ray-core v4.28.0-, so you can use `alterId 0` without forced vmess aead. To do this, add `vmessAEAD: false` at your vmess conf
- Complete v2ray-core support
- Per-App-Proxy List backup
- Fake DNS
- Local DNS (will)
- TODO [#4](https://github.com/rurirei/kitsunebi/issues/4)

### Credits
- [v2fly/v2ray-core](https://github.com/v2fly/v2ray-core)
- [2dust/v2rayNG](https://github.com/2dust/v2rayNG)
- [shadowsocks/shadowsocks](https://github.com/shadowsocks/shadowsocks-android)
- [eycorsican/kitsunebi-android](https://github.com/eycorsican/kitsunebi-android)
- [eycorsican/go-tun2socks](https://github.com/eycorsican/go-tun2socks)
- [trojan-gfw/igniter-go-libs](https://github.com/trojan-gfw/igniter-go-libs)

### Contribution
welcome for your pull request with those requirements:
 - language: `kotlin` preferred for android platform, `golang` only for native implementation
 - style: clean code, no deprecated or dirty method
 - sdk: complete support for api 28 and above
 - commit: use at base branch [debug](https://github.com/rurirei/kitsunebi/tree/debug)
