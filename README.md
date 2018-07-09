# fetch-fx

A [re-frame](https://github.com/Day8/re-frame) fx handler for the Javascript Fetch API.

## Usage

```clj
(ns foo
  (:require [re-frame.core :as rf]
            [fetch-fx.core :as fetch]))

(rf/reg-fx :your-key fetch/effect)
```

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
