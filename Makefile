DOCKER_REPOSITORY := rodaka123/log

ec2_push_production_api:
	@echo "username: ${DOCKER_HUB_USERNAME}"
	./gradlew clean -PcontainerImage=$(DOCKER_REPOSITORY) -PimageTag=latest -Pstage=production api:jib -Djib.to.auth.username=${DOCKER_HUB_USERNAME} -Djib.to.auth.password=${DOCKER_HUB_PASSWORD} -x test -x ktlintMainSourceSetCheck -x ktlintTestSourceSetCheck

ec2_pull_and_start_production_api:
	@echo "docker login -username ${DOCKER_USERNAME} --password-stdin ${DOCKER_PASSWORD}"
	sudo docker pull $(DOCKER_REPOSITORY):latest
	cd ./deploy && sudo docker-compose down && sudo docker-compose up -d
