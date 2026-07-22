SHELL := /bin/sh
MAVEN_IMAGE ?= maven:3.9.11-eclipse-temurin-21
MAVEN_RUN = docker run --rm -v "$$(pwd):/workspace" -v "$$(pwd)/.m2-cache:/root/.m2" -w /workspace $(MAVEN_IMAGE)

.PHONY: init up down logs test package verify clean smoke public-check license-report

init:
	./scripts/bootstrap-env.sh

up: init
	docker compose --env-file .env up --build --detach

down:
	docker compose --env-file .env down

logs:
	docker compose --env-file .env logs --follow app db

test:
	mkdir -p .m2-cache
	$(MAVEN_RUN) ./mvnw --batch-mode --no-transfer-progress test

package:
	mkdir -p .m2-cache
	$(MAVEN_RUN) ./mvnw --batch-mode --no-transfer-progress clean package

verify: test
	docker compose --env-file .env config --quiet
	docker build --build-arg MAVEN_IMAGE=$(MAVEN_IMAGE) --tag secure-mvc-starter:verify .

smoke:
	./scripts/smoke-test.sh

public-check:
	./scripts/public-release-check.sh repository

license-report:
	mkdir -p .m2-cache
	$(MAVEN_RUN) ./mvnw --batch-mode --no-transfer-progress generate-resources

clean:
	$(MAVEN_RUN) ./mvnw --batch-mode --no-transfer-progress clean
