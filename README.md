# 개인 블로그 프로젝트

## 배포 환경

> ![image](https://github.com/chobolevel/ikea/assets/104749958/dc31569c-bcca-4797-9fc4-9e17bccec390)

## [프로젝트 서비스 링크](https://chobolevel.site)

> + 카페24 호스팅 서비스에 도커를 이용하여 배포하였습니다.
> + 프론트엔드 프로젝트는 VERCEL 서비스를 이용하였습니다.
> + Jar, Nginx, Redis, Certbot 등 총 4개의 도커 컨테이너가 실행 중인 상태입니다.
> + DB는 AWS의 RDS 서비스를 사용하였습니다.

## 프로젝트를 진행한 이유

> 여러 블로그를 참고하면서 능력있는 개발자들의 블로그는 보통 직접 개발한 경우가 많다고 보아서 따라해보게 되었습니다.\n
> 이전까지 배운 내용들을 활용해보고 중간중간 새로운 부분도 익히기 위해 진행하였습니다.

## 백엔드 프로젝트 사용기술

> + Kotlin
>+ SpringBoot 3.1.0
>+ spring-boot-data-jpa, query dsl
>+ spring-data-envers
>+ MySQL 8.0(AWS RDS)
>+ Redis
>+ Nginx, Certbot

## CI/CD
> github action, Jib 라이브러리 그리고 Makefile 커맨드 등을 이용해 브랜치의 푸시 이벤트를 감지하여 도커 허브 레포에 도커 이미지를 빌드하여 푸시하도록 하였습니다.\n
> 이후 해당 이미지를 서버에서 다운로드 받아서 도커 컨테이너로 실행하도록 구성하였습니다.

## DB 테이블 스키마

> ![image](https://github.com/user-attachments/assets/77952455-e154-4b8a-b52f-6e1945ecabdc)
>
> 각 테이블은 스프링의 envers 통해 수정/삭제 시 히스토리를 관리하도록 했습니다.

## SWAGGER UI

> [SWAGGER UI LINK](https://api.chobolevel.site/swagger-ui/index.html)
>
> 작성한 API들을 문서 형식으로 확인 가능하도록 작성해봤습니다.

## 초기 화면

> ![www chobolevel site_ (1)](https://github.com/user-attachments/assets/a1183088-a089-4834-b685-458a6c8c34cf)

