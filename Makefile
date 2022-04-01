VERSION := 1.11

.inline-deps:
	lein inline-deps
	touch .inline-deps

.PHONY: deps
deps: .inline-deps

.PHONY: repl
repl:
	iced repl --without-cljs with-profile $(VERSION)

.PHONY: lint
lint:
	clj-kondo --lint src:test

.PHONY: coverage
coverage:
	lein with-profile +$(VERSION) cloverage \
	    --codecov \
	    --ns-exclude-regex 'icedtest\..*'

.PHONY: test
test: .inline-deps
	lein with-profile +plugin.mranderson/config test-all
.PHONY: dev-test
dev-test:
	lein with-profile +$(VERSION) test

.PHONY: install
install: .inline-deps
	lein with-profile +release,+plugin.mranderson/config install
.PHONY: dev-install
dev-install:
	lein with-profile +release install

.PHONY: release
release:
	lein with-profile +release release

.PHONY: deploy
deploy: .inline-deps
	lein with-profile +release,+plugin.mranderson/config deploy clojars

.PHONY: outdated
outdated:
	lein with-profile +antq run -m antq.core --upgrade

.PHONY: clean
clean:
	lein clean
	\rm -f .inline-deps
