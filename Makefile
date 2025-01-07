DOCKER_USERNAME := ${shell echo DOCKER_USERNAME}
DOCKER_PASSWORD := ${shell echo DOCKER_PASSWORD}
DOCKER_REPOSITORY := rodaka123/log

ec2_push_production_api:
	./gradlew clean -PcontainerImage=$(DOCKER_REPOSITORY) -PimageTag=latest -Pstage=production api:jib --stacktrace -x test -x ktlintMainSourceSetCheck -x ktlintTestSourceSetCheck

ec2_pull_and_start_production_api:
	docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}
	sudo docker pull $(DOCKER_REPOSITORY):latest
	cd ./deploy && sudo docker-compose down && sudo docker-compose up -d
