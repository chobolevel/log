# [✍개인 블로그 프로젝트✍](https://www.chobolevel.co.kr/)(2024.08.08 ~)

## [프론트엔드 프로젝트 레포로 이동](https://github.com/chobolevel/log-fe)

## 목차

> 1. [백엔드 프로젝트 사용기술](#백엔드-프로젝트-사용기술)
> 2. [배포 환경](#배포-환경)
> 3. [프로젝트를 진행한 이유](#프로젝트를-진행한-이유)
> 4. [CI/CD](#cicd)
> 5. [DB 테이블 스키마](#db-테이블-스키마)
> 6. [SWAGGER UI(API DOCS)](#swagger-ui)
> 7. [모니터링](#모니터링-대시보드)
> 8. [초기 화면](#초기-화면)
> 9. [주요 기능](#주요-기능)

## 프로젝트 사용기술

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
> 
> + **카페24 호스팅 서비스**에 도커를 이용하여 배포하였습니다.
> + **SpringBoot, Nginx, Redis, Certbot, prometheus, loki, grafana** 등 총 6개의 도커 컨테이너가 실행 중인 상태입니다.
> + DB는 **AWS의 RDS 서비스**를 사용하였습니다.

## 프로젝트를 진행한 이유

> + 여러 블로그를 참고하면서 능력있는 개발자들의 블로그는 보통 직접 개발한 경우가 많다고 보아서 따라해보게 되었습니다.
> + 이전까지 배운 내용들을 활용해보고 중간중간 알게된 새로운 기술도 익히기 위해 진행하였습니다.

## CI/CD

> + **github actions, Jib 라이브러리, 도커 허브 그리고 Makefile 커맨드와 docker-compose** 등을 이용해 브랜치의 푸시 이벤트를 감지하여 도커 허브 레포에 도커 이미지를 빌드하여 푸시하도록 하였습니다.
> + 이후 해당 이미지를 도커 허브에서 다운로드 받아 도커 컨테이너 혀태로 실행하도록 구성하였습니다.

## DB 테이블 스키마

> ![image](https://github.com/user-attachments/assets/e16d114f-0677-4c6f-b910-363deafb54d4)
>
> + 각 테이블은 스프링의 envers 통해 **등록/수정/삭제 시 히스토리를 관리**하도록 했습니다(히스토리 테이블은 스키마에서 제외하였습니다.).

## SWAGGER UI

> [**SWAGGER UI LINK**](https://api.chobolevel.co.kr/swagger-ui/index.html)
>
> + 작성한 API들을 문서 형식으로 확인 가능하도록 작성해봤습니다.

## 모니터링 대시보드

> ![image](https://github.com/user-attachments/assets/8479489a-26fc-4ad6-b492-d16f79d0f3c3)
>
> [**GRAFANA LINK**](http://210.114.22.52:3000/d/dLsDQIUnzb/chobolevel-log-monitors?orgId=1&from=now-5m&to=now&timezone=browser&var-app_name=&var-log_keyword=&refresh=5s)
> 
> + actuator + prometheus + loki + grafana 조합을 통해 상태 및 로깅 모니터링 대시보드를 구성하였습니다.

## 초기 화면

> ![www chobolevel site_ (4)](https://github.com/user-attachments/assets/57851fd1-089b-49c8-b6cd-44bb6358e381)

## 주요 기능

> ### 인증 및 인가
> + **인증** (일반/소셜 로그인에 따라 로직을 수행하고 access_token, refresh_token 발급하여 프론트엔드 서버의 인증을 위해 사용하고 있습니다.)
    ![image](https://github.com/user-attachments/assets/48579312-1f37-4190-9c63-7246af36a664)
> + **인가** (인증이 완료된 요청의 인증 정보를 security context holder 보관하여 요청의 ROLE에 따라 인가처리 위해 사용하였습니다.)
    ![image](https://github.com/user-attachments/assets/ffd75317-2052-40c6-b02d-5e02d275aab4)

> ### 웜 업
> ![image](https://github.com/user-attachments/assets/04429651-0bb2-4141-8d20-6a2af7e0ecf4)
> ![image](https://github.com/user-attachments/assets/bbb76c66-e717-4f25-a279-f20a2df039de)
> ![image](https://github.com/user-attachments/assets/60c85522-8b15-45c7-8078-813bde0765b5)
> ![image](https://github.com/user-attachments/assets/2ab98ec5-19fe-47da-85dc-5ada974fea8a)
> + 애플리케이션 재시작했을 떄 JVM 특성상 최소한의 클래스만 로드해서 시작하는데 이로 인해 첫 API 호출 시 클래스 로드를 수행해야 하므로 지연이 발생하였습니다.
> + 이를 방지하기 위해 시작할 때 자주 사용되는 API 호출하여 클래스를 로드해두는 로직을 작성하였습니다.
> + Warmer 추상화를 통해 Warmer 인터페이스 구현한 클래스를 빈으로 등록하면 웜 업 로직에 포함되도록 하였습니다.
> + 사용자가 많지 않은 서비스의 경우 오래동안 사용되지 않은 클래스는 메모리에서 제거되기 때문에 지속적으로 웜 업하도록 스케줄러를 이용하는 로직도 추가하였습니다.

> ### Github Actions
> ![image](https://github.com/user-attachments/assets/13fc15a2-2d8a-4163-8799-373a6b6e392c)
> + main 브랜치의 푸시 이벤트에 트리거하여 Makefile에 정의된 커맨드를 통해 Jib로 도커 이미지로 빌드하고 Docker Hub Repo에 푸시까지 수행하도록 하였습니다.
> + 이후 서버에서도 Makefile에 정의된 커맨드를 통해 Docker Hub Repo에서 이미지를 받아 실행하여 코드 수정 후 재배포를 간단히 하였습니다.

> ### Amazon S3 Presigned
> ![image](https://github.com/user-attachments/assets/a0282646-d5d9-4a7b-8593-42a0413c97e9)
> + 파일을 업로드할 때 해당 파일을 서버에서 전달하고 서버에서 저장소로 저장하는 것은 비효율 적이며 성능 저하를 초해할 수 있습니다.(특히 대용량 파일의 경우 큰 성능 저하 초래 가능성)
> + 하지만 인가받은 사용자만 파일을 저장소에 저장해야 하기 때문에 서버를 받드시 거쳐야 하긴 했습니다.
> + 이러한 상황을 해소하기 위해 **S3 Presigned URL**을 사용하여 인가받은 사용자만 저장소에 파일을 올릴 수 있는 URL을 부여받고 서버에서는 파일에 접근할 수 있는 URL만 저장하여 파일 전송으로
    인한 성능 저하를 방지하였습니다.

> ### 로깅
> ![image](https://github.com/user-attachments/assets/0c38a2b5-a9d1-48f8-98e4-af72b4655203)
> ![image](https://github.com/user-attachments/assets/4be56dfa-3aa9-4cc3-bed9-c3f2175afa0e)
> + 로컬에서 발생하는 에러 로그 발생 시 바로 확인할 수 있지만 운영 서버에서 발생하는 에러 로그는 서버에 즉각적인 확인이 어려웠습니다.
> + 이를 위해 Webhook을 사용해 로그를 받을 수 있는 서비스(Discord, Slack 등)가 있다는 것을 알게되었고 Discord 채널에서 에러 로그를 받도록 설정하였습니다.

> ### 채팅 기능
> ![image](https://github.com/user-attachments/assets/11514145-c1b9-4c19-83ad-8b8fdd16f1d3)
> ![image](https://github.com/user-attachments/assets/82c57f0e-5701-46bf-92f1-97a2a26da2b2)
> ![image](https://github.com/user-attachments/assets/2e2a27b9-7a3c-4a99-80aa-69d1a43d5843)
> + Spring에서 제공되는 WebSocket(Stomp)을 통해 채팅 기능을 제공하였습니다.
> + 설정 파일을 통해 WebSocket 연결을 위한 엔트포인트와 메세지 구독/발행을 위한 엔드포인트를 설정해주었습니다.
> + 커스텀 인터셉터를 구성하여 WebSocket 연결, 메세지 구독/발행 요청을 인증하여 인증된 사용자만 접근할 수 있도록 하였습니다.
> + 메세지 발행 시 메세지를 저장하고 이후 구독중인 클라이언트에 메세지를 발송하는 로직을 통해 채팅 내용을 보관하고 즉시 확인할 수 있도록 하였습니다.
