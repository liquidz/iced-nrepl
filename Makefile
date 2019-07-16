.PHONY: deps repl coverage test dev-test install dev-install release deploy clean

VERSION := 1.10.1

inline-deps.patch:
	diff -uprN target.org/srcdeps target.new/srcdeps > inline-deps.patch

.inline-deps:
	lein inline-deps
	\rm -rf target.org && \cp -pir target target.org
	touch .inline-deps

.patch:
	\rm -rf target && \cp -pir target.org target
	(cd target && patch -p1 < ../inline-deps.patch)
	touch .patch

deps: .inline-deps .patch

repl:
	iced repl --without-cljs with-profile $(VERSION)

coverage:
	lein with-profile +$(VERSION) cloverage \
	    --codecov \
	    --ns-exclude-regex 'icedtest\..*'

test: .inline-deps .patch
	lein with-profile +plugin.mranderson/config test-all
dev-test:
	lein with-profile +$(VERSION) test

install: .inline-deps .patch
	lein with-profile +release,+plugin.mranderson/config install
dev-install:
	lein with-profile +release install

release:
	lein with-profile +release release

deploy: .inline-deps .patch
	lein with-profile +release,+plugin.mranderson/config deploy clojars

clean:
	lein clean
	\rm -f .inline-deps .patch
