// Jenkins: JDK 17, Maven, Appium (một server 4723 phục vụ nhiều session song song), adb + Xcode/idevice.
// HC BOX (nhiều máy USB): dùng -Phc-box → tách Allure: python3 scripts/split-allure-by-device.py
// Job nên trỏ nhánh daily (hoặc Multibranch). Cron 8:00 theo múi giờ master Jenkins.
// Trước bước test: agent Mac cần bật Appium (systemd/launchd hoặc stage riêng).

pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    parameters {
        string(name: 'GIT_BRANCH', defaultValue: 'daily', description: 'Tên nhánh (dùng khi job Pipeline checkout theo parameter; Multibranch bỏ qua)')
    }

    options {
        timestamps()
    }

    triggers {
        cron('0 8 * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --abbrev-ref HEAD && git log -1 --oneline'
            }
        }

        stage('Maven Test') {
            steps {
                sh 'mvn -B clean test -Phc-box'
                sh 'python3 scripts/split-allure-by-device.py || true'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'allure-results/**,allure-results-by-device/**,target/ios-audit/**', allowEmptyArchive: true
        }
    }
}
