# 개인 블로그 프로젝트

## 백엔드 프로젝트 사용기술

> <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
> <img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
> <img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
> <img src="https://img.shields.io/badge/spring boot jpa | query dsl-6DB33F?style=for-the-badge&logo=&logoColor=white">
> <img src="https://img.shields.io/badge/envers-6DB33F?style=for-the-badge&logo=&logoColor=white">
> <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
> <img src="https://img.shields.io/badge/flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">
> <img src="https://img.shields.io/badge/amazon rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">
> <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
> <img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
> <img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white">
> <img src="https://img.shields.io/badge/grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">
> <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
> <img src="https://img.shields.io/badge/ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white">
> <img src="https://img.shields.io/badge/amazon s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
> <img src="https://img.shields.io/badge/github actions-181717?style=for-the-badge&logo=githubactions&logoColor=white">
> <img src="https://img.shields.io/badge/discord logging-5865F2?style=for-the-badge&logo=discord&logoColor=white">
> <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">

## 배포 환경

> ![spring-docker](https://github.com/user-attachments/assets/7b26f824-e916-4685-9d45-759455d7170f)

## [프로젝트 서비스 링크](https://chobolevel.site)

> + **카페24 호스팅 서비스**에 도커를 이용하여 배포하였습니다.
> + 프론트엔드 프로젝트는 VERCEL 서비스를 이용하였습니다.
> + **SpringBoot, Nginx, Redis, Certbot, prometheus, grafana** 등 총 6개의 도커 컨테이너가 실행 중인 상태입니다.
> + DB는 AWS의 RDS 서비스를 사용하였습니다.

## 프로젝트를 진행한 이유

> 여러 블로그를 참고하면서 능력있는 개발자들의 블로그는 보통 직접 개발한 경우가 많다고 보아서 따라해보게 되었습니다.<br />
> 이전까지 배운 내용들을 활용해보고 중간중간 알게된 새로운 기술도 익히기 위해 진행하였습니다.

## CI/CD

> **github actions, Jib 라이브러리, 도커 허브 그리고 Makefile 커맨드와 docker-compose** 등을 이용해 브랜치의 푸시 이벤트를 감지하여 도커 허브 레포에 도커 이미지를 빌드하여 푸시하도록 하였습니다.<br />
> 이후 해당 이미지를 서버에서 다운로드 받아서 도커 컨테이너로 실행하도록 구성하였습니다.

## DB 테이블 스키마

> ![log-table-scheme](https://github.com/user-attachments/assets/c06d3419-039f-461b-b4b1-b47774605619)
>
> 각 테이블은 스프링의 envers 통해 **등록/수정/삭제 시 히스토리를 관리**하도록 했습니다.

## SWAGGER UI

> [**SWAGGER UI LINK**](https://api.chobolevel.site/swagger-ui/index.html)
>
> 작성한 API들을 문서 형식으로 확인 가능하도록 작성해봤습니다.

## 모니터링 대시보드

> [**DASHBOARD LINK**](http://210.114.22.52:3000/d/spring_boot_21/f82469d?orgId=1&from=now-1h&to=now&var-application=&var-instance=210.114.22.52:9565&var-hikaricp=HikariPool-1&var-memory_pool_heap=$__all&var-memory_pool_nonheap=$__all&refresh=5s)
> 
> actuator + prometheus + grafana 조합을 통해 모니터링 대시보드를 구성하였습니다.

## 초기 화면

> ![www chobolevel site_ (2)](https://github.com/user-attachments/assets/91c9856a-1916-46f1-9a60-163a1b43f716)

