REGISTRY := stocat
TAG := 0.0.1
KIND_CLUSTER := stocat-local
OTEL_AGENT_VERSION := 2.8.0

SERVICES := auth-api

# ANSI 색상 코드 (TUI/터미널 가독성용)
BLUE   = \033[1;34m
GREEN  = \033[1;32m
YELLOW = \033[1;33m
RED    = \033[1;31m
RESET  = \033[0m

.PHONY: help all infra-up infra-wait config-all auth-api log-auth-api stop-auth-api stop-all ps

help:
	@echo "$(BLUE)Stocat Auth 서비스 관리 도구$(RESET)"
	@echo "사용 가능한 명령:"
	@echo "  $(GREEN)infra-up$(RESET)        인프라(MySQL, Redis 등) 실행"
	@echo "  $(GREEN)infra-wait$(RESET)      인프라 준비 대기"
	@echo "  $(GREEN)config-all$(RESET)      시트콤/라우트 설정을 Consul에 등록 (필요시)"
	@echo "  $(GREEN)auth-api$(RESET)        백그라운드에서 Auth 서비스 실행"
	@echo "  $(GREEN)log-auth-api$(RESET)    Auth 서비스 실시간 로그 보기"
	@echo "  $(RED)stop-auth-api$(RESET)   Auth 서비스 종료"
	@echo "  $(RED)stop-all$(RESET)       모든 백그라운드 서비스 종료"
	@echo "  $(YELLOW)all$(RESET)             인프라 + 설정 + 서비스를 한 번에 실행 (All-in-One)"
	@echo "  $(YELLOW)ps$(RESET)              컨테이너 상태 확인"
	@echo ""
	@echo "빌드/배포 관련:"
	@echo "  boot-all, docker-all, push-all, kind-load-all"

# --- 1. Infrastructure (인프라 계층) ---
infra-up:
	@echo "$(BLUE)[infra] 인프라를 기동합니다...$(RESET)"
	docker-compose up -d

infra-wait:
	@echo "$(BLUE)[infra] 인프라 준비 상태를 확인합니다...$(RESET)"
	@sleep 5
	@echo "$(GREEN)[infra] 인프라 준비 완료!$(RESET)"

config-all:
	@echo "$(YELLOW)[config] 별도의 설정 등록이 필요하지 않습니다.$(RESET)"

# --- 2. Applications (애플리케이션 계층) ---
auth-api:
	@echo "$(BLUE)[app] auth-api 를 백그라운드에서 실행합니다...$(RESET)"
	@mkdir -p logs
	@nohup ./gradlew :auth-api:bootRun > logs/auth-api.log 2>&1 &
	@echo "$(GREEN)[app] auth-api 실행 명령 전송 완료$(RESET)"
	@echo "$(YELLOW)로그 확인: make log-auth-api$(RESET)"

log-auth-api:
	@echo "$(BLUE)[logs] auth-api 실시간 로그를 출력합니다... (종료: Ctrl+C)$(RESET)"
	@if [ -f logs/auth-api.log ]; then \
		tail -f logs/auth-api.log; \
	else \
		echo "$(RED)[error] 로그 파일이 없습니다. 서비스가 실행 중인지 확인하세요.$(RESET)"; \
	fi

stop-auth-api:
	@echo "$(RED)[stop] auth-api 서비스를 종료합니다...$(RESET)"
	-@pkill -f "auth-api.*bootRun" || echo "이미 종료되었거나 실행 중이지 않습니다."

stop-all:
	@echo "$(RED)[stop] 모든 백그라운드 Spring Boot 서비스를 종료합니다...$(RESET)"
	-@pkill -f "bootRun" || echo "실행 중인 프로세스가 없습니다."

# --- 3. Orchestration (복합 실행) ---
all: infra-up infra-wait config-all auth-api

ps:
	@docker-compose ps

# ==========================================
# Original Build & Deploy Targets (Preserved)
# ==========================================
.PHONY: $(addprefix boot-,$(SERVICES)) $(addprefix docker-,$(SERVICES)) $(addprefix push-,$(SERVICES)) $(addprefix kind-load-,$(SERVICES)) $(addprefix helm-local-,$(SERVICES))

$(addprefix boot-,$(SERVICES)): boot-%:
	$(MAKE) -C $* boot REGISTRY=$(REGISTRY) TAG=$(TAG) KIND_CLUSTER=$(KIND_CLUSTER) OTEL_AGENT_VERSION=$(OTEL_AGENT_VERSION)

$(addprefix docker-,$(SERVICES)): docker-%:
	$(MAKE) -C $* docker REGISTRY=$(REGISTRY) TAG=$(TAG) KIND_CLUSTER=$(KIND_CLUSTER) OTEL_AGENT_VERSION=$(OTEL_AGENT_VERSION)

$(addprefix kind-load-,$(SERVICES)): kind-load-%:
	$(MAKE) -C $*/localhost kind-load REGISTRY=$(REGISTRY) TAG=$(TAG) KIND_CLUSTER=$(KIND_CLUSTER) OTEL_AGENT_VERSION=$(OTEL_AGENT_VERSION)

$(addprefix helm-local-,$(SERVICES)): helm-local-%:
	$(MAKE) -C $*/localhost helm-deploy REGISTRY=$(REGISTRY) TAG=$(TAG) KIND_CLUSTER=$(KIND_CLUSTER) OTEL_AGENT_VERSION=$(OTEL_AGENT_VERSION)

$(addprefix push-,$(SERVICES)): push-%:
	$(MAKE) -C $* push REGISTRY=$(REGISTRY) TAG=$(TAG) KIND_CLUSTER=$(KIND_CLUSTER) OTEL_AGENT_VERSION=$(OTEL_AGENT_VERSION)

.PHONY: boot-all docker-all push-all kind-load-all helm-local-all
boot-all: $(addprefix boot-,$(SERVICES))
docker-all: $(addprefix docker-,$(SERVICES))
push-all: $(addprefix push-,$(SERVICES))
kind-load-all: $(addprefix kind-load-,$(SERVICES))
helm-local-all: $(addprefix helm-local-,$(SERVICES))
