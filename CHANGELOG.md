# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [0.2.0] - 2018-07-09
### Changed
- Support Blob response type with `:response-type :blob`.
- Prevent autoregistering the effect under `:fetch` when the core namespace is required. **This is a breaking change.** Previously the core namespace called `(reg-fx :fetch fetch-effect)` and registered itself with re-frame automatically under the key `:fetch` if the library was required anywhere in a project. To use the effect you must now perform that step yourself and can provide any key you like.
