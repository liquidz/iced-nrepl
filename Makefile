.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.inline-deps:
	clojure -T:build inline-deps
	touch .inline-deps

.PHONY: inline-deps
inline-deps: clean .inline-deps ## Inline deps

.PHONY: repl
repl: ## Start REPL
	iced repl --without-cljs -A:dev:injection

.PHONY: lint
lint: ## Lint codes
	clj-kondo --lint src:test
	cljstyle check

.PHONY: test
test: .inline-deps ## Run tests with inlining
	clojure -M:dev:srcdeps:test
	clojure -M:dev:1.9:srcdeps:test
	clojure -M:dev:1.10:srcdeps:test

.PHONY: dev-test
dev-test: ## Run tests without inlining
	clojure -M:dev:injection:test

.PHONY: install
install: .inline-deps ## Install jar to local Maven repos
	clojure -T:build install

.PHONY: coverage
coverage: ## Show coverage
	clojure -M:dev:injection:coverage \
		--src-ns-path=src \
		--test-ns-path=test  \
		--codecov \
		--ns-exclude-regex 'icedtest\..*'

.PHONY: outdated
outdated: ## Show and upgrade outdated deps
	clojure -M:outdated --upgrade

.PHONY: clean
clean: ## Clean
	\rm -rf target .inline-deps .cpcache
