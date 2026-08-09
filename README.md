# LEO PHOTO

사진과 이야기를 게시글로 공유하는 커뮤니티 서비스입니다.
이 저장소는 LEO PHOTO의 Spring Boot 백엔드 애플리케이션을 관리합니다.

- 서비스: [LEO PHOTO 바로가기](https://leo-community.xyz)
- 시연 영상: [Google Drive](https://drive.google.com/file/d/1HAiR3l3Hz_b8c-B4qi4QNPXCV0CGdwpV/view?usp=sharing)
- 프론트엔드: [4-leo-community-FE](https://github.com/100-hours-a-week/4-leo-community-FE)
- 인프라: [community-infra](https://github.com/lee-y-ch/community-infra)

## 주요 기능

### 회원

- 이메일과 비밀번호를 이용한 회원가입 및 로그인
- 이메일·닉네임 중복 확인
- 회원정보와 비밀번호 수정
- 프로필 이미지 등록 및 변경
- 회원 탈퇴

### 게시글

- 게시글 작성, 조회, 수정, 삭제
- 커서 기반 게시글 목록 조회
- 최신순·정확도순 검색
- 게시글 좋아요
- 게시글 조회수 집계

### 댓글

- 게시글별 댓글 조회 및 작성
- 본인이 작성한 댓글 수정 및 삭제

### 이미지

- S3 Presigned URL 발급
- 브라우저에서 S3로 원본 이미지 직접 업로드
- Lambda를 통한 WebP 변환
- CloudFront 이미지 제공

## 아키텍처

![LEO PHOTO architecture](<docs/architecture/leo.lee(이용찬)_클라우드_아키텍처.png>)

- Route 53과 Application Load Balancer를 통해 프론트엔드와 백엔드 요청을 분기합니다.
- Kubernetes 클러스터는 Control Plane 1대와 Worker Node 2대로 구성했습니다.
- MySQL과 Valkey는 Private Data Subnet에서 운영합니다.
- 애플리케이션은 Helm과 Argo CD를 이용해 배포합니다.
- 이미지 업로드와 조회는 애플리케이션 트래픽에서 분리했습니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5, Spring MVC, Spring Data JPA |
| Database | MySQL 8, Amazon RDS |
| Query | QueryDSL, JDBC, MySQL FULLTEXT |
| Cache | Amazon ElastiCache Serverless for Valkey |
| Authentication | JWT, BCrypt |
| Image | Amazon S3, SQS, Lambda, CloudFront |
| Deployment | Docker, Kubernetes, Helm, Argo CD |
| Monitoring | Prometheus, Loki, Grafana, CloudWatch |
| Test | JUnit 5, Mockito, k6 |

## 프로젝트 구조

<details>
<summary>폴더 구조 보기</summary>

```text
src/main/java/com/community/community
├── auth          # JWT 처리와 로그인 사용자 확인
├── config        # QueryDSL, MVC, Valkey 설정
├── controller    # REST API
├── dto           # 요청 및 응답 객체
├── entity        # JPA 엔티티
├── exception     # 예외 및 공통 오류 응답
├── repository    # JPA, QueryDSL, JDBC 데이터 접근
└── service       # 회원, 게시글, 댓글, 이미지 도메인 로직
```

</details>

## API

<details>
<summary>API 목록 보기</summary>

### 인증

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/auth` | 로그인 |
| `DELETE` | `/auth` | 로그아웃 |
| `GET` | `/auth/check` | 로그인 상태 확인 |

### 회원

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/users` | 회원가입 |
| `GET` | `/users/{userId}` | 회원정보 조회 |
| `PATCH` | `/users/{userId}` | 회원정보 수정 |
| `PATCH` | `/users/{userId}/password` | 비밀번호 변경 |
| `DELETE` | `/users/{userId}` | 회원 탈퇴 |
| `GET` | `/users/email/check` | 이메일 중복 확인 |
| `GET` | `/users/nickname/check` | 닉네임 중복 확인 |

### 게시글과 댓글

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/posts` | 게시글 목록 조회 |
| `GET` | `/posts/search` | 게시글 검색 |
| `POST` | `/posts` | 게시글 작성 |
| `GET` | `/posts/{postId}` | 게시글 상세 조회 |
| `PATCH` | `/posts/{postId}` | 게시글 수정 |
| `DELETE` | `/posts/{postId}` | 게시글 삭제 |
| `POST` | `/posts/{postId}/like` | 좋아요 처리 |
| `GET` | `/posts/{postId}/comments` | 댓글 목록 조회 |
| `POST` | `/posts/{postId}/comments` | 댓글 작성 |
| `PUT` | `/comments/{commentId}` | 댓글 수정 |
| `DELETE` | `/comments/{commentId}` | 댓글 삭제 |

### 이미지와 상태 확인

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/images/presigned-url` | 게시글 이미지 업로드 URL 발급 |
| `POST` | `/images/presigned-url/signup-profile` | 회원가입 프로필 업로드 URL 발급 |
| `POST` | `/images/status` | 게시글 이미지 처리 상태 확인 |
| `POST` | `/images/status/signup-profile` | 프로필 이미지 처리 상태 확인 |
| `GET` | `/health/live` | 애플리케이션 동작 확인 |
| `GET` | `/health/ready` | 요청 처리 준비 상태 확인 |

</details>

## 로컬 실행

### 요구 사항

- JDK 17
- MySQL 8
- AWS 자격 증명과 S3 Bucket
- Valkey 또는 Redis Cluster 호환 환경 *(조회수 buffer 사용 시)*

### 환경변수

```bash
export DB_URL='jdbc:mysql://localhost:3306/community'
export DB_USERNAME='community'
export DB_PASSWORD='your-db-password'
export JWT_SECRET='at-least-32-byte-secret-value'

export AWS_REGION='ap-northeast-2'
export S3_ORIGINAL_BUCKET='your-original-bucket'
export S3_PROCESSED_BUCKET='your-processed-bucket'
export IMAGE_BASE_URL='https://your-image-domain.example'

# Valkey 조회수 buffer를 사용하지 않을 때
export VIEW_COUNT_BUFFER_ENABLED='false'
```

```bash
./gradlew bootRun
```

## 테스트 및 빌드

```bash
./gradlew clean test
./gradlew bootJar
```

## 배포

백엔드는 [Dockerfile](Dockerfile)로 컨테이너 이미지를 만들고 Kubernetes에 배포합니다. Helm Chart, Argo CD Application과 AWS 리소스 설정은 [community-infra](https://github.com/lee-y-ch/community-infra) 저장소에서 관리합니다.

DB 접속 정보, JWT Secret과 AWS 설정은 배포 환경에서 Secret과 환경변수로 주입합니다.
