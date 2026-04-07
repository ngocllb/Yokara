pipeline {
    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
    }

    environment {
        REPO_URL = 'https://github.com/ngocllb/Yokara.git'
        REPO_BRANCH = 'main'
        COMMON_PATH = '/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${REPO_BRANCH}", url: "${REPO_URL}"
                stash name: 'source', includes: '**/*', useDefaultExcludes: false
            }
        }

        stage('Verify Tools') {
            steps {
                sh '''
                    set -e
                    export PATH="${COMMON_PATH}:$PATH"

                    command -v python3 >/dev/null 2>&1 || { echo "Thiếu python3"; exit 127; }
                    command -v node >/dev/null 2>&1 || { echo "Thiếu node"; exit 127; }
                    command -v mvn >/dev/null 2>&1 || { echo "Thiếu maven (mvn)"; exit 127; }

                    if command -v appium >/dev/null 2>&1; then
                      APPIUM_CMD="appium"
                    elif command -v npx >/dev/null 2>&1; then
                      APPIUM_CMD="npx appium"
                    else
                      echo "Thiếu appium (hoặc npx appium)"
                      exit 127
                    fi

                    python3 --version
                    node -v
                    mvn -v
                    sh -c "$APPIUM_CMD --version"
                '''
            }
        }

        stage('Detect Devices') {
            steps {
                script {
                    def devicesRaw = sh(
                        script: '''
                            set -e
                            export PATH="${COMMON_PATH}:$PATH"
                            python3 scripts/get_jenkins_devices.py
                        ''',
                        returnStdout: true
                    ).trim()

                    if (!devicesRaw) {
                        error "Không phát hiện thiết bị Android/iOS đang online."
                    }

                    env.DETECTED_DEVICES = devicesRaw
                    echo "Detected devices:\\n${env.DETECTED_DEVICES}"
                }
            }
        }

        stage('Run Parallel Tests') {
            steps {
                script {
                    sh 'rm -rf allure-merge && mkdir -p allure-merge'

                    def devices = env.DETECTED_DEVICES.split("\\n")
                    def branches = [:]

                    devices.eachWithIndex { line, idx ->
                        def clean = line?.trim()
                        if (!clean || !clean.contains('|')) {
                            return
                        }

                        def parts = clean.split("\\|")
                        if (parts.length < 2) {
                            return
                        }

                        def platform = parts[0].trim().toLowerCase()
                        def udid = parts[1].trim()
                        def shortUdid = udid.size() > 8 ? udid.substring(udid.size() - 8) : udid

                        branches["${platform.toUpperCase()}-${shortUdid}"] = {
                            ws("${env.WORKSPACE}@${platform}-${shortUdid}") {
                                deleteDir()
                                unstash 'source'

                                def allureDir = "target/allure-results-${udid}"
                                def appiumPort = platform == 'android' ? 4723 + idx : 4823 + idx
                                def systemPortBase = 8300 + (idx * 10)
                                def wdaLocalPortBase = 8200 + (idx * 10)
                                def mjpegServerPortBase = 10200 + (idx * 10)
                                def pidFile = "appium-${platform}-${shortUdid}.pid"
                                def logFile = "appium-${platform}-${shortUdid}.log"

                                def extraArgs = platform == 'android'
                                    ? "-Dandroid.udid=${udid} -Dandroid.systemPort.base=${systemPortBase}"
                                    : "-Dios.udid=${udid} -Dios.wdaLocalPort.base=${wdaLocalPortBase} -Dios.mjpegServerPort.base=${mjpegServerPortBase}"

                                try {
                                    sh """
                                        set -e
                                        export PATH="${COMMON_PATH}:\$PATH"
                                        mkdir -p ${allureDir}
                                        rm -rf target/surefire-reports

                                        if command -v appium >/dev/null 2>&1; then
                                          APPIUM_CMD="appium"
                                        else
                                          APPIUM_CMD="npx appium"
                                        fi

                                        lsof -ti tcp:${appiumPort} | xargs kill -9 || true
                                        nohup sh -c "\$APPIUM_CMD --address 127.0.0.1 --port ${appiumPort}" > ${logFile} 2>&1 &
                                        echo \$! > ${pidFile}
                                    """

                                    sh """
                                        set -e
                                        for i in \$(seq 1 45); do
                                          curl -fsS "http://127.0.0.1:${appiumPort}/status" >/dev/null && exit 0
                                          sleep 1
                                        done
                                        echo "Appium không start được ở port ${appiumPort}"
                                        exit 1
                                    """

                                    sh """
                                        set -e
                                        export PATH="${COMMON_PATH}:\$PATH"
                                        mvn clean test \
                                          -DsuiteXmlFile=testng-jenkins.xml \
                                          -Dplatform=${platform} \
                                          -DappiumServer=http://127.0.0.1:${appiumPort} \
                                          ${extraArgs} \
                                          -Dallure.results.directory=${allureDir}
                                    """
                                } finally {
                                    sh """
                                        set +e
                                        if [ -f ${pidFile} ]; then
                                          kill \$(cat ${pidFile}) || true
                                        fi

                                        mkdir -p ${env.WORKSPACE}/allure-merge
                                        cp -R ${allureDir}/. ${env.WORKSPACE}/allure-merge/ || true

                                        mkdir -p artifacts
                                        cp -f ${logFile} artifacts/ || true
                                        cp -R target/surefire-reports artifacts/surefire-reports || true
                                    """

                                    archiveArtifacts artifacts: 'artifacts/**', allowEmptyArchive: true
                                    archiveArtifacts artifacts: "${allureDir}/**", allowEmptyArchive: true
                                }
                            }
                        }
                    }

                    if (branches.isEmpty()) {
                        error "Không tạo được branch test nào từ danh sách thiết bị."
                    }

                    parallel branches
                }
            }
        }

        stage('Publish Allure Report') {
            steps {
                allure includeProperties: false,
                       jdk: '',
                       results: [[path: 'allure-merge']]
            }
        }
    }
}
