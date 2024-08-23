DOCKER_USERNAME := rodaka123
DOCKER_PASSWORD := rkddlswo218@
DOCKER_REPOSITORY := rodaka123/log

ec2_push_production_api:
	sudo ./gradlew clean -PcontainerImage=$(DOCKER_REPOSITORY) -PimageTag=latest -Pstage=production api:jib -x test -x ktlintMainSourceSetCheck -x ktlintTestSourceSetCheck

ec2_pull_and_start_production_api:
	echo ${DOCKER_PASSWORD} | docker login --username $(DOCKER_USERNAME) --password-stdin
	sudo docker pull $(DOCKER_REPOSITORY):latest
	cd ./deploy && sudo docker-compose down && sudo docker-compose up -d
