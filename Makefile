.PHONY: patch deps repl test install release deploy clean

VERSION := 1.10.1

inline-deps.patch:
	diff -uprN target.org/srcdeps target.new/srcdeps > inline-deps.patch

.inline-deps:
	lein inline-deps
	\rm -rf target.org && \cp -pir target target.org
	touch .inline-deps

.patch: inline-deps.patch
	\rm -rf target && \cp -pir target.org target
	(cd target && patch -p1 < ../inline-deps.patch)
	touch .patch

test_files/clojuredocs-export.json:
	curl -o $@ https://clojuredocs.org/clojuredocs-export.json

deps: .inline-deps .patch

repl:
	iced repl --without-cljs with-profile $(VERSION)

coverage:
	lein with-profile +$(VERSION) cloverage \
	    --codecov \
	    --ns-exclude-regex 'icedtest\..*'

test: .inline-deps .patch test_files/clojuredocs-export.json
	lein with-profile +plugin.mranderson/config test-all
dev-test: test_files/clojuredocs-export.json
	lein with-profile +$(VERSION) test

install: .inline-deps .patch
	lein with-profile +release,+plugin.mranderson/config install
dev-install:
	lein with-profile +release install

release:
	lein with-profile +release release

deploy: .inline-deps .patch
	lein with-profile +$(VERSION),+plugin.mranderson/config deploy clojars

clean:
	lein clean
	\rm -f .inline-deps .patch test_files/clojuredocs-export.json
