.PHONY: build

build:
	@cd core && mvn package -P fatjar
	@cp core/target/karate-1.0-SNAPSHOT.jar karate-grpcurl-fatjar.jar