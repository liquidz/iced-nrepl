.PHONY: javac deps patch repl test install deploy clean

.diff-match-patch:
	git clone https://github.com/google/diff-match-patch .diff-match-patch

source-deps.patch:
	diff -uprN target.org/srcdeps target.new/srcdeps > source-deps.patch

.source-deps:
	lein source-deps
	\rm -rf target.org && \cp -pir target target.org
	touch .source-deps

patch:
	\rm -rf target && \cp -pir target.org target
	(cd target && patch -p1 < ../source-deps.patch)

javac: .diff-match-patch
	lein javac

deps: .source-deps patch

repl: javac .source-deps
	iced repl with-profile +plugin.mranderson/config

test: javac .source-deps
	lein with-profile +plugin.mranderson/config test-all

install: javac .source-deps
	lein with-profile +plugin.mranderson/config install

release:
	lein release

deploy: javac .source-deps
	lein with-profile +plugin.mranderson/config deploy clojars

clean:
	lein clean
	\rm -rf .diff-match-patch
	\rm -f .source-deps
