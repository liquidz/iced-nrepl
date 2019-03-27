.PHONY: patch deps repl test install release deploy clean

VERSION := 1.9

source-deps.patch:
	diff -uprN target.org/srcdeps target.new/srcdeps > source-deps.patch

.source-deps:
	lein source-deps
	\rm -rf target.org && \cp -pir target target.org
	touch .source-deps

.patch:
	\rm -rf target && \cp -pir target.org target
	(cd target && patch -p1 < ../source-deps.patch)
	touch .patch

deps: .source-deps .patch

repl:
	iced repl --without-cljs with-profile $(VERSION)

test: .source-deps .patch
	lein with-profile +plugin.mranderson/config test-all

install: .source-deps .patch
	lein with-profile +$(VERSION),+plugin.mranderson/config install

release:
	lein with-profile +$(VERSION) release

deploy: .source-deps .patch
	lein with-profile +$(VERSION),+plugin.mranderson/config deploy clojars

clean:
	lein clean
	\rm -f .source-deps
