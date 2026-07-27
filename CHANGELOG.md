# Changelog

## 0.1.0-alpha.6 (2026-07-27)

Full Changelog: [v0.1.0-alpha.5...v0.1.0-alpha.6](https://github.com/hanzoai/java-sdk/compare/v0.1.0-alpha.5...v0.1.0-alpha.6)

### Features

* **stlc:** configurable CI runner and private-production-repo support in workflow templates ([5ccfcd3](https://github.com/hanzoai/java-sdk/commit/5ccfcd31dbcd562012b06a749638a09e4cdea4fe))


### Chores

* **brand:** dynamic hero banner ([76d50f0](https://github.com/hanzoai/java-sdk/commit/76d50f0ceb26da97070f31254d2cf9c312222a80))


### Documentation

* **brand:** add hero banner ([de85e51](https://github.com/hanzoai/java-sdk/commit/de85e513db40dfcd147f8d310e8c4f245973e27a))

## 0.1.0-alpha.5 (2026-06-17)

Full Changelog: [v0.1.0-alpha.4...v0.1.0-alpha.5](https://github.com/hanzoai/java-sdk/compare/v0.1.0-alpha.4...v0.1.0-alpha.5)

### Chores

* sync repo ([e5c80ee](https://github.com/hanzoai/java-sdk/commit/e5c80ee8d095a4d80465eef186ee8745826d1d52))


### Documentation

* tidy LLM.md indexes; CLAUDE.md -&gt; LLM.md symlink convention ([f6b9d51](https://github.com/hanzoai/java-sdk/commit/f6b9d51d9c5c820c499470521956bbf9064e8bd6))

## 0.1.0-alpha.4 (2026-06-01)

Full Changelog: [v0.1.0-alpha.3...v0.1.0-alpha.4](https://github.com/hanzoai/java-sdk/compare/v0.1.0-alpha.3...v0.1.0-alpha.4)

### Features

* **api:** api update ([6a5c942](https://github.com/hanzoai/java-sdk/commit/6a5c942be9a61eb93a4aa05e380777a863f5c03d))
* **client:** add connection pooling option ([fba313c](https://github.com/hanzoai/java-sdk/commit/fba313c64ea9b04525494200653fe3669677dbf3))


### Bug Fixes

* **client:** allow updating header/query affecting fields in `toBuilder()` ([8cf6f93](https://github.com/hanzoai/java-sdk/commit/8cf6f935307c05840390e972e1a1dd73fa1aafab))
* **client:** incorrect `Retry-After` parsing ([577f45f](https://github.com/hanzoai/java-sdk/commit/577f45f71570456c0861bbcf155fe972cb68e8be))


### Chores

* **ci:** skip lint on metadata-only changes ([5f08f1c](https://github.com/hanzoai/java-sdk/commit/5f08f1cac913e215e651c21c9de0a0745c3a5acb))
* **ci:** skip uploading artifacts on stainless-internal branches ([b81b416](https://github.com/hanzoai/java-sdk/commit/b81b416d00611d4f7d51112d4da8e10835b51eda))
* drop apache dependency ([3f59101](https://github.com/hanzoai/java-sdk/commit/3f591015b6f1701ac2571f794f3f2f3391b3ae39))
* **internal:** allow passing args to `./scripts/test` ([ab07f70](https://github.com/hanzoai/java-sdk/commit/ab07f70005e85d10f939bfa852865094539051f3))
* **internal:** bump ktfmt ([c5969c8](https://github.com/hanzoai/java-sdk/commit/c5969c8c0c88e4e88893f1ba64d7940252c578b1))
* **internal:** bump palantir-java-format ([61e03d4](https://github.com/hanzoai/java-sdk/commit/61e03d4f071d4adb7bcb2e052c63825781776a8e))
* **internal:** codegen related update ([2564be5](https://github.com/hanzoai/java-sdk/commit/2564be5a17f8741e8410a0cfca06f5dc49df9a88))
* **internal:** expand imports ([2c74317](https://github.com/hanzoai/java-sdk/commit/2c7431780363720dcefea6ff782365593430c89c))
* **internal:** make `OkHttp` constructor internal ([de8a086](https://github.com/hanzoai/java-sdk/commit/de8a0862639fd32f3c45f0539905200febe18aa5))
* **internal:** remove mock server code ([f9feeb4](https://github.com/hanzoai/java-sdk/commit/f9feeb42545b6defda1a7003dd9f9dcbf79e896f))
* **internal:** tweak CI branches ([2d74825](https://github.com/hanzoai/java-sdk/commit/2d748253896ff073d12c670b74c7ea44879b2981))
* **internal:** update `TestServerExtension` comment ([42da073](https://github.com/hanzoai/java-sdk/commit/42da073830c58475443795f4a36635925f11a73d))
* **internal:** update gitignore ([8d3685f](https://github.com/hanzoai/java-sdk/commit/8d3685f4a461c90abe0918255af72682fcb96a7d))
* **internal:** update retry delay tests ([1bf7ac9](https://github.com/hanzoai/java-sdk/commit/1bf7ac91a35acd2b92b049f529f280ab303249b2))
* **internal:** upgrade AssertJ ([b191a70](https://github.com/hanzoai/java-sdk/commit/b191a701a1df83215695ac5c684336ba7f5f9189))
* make `Properties` more resilient to `null` ([0f1fecf](https://github.com/hanzoai/java-sdk/commit/0f1fecfc945222275175b57c77e4145880a31762))
* update mock server docs ([9b30b6f](https://github.com/hanzoai/java-sdk/commit/9b30b6fa919390e81f44e9d331e0fec4f951c79d))
* update placeholder string ([8ebc000](https://github.com/hanzoai/java-sdk/commit/8ebc000175a03da2caa742dd0c376aa734eaf760))

## 0.1.0-alpha.3 (2026-01-28)

Full Changelog: [v0.1.0-alpha.2...v0.1.0-alpha.3](https://github.com/hanzoai/java-sdk/compare/v0.1.0-alpha.2...v0.1.0-alpha.3)

### ⚠ BREAKING CHANGES

* **client:** extract auto pagination to shared classes
* **client:** **Migration:** - If you were referencing the `AutoPager` class on a specific `*Page` or `*PageAsync` type, then you should instead reference the shared `AutoPager` and `AutoPagerAsync` types, under the `core` package
    - `AutoPagerAsync` now has different usage. You can call `.subscribe(...)` on the returned object instead to get called back each page item. You can also call `onCompleteFuture()` to get a future that completes when all items have been processed. Finally, you can call `.close()` on the returned object to stop auto-paginating early
    - If you were referencing `getNextPage` or `getNextPageParams`:
       - Swap to `nextPage()` and `nextPageParams()`
       - Note that these both now return non-optional types (use `hasNextPage()` before calling these, since they will throw if it's impossible to get another page)

### Features

* **api:** api update ([93f7cd0](https://github.com/hanzoai/java-sdk/commit/93f7cd095fb6cdb1003bad2f3c3914bcbde48bf6))
* **api:** api update ([589ce7a](https://github.com/hanzoai/java-sdk/commit/589ce7af97f7b1fcea2519ae75f184ea7a6529ff))
* **api:** api update ([5997aeb](https://github.com/hanzoai/java-sdk/commit/5997aeb3a12a1dbb0e628c3ae2e2b73d6f5c0391))
* **api:** api update ([bb22539](https://github.com/hanzoai/java-sdk/commit/bb225395cd286571c72d6ad48582d0f5b54b9db9))
* **api:** api update ([#9](https://github.com/hanzoai/java-sdk/issues/9)) ([a0ab87a](https://github.com/hanzoai/java-sdk/commit/a0ab87ace8769fc2320a22cf1bd2d259ede567ae))
* **client:** add enum validation method ([ebf7b97](https://github.com/hanzoai/java-sdk/commit/ebf7b9734d07621d28d394281160823dea9c8de3))
* **client:** allow providing some params positionally ([4f35e18](https://github.com/hanzoai/java-sdk/commit/4f35e180123ecba96578a2eab86af922115cd005))
* **client:** expose request body setter and getter ([#16](https://github.com/hanzoai/java-sdk/issues/16)) ([69c2c6c](https://github.com/hanzoai/java-sdk/commit/69c2c6cd26d41cd1e0a5012398cd8ba54a7617ca))
* **client:** extract auto pagination to shared classes ([e735ee9](https://github.com/hanzoai/java-sdk/commit/e735ee9fb131714101989c4d8a724399932e8026))
* **client:** make datetime deserialization more lenient ([#15](https://github.com/hanzoai/java-sdk/issues/15)) ([d1cf89d](https://github.com/hanzoai/java-sdk/commit/d1cf89db05f61aa38c0a8d274ba3ee77d17e0341))
* **client:** make union deserialization more robust ([#14](https://github.com/hanzoai/java-sdk/issues/14)) ([ebf7b97](https://github.com/hanzoai/java-sdk/commit/ebf7b9734d07621d28d394281160823dea9c8de3))
* **client:** support setting base URL via env var ([7b40d13](https://github.com/hanzoai/java-sdk/commit/7b40d13ad642b0db4f872cf54cb0e75a8fb5fc1c))


### Bug Fixes

* **client:** add missing convenience methods ([afd652a](https://github.com/hanzoai/java-sdk/commit/afd652a14b550ae68fc192379c2649f3dea63601))
* **client:** bump to better jackson version ([21f5b79](https://github.com/hanzoai/java-sdk/commit/21f5b79e8a12fb75bb6ea91fc46173f4fed1fc8f))
* **client:** don't call `validate()` during deserialization if we don't have to ([#11](https://github.com/hanzoai/java-sdk/issues/11)) ([e5af84a](https://github.com/hanzoai/java-sdk/commit/e5af84a40d2f50556adcb9b54d088ff4482c81c7))
* **client:** limit json deserialization coercion ([#12](https://github.com/hanzoai/java-sdk/issues/12)) ([a476286](https://github.com/hanzoai/java-sdk/commit/a4762869b0ad34a0212ed4ea2fc00cce3a020487))
* **client:** return `Optional&lt;T&gt;` instead of `Optional<? extends T>` ([#19](https://github.com/hanzoai/java-sdk/issues/19)) ([becb670](https://github.com/hanzoai/java-sdk/commit/becb670d92fab9e105289da811f8b2ce3c63d776))
* pluralize `list` response variables ([#13](https://github.com/hanzoai/java-sdk/issues/13)) ([a7a0b4a](https://github.com/hanzoai/java-sdk/commit/a7a0b4a6b847da6df6b459af2c149c7569ad9e0c))


### Performance Improvements

* **client:** cached parsed type in `HttpResponseFor` ([#17](https://github.com/hanzoai/java-sdk/issues/17)) ([91c37b1](https://github.com/hanzoai/java-sdk/commit/91c37b152243864b852b3ab04c05ab0aadf28912))
* **internal:** improve compilation+test speed ([668e12a](https://github.com/hanzoai/java-sdk/commit/668e12a649f917543d6a44810cdce0f035993afe))


### Chores

* **ci:** add timeout thresholds for CI jobs ([2711202](https://github.com/hanzoai/java-sdk/commit/271120209107485ef828f66150c6b32c14db9d2f))
* **ci:** only use depot for staging repos ([76c8fa1](https://github.com/hanzoai/java-sdk/commit/76c8fa1b7cc7a685bbc0326773579dee3120a678))
* **client:** remove unnecessary json state from some query param classes ([ebf7b97](https://github.com/hanzoai/java-sdk/commit/ebf7b9734d07621d28d394281160823dea9c8de3))
* configure new SDK language ([4e8ff89](https://github.com/hanzoai/java-sdk/commit/4e8ff890138cf98a46038bf5ca5484820136929a))
* **docs:** grammar improvements ([ccd164e](https://github.com/hanzoai/java-sdk/commit/ccd164eaddd08e28deb6dc7934e27b75febee968))
* **internal:** add invalid json deserialization tests ([ebf7b97](https://github.com/hanzoai/java-sdk/commit/ebf7b9734d07621d28d394281160823dea9c8de3))
* **internal:** add json roundtripping tests ([ebf7b97](https://github.com/hanzoai/java-sdk/commit/ebf7b9734d07621d28d394281160823dea9c8de3))
* **internal:** codegen related update ([f5151f7](https://github.com/hanzoai/java-sdk/commit/f5151f714c25e8331f9da13e5f3cf0169d2c1bd5))
* **internal:** codegen related update ([0b6be6f](https://github.com/hanzoai/java-sdk/commit/0b6be6fc53010ddef052f0142400aa2599225a26))
* **internal:** expand CI branch coverage ([1eddeb1](https://github.com/hanzoai/java-sdk/commit/1eddeb17b64eb80f6db6969d181c8f48ec9a08a6))
* **internal:** java 17 -&gt; 21 on ci ([2bcbac5](https://github.com/hanzoai/java-sdk/commit/2bcbac55db86820f67047c1be46898513970bee8))
* **internal:** reduce CI branch coverage ([c8a88d7](https://github.com/hanzoai/java-sdk/commit/c8a88d7303cd41aada70dea065bb8a51522c3373))
* **internal:** remove flaky `-Xbackend-threads=0` option ([2548d25](https://github.com/hanzoai/java-sdk/commit/2548d2575aaab0c2dbcf87b9df3e9d04cd2f9f25))
* **internal:** swap from `getNullable` to `getOptional` ([#18](https://github.com/hanzoai/java-sdk/issues/18)) ([b4a0c8b](https://github.com/hanzoai/java-sdk/commit/b4a0c8b295e7216dd22f472b4ee4198664f7dad0))
* **internal:** update java toolchain ([5c62cf3](https://github.com/hanzoai/java-sdk/commit/5c62cf3a190d7fc3d2c0def9b589c7dceb0bb025))
* **internal:** use `byteInputStream()` in tests ([afd652a](https://github.com/hanzoai/java-sdk/commit/afd652a14b550ae68fc192379c2649f3dea63601))


### Documentation

* add comments to `JsonField` classes ([becb670](https://github.com/hanzoai/java-sdk/commit/becb670d92fab9e105289da811f8b2ce3c63d776))
* **client:** update jackson compat error message ([b2310d3](https://github.com/hanzoai/java-sdk/commit/b2310d32ce73c02a5942f04fbfc66d38c6c371d7))
* document how to forcibly omit required field ([4dc5520](https://github.com/hanzoai/java-sdk/commit/4dc5520842ee6f01676aa386177d8924430fc3fa))
* explain http client customization ([e43d638](https://github.com/hanzoai/java-sdk/commit/e43d6381ec86c1c7372e03fede163b2bd4fa673a))
* explain jackson compat in readme ([b62faed](https://github.com/hanzoai/java-sdk/commit/b62faed4882e4f2a39014a11fda5297c09269a07))
* swap examples used in readme ([#20](https://github.com/hanzoai/java-sdk/issues/20)) ([4dc5520](https://github.com/hanzoai/java-sdk/commit/4dc5520842ee6f01676aa386177d8924430fc3fa))
* update documentation links to be more uniform ([c878297](https://github.com/hanzoai/java-sdk/commit/c878297f959b3215eacb52b1579d4dc0eae1f79b))

## 0.1.0-alpha.2 (2025-03-27)

Full Changelog: [v0.1.0-alpha.1...v0.1.0-alpha.2](https://github.com/hanzoai/java-sdk/compare/v0.1.0-alpha.1...v0.1.0-alpha.2)

### Chores

* update SDK settings ([#6](https://github.com/hanzoai/java-sdk/issues/6)) ([db7f67a](https://github.com/hanzoai/java-sdk/commit/db7f67aaab4d79b434ee3f7e1e4c51dc37cfcd1c))

## 0.1.0-alpha.1 (2025-03-27)

Full Changelog: [v0.0.1-alpha.0...v0.1.0-alpha.1](https://github.com/hanzoai/java-sdk/compare/v0.0.1-alpha.0...v0.1.0-alpha.1)

### Features

* **api:** api update ([#4](https://github.com/hanzoai/java-sdk/issues/4)) ([44028d5](https://github.com/hanzoai/java-sdk/commit/44028d59e98a3f2e32172a5c219097f6f27da433))
* **api:** manual updates ([#3](https://github.com/hanzoai/java-sdk/issues/3)) ([1487697](https://github.com/hanzoai/java-sdk/commit/1487697b105d2b01a2db415cd740168a282bf30b))


### Chores

* configure new SDK language ([938d5c6](https://github.com/hanzoai/java-sdk/commit/938d5c60845573651f83ca7095d10ae03850091e))
* go live ([#1](https://github.com/hanzoai/java-sdk/issues/1)) ([a72d2b3](https://github.com/hanzoai/java-sdk/commit/a72d2b3ffa5042d2a5ee3002090a80a9ceac6ae3))
