.inline-deps:
	clojure -T:build inline-deps
	touch .inline-deps

.PHONY: inline-deps
inline-deps: clean .inline-deps

.PHONY: repl
repl:
	iced repl --without-cljs --force-clojure-cli -A:dev:injection

.PHONY: lint
lint:
	clj-kondo --lint src:test
	cljstyle check


.PHONY: test
test: .inline-deps
	clojure -M:dev:srcdeps:test
	clojure -M:dev:1.9:srcdeps:test
	clojure -M:dev:1.10:srcdeps:test

.PHONY: dev-test
dev-test:
	clojure -M:dev:injection:test

.PHONY: install
install: .inline-deps
	clojure -T:build install

.PHONY: coverage
coverage:
	clojure -M:dev:injection:coverage \
		--src-ns-path=src \
		--test-ns-path=test  \
		--codecov \
		--ns-exclude-regex 'icedtest\..*'

.PHONY: outdated
outdated:
	clojure -M:outdated --upgrade

.PHONY: clean
clean:
	\rm -rf target .inline-deps .cpcache
