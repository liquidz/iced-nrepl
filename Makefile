.PHONY: deps repl coverage test dev-test install dev-install release deploy clean

VERSION := 1.10.1

.inline-deps:
	lein inline-deps
	touch .inline-deps

deps: .inline-deps

repl:
	iced repl --without-cljs with-profile $(VERSION)

lint:
	clj-kondo --lint src:test

coverage:
	lein with-profile +$(VERSION) cloverage \
	    --codecov \
	    --ns-exclude-regex 'icedtest\..*'

test: .inline-deps
	lein with-profile +plugin.mranderson/config test-all
dev-test:
	lein with-profile +$(VERSION) test

install: .inline-deps
	lein with-profile +release,+plugin.mranderson/config install
dev-install:
	lein with-profile +release install

release:
	lein with-profile +release release

deploy: .inline-deps
	lein with-profile +release,+plugin.mranderson/config deploy clojars

clean:
	lein clean
	\rm -f .inline-deps
