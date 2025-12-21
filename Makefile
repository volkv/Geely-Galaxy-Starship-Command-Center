.PHONY: build setup deploy deploy-fresh release clean logs start stop uninstall help

PACKAGE_NAME := com.ggscc.app

help:
	@echo "GGSCC Build System"
	@echo ""
	@echo "  make setup         - Download AOSP signing keys"
	@echo "  make build         - Build release APK"
	@echo "  make deploy        - Quick update (preserves privileged mode)"
	@echo "  make deploy-fresh  - Full reinstall with reboot"
	@echo "  make release V=x.x - Create release package (e.g. make release V=1.0.0)"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make logs          - Show logcat for the app"
	@echo "  make start         - Launch the app"
	@echo "  make stop          - Force stop the app"
	@echo "  make uninstall     - Remove app from device"

setup:
	./setup-keys.sh

build: setup
	./gradlew assemblePrivilegedRelease

deploy:
	./deploy.sh

deploy-fresh:
	./deploy.sh --fresh

release:
ifndef V
	$(error VERSION not set. Usage: make release V=1.0.0)
endif
	./release.sh $(V)

clean:
	./gradlew clean

logs:
	adb logcat -v time | grep -E "$(PACKAGE_NAME)|CarModel|VehiclePropertyHelper|Controller"

start:
	adb shell am start -n $(PACKAGE_NAME)/.MainActivity

stop:
	adb shell am force-stop $(PACKAGE_NAME)

uninstall:
	adb root
	adb remount
	adb shell rm -rf /system/priv-app/$(PACKAGE_NAME)
	adb shell rm -f /system/etc/permissions/privapp-permissions-$(PACKAGE_NAME).xml
	@echo "Reboot required: adb reboot"
