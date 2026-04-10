pipeline {
    agent any

    triggers {
        cron('0 7 * * *')
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        USER_HOME = '/Users/quhuy'
        ANDROID_HOME = '/Users/quhuy/Library/Android/sdk'
        ANDROID_SDK_ROOT = '/Users/quhuy/Library/Android/sdk'

        MOBILE_EXTRA_PATH = '/Users/quhuy/.local/bin:/opt/homebrew/bin:/usr/local/bin:/Users/quhuy/Library/Android/sdk/platform-tools:/Users/quhuy/Library/Android/sdk/emulator:/Users/quhuy/Library/Android/sdk/cmdline-tools/latest/bin'
        NODE_BIN = '/Users/quhuy/.nvm/versions/node/v22.22.2/bin'
        FULL_PATH = '/Users/quhuy/.local/bin:/opt/homebrew/bin:/usr/local/bin:/Users/quhuy/Library/Android/sdk/platform-tools:/Users/quhuy/Library/Android/sdk/emulator:/Users/quhuy/Library/Android/sdk/cmdline-tools/latest/bin:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin'

        IDEVICE_ID_BIN = '/opt/homebrew/bin/idevice_id'
        APPIUM_BIN = '/Users/quhuy/.nvm/versions/node/v22.22.2/bin/appium'
        PYTHON_BIN = '/opt/homebrew/bin/python3'

        // Port strategy: mỗi device 1 slot, cách nhau 10
        APPIUM_BASE_PORT = '4700'
        IOS_WDA_BASE_PORT = '8100'
        IOS_MJPEG_BASE_PORT = '10100'
        ANDROID_SYSTEM_BASE_PORT = '8200'
        PORT_STEP = '10'
    }

    stages {
        stage('Checkout') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    deleteDir()
                    checkout scmGit(
                        branches: [[name: '*/main']],
                        extensions: [[$class: 'CloneOption', depth: 1, noTags: true, shallow: true]],
                        userRemoteConfigs: [[url: 'https://github.com/ngocllb/Yokara']]
                    )
                    stash name: 'repo-source', includes: '**/*', useDefaultExcludes: false
                }
            }
        }

        stage('Verify Environment') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    sh '''
                        set +e
                        echo "===== VERIFY ENV ====="
                        echo "HOME=$HOME"
                        echo "PATH=$PATH"
                        echo "ANDROID_HOME=$ANDROID_HOME"
                        echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"

                        echo "----- WHICH -----"
                        which sh || true
                        which python3 || true
                        which adb || true
                        which appium || true
                        which idevice_id || true
                        which tidevice || true
                        which curl || true
                        which lsof || true
                        which mvn || true
                        which node || true

                        echo "----- VERSIONS -----"
                        python3 --version || true
                        adb version || true
                        appium --version || true
                        mvn -v || true
                        node -v || true
                        tidevice version || true

                        echo "----- DEVICES -----"
                        adb devices || true
                        idevice_id -l || true
                        tidevice list || true
                    '''
                }
            }
        }

        stage('Detect Devices') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    script {
                        sh '''
                            rm -rf allure-merge device-dashboard
                            mkdir -p allure-merge device-dashboard
                        '''

                        def raw = sh(
                            script: "${env.PYTHON_BIN} scripts/get_jenkins_devices.py",
                            returnStdout: true
                        ).trim()

                        if (!raw) {
                            error("No physically connected devices found.")
                        }

                        def lines = raw.split('\n').findAll { it?.trim() && it.contains('|') }
                        if (lines.isEmpty()) {
                            error("Device script returned no valid records.")
                        }

                        def allDevices = []

                        for (String line : lines) {
                            def parts = line.split('\\|')
                            if (parts.size() < 2) {
                                continue
                            }

                            def platform = parts[0].trim().toLowerCase()
                            def udid = parts[1].trim()

                            if (platform == 'ios' || platform == 'android') {
                                allDevices << [platform: platform, udid: udid]
                            }
                        }

                        if (allDevices.isEmpty()) {
                            error("No supported devices detected.")
                        }

                        // sort an toàn cho Jenkins CPS
                        allDevices = allDevices.collect()
                        allDevices.sort { it.udid }

                        def appiumBase = env.APPIUM_BASE_PORT.toInteger()
                        def iosWdaBase = env.IOS_WDA_BASE_PORT.toInteger()
                        def iosMjpegBase = env.IOS_MJPEG_BASE_PORT.toInteger()
                        def androidSystemBase = env.ANDROID_SYSTEM_BASE_PORT.toInteger()
                        def step = env.PORT_STEP.toInteger()

                        def deviceMatrix = []

                        for (int idx = 0; idx < allDevices.size(); idx++) {
                            def dev = allDevices[idx]
                            def shortUdid = dev.udid.size() > 8 ? dev.udid.substring(dev.udid.size() - 8) : dev.udid
                            def slot = idx

                            def item = [
                                slot      : slot,
                                platform  : dev.platform,
                                udid      : dev.udid,
                                shortUdid : shortUdid,
                                appiumPort: appiumBase + (slot * step)
                            ]

                            if (dev.platform == 'ios') {
                                item.wdaLocalPort = iosWdaBase + (slot * step)
                                item.mjpegServerPort = iosMjpegBase + (slot * step)
                                item.derivedDataPath = "/tmp/wda-${shortUdid}-${env.BUILD_NUMBER}"
                            } else {
                                item.systemPort = androidSystemBase + (slot * step)
                            }

                            deviceMatrix << item
                        }

                        echo "===== DEVICE MATRIX ====="
                        for (def d in deviceMatrix) {
                            echo "${d}"
                        }

                        writeJSON file: 'device-dashboard/device-matrix.json', json: deviceMatrix, pretty: 4
                        stash name: 'device-matrix', includes: 'device-dashboard/device-matrix.json'
                    }
                }
            }
        }

        stage('Run Parallel Tests') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    script {
                        unstash 'device-matrix'
                        def deviceMatrix = readJSON file: 'device-dashboard/device-matrix.json'
                        def branches = [:]

                        for (int i = 0; i < deviceMatrix.size(); i++) {
                            def d = deviceMatrix[i]
                            def platform = d.platform
                            def udid = d.udid
                            def shortUdid = d.shortUdid
                            def slot = d.slot
                            def appiumPort = d.appiumPort as Integer
                            def appiumUrl = "http://127.0.0.1:${appiumPort}"
                            def branchName = "${platform}-${shortUdid}"
                            def branchWs = "${env.WORKSPACE}@${branchName}"
                            def mergeDir = "${env.WORKSPACE}/allure-merge/${branchName}"
                            def dashboardDir = "${env.WORKSPACE}/device-dashboard/${branchName}"

                            branches["${platform.toUpperCase()}-${shortUdid}"] = {
                                timeout(time: 30, unit: 'MINUTES') {
                                    retry(2) {
                                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                            ws(branchWs) {
                                                withEnv([
                                                    "HOME=${env.USER_HOME}",
                                                    "PATH=${env.FULL_PATH}",
                                                    "ANDROID_HOME=${env.ANDROID_HOME}",
                                                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                                                ]) {
                                                    deleteDir()
                                                    unstash 'repo-source'

                                                    def appiumPid = ''
                                                    def resultStatus = 'UNKNOWN'
                                                    def startTs = System.currentTimeMillis()

                                                    try {
                                                        sh """
                                                            set +e
                                                            mkdir -p target/allure-results-${udid}
                                                            mkdir -p artifacts
                                                            echo "===== BRANCH INFO ====="
                                                            echo "Platform=${platform}"
                                                            echo "UDID=${udid}"
                                                            echo "ShortUDID=${shortUdid}"
                                                            echo "Slot=${slot}"
                                                            echo "Workspace=\$(pwd)"
                                                            echo "AppiumUrl=${appiumUrl}"
                                                            echo "PATH=\$PATH"
                                                            echo "ANDROID_HOME=\$ANDROID_HOME"
                                                            echo "ANDROID_SDK_ROOT=\$ANDROID_SDK_ROOT"
                                                        """

                                                        if (platform == 'ios') {
                                                            def iosAlive = sh(
                                                                script: "${env.IDEVICE_ID_BIN} -l | grep '${udid}' || true",
                                                                returnStdout: true
                                                            ).trim()
                                                            if (!iosAlive) {
                                                                error("iOS device ${udid} is not connected.")
                                                            }

                                                            sh """
                                                                set +e
                                                                lsof -ti tcp:${appiumPort} | xargs kill -9 2>/dev/null || true
                                                                lsof -ti tcp:${d.wdaLocalPort} | xargs kill -9 2>/dev/null || true
                                                                lsof -ti tcp:${d.mjpegServerPort} | xargs kill -9 2>/dev/null || true
                                                                rm -rf "${d.derivedDataPath}" || true
                                                                mkdir -p "${d.derivedDataPath}"
                                                            """
                                                        } else {
                                                            def androidAlive = sh(
                                                                script: "adb -s '${udid}' get-state 2>/dev/null || true",
                                                                returnStdout: true
                                                            ).trim()
                                                            if (androidAlive != 'device') {
                                                                error("Android device ${udid} is not ready. State=${androidAlive}")
                                                            }

                                                            sh """
                                                                set +e
                                                                lsof -ti tcp:${appiumPort} | xargs kill -9 2>/dev/null || true
                                                                lsof -ti tcp:${d.systemPort} | xargs kill -9 2>/dev/null || true
                                                            """
                                                        }

                                                        sh """
                                                            set -e
                                                            echo "===== START APPIUM ====="
                                                            nohup env \\
                                                              HOME="${env.USER_HOME}" \\
                                                              PATH="${env.FULL_PATH}" \\
                                                              ANDROID_HOME="${env.ANDROID_HOME}" \\
                                                              ANDROID_SDK_ROOT="${env.ANDROID_SDK_ROOT}" \\
                                                              ${env.APPIUM_BIN} server \\
                                                              --address 127.0.0.1 \\
                                                              --port ${appiumPort} \\
                                                              --log-level info \\
                                                              > appium-${platform}-${shortUdid}.log 2>&1 &
                                                            echo \$! > appium-${platform}-${shortUdid}.pid
                                                        """

                                                        appiumPid = sh(
                                                            script: "cat appium-${platform}-${shortUdid}.pid",
                                                            returnStdout: true
                                                        ).trim()

                                                        sh """
                                                            set -e
                                                            echo "Waiting Appium status on ${appiumUrl}/status"
                                                            for i in \$(seq 1 40); do
                                                              if curl -sf ${appiumUrl}/status >/dev/null 2>&1; then
                                                                echo "Appium is ready on ${appiumUrl}"
                                                                exit 0
                                                              fi
                                                              sleep 1
                                                            done
                                                            echo "Appium did not become ready in time"
                                                            cat appium-${platform}-${shortUdid}.log || true
                                                            exit 1
                                                        """

                                                        if (platform == 'ios') {
                                                            sh """
                                                                set -e
                                                                ${env.IDEVICE_ID_BIN} -l || true

                                                                mvn clean test \\
                                                                  -DsuiteXmlFile=testng-jenkins.xml \\
                                                                  -Dplatform=ios \\
                                                                  -DappiumServer=${appiumUrl} \\
                                                                  -Dios.udid="${udid}" \\
                                                                  -Dios.wdaLocalPort.base=${d.wdaLocalPort} \\
                                                                  -Dios.mjpegServerPort.base=${d.mjpegServerPort} \\
                                                                  -Dios.derivedDataPath="${d.derivedDataPath}" \\
                                                                  -Djenkins.slot=${slot} \\
                                                                  -Djenkins.appiumPort=${appiumPort} \\
                                                                  -Djenkins.wdaLocalPort=${d.wdaLocalPort} \\
                                                                  -Djenkins.mjpegServerPort=${d.mjpegServerPort} \\
                                                                  -Dallure.results.directory=target/allure-results-${udid}
                                                            """
                                                        } else {
                                                            sh """
                                                                set -e
                                                                adb devices || true

                                                                mvn clean test \\
                                                                  -DsuiteXmlFile=testng-jenkins.xml \\
                                                                  -Dplatform=android \\
                                                                  -DappiumServer=${appiumUrl} \\
                                                                  -Dandroid.udid="${udid}" \\
                                                                  -Dandroid.systemPort.base=${d.systemPort} \\
                                                                  -Djenkins.slot=${slot} \\
                                                                  -Djenkins.appiumPort=${appiumPort} \\
                                                                  -Djenkins.systemPort=${d.systemPort} \\
                                                                  -Dallure.results.directory=target/allure-results-${udid}
                                                            """
                                                        }

                                                        resultStatus = 'PASSED'
                                                    } catch (err) {
                                                        resultStatus = 'FAILED'
                                                        throw err
                                                    } finally {
                                                        def durationSec = ((System.currentTimeMillis() - startTs) / 1000L) as Long

                                                        sh """
                                                            set +e

                                                            if [ -n "${appiumPid}" ]; then
                                                              kill ${appiumPid} || true
                                                            fi

                                                            if [ -f appium-${platform}-${shortUdid}.pid ]; then
                                                              kill \$(cat appium-${platform}-${shortUdid}.pid) || true
                                                            fi

                                                            lsof -ti tcp:${appiumPort} | xargs kill -9 2>/dev/null || true
                                                        """

                                                        if (platform == 'ios') {
                                                            sh """
                                                                set +e
                                                                lsof -ti tcp:${d.wdaLocalPort} | xargs kill -9 2>/dev/null || true
                                                                lsof -ti tcp:${d.mjpegServerPort} | xargs kill -9 2>/dev/null || true
                                                                rm -rf "${d.derivedDataPath}" || true
                                                            """
                                                        } else {
                                                            sh """
                                                                set +e
                                                                lsof -ti tcp:${d.systemPort} | xargs kill -9 2>/dev/null || true
                                                            """
                                                        }

                                                        sh """
                                                            set +e
                                                            mkdir -p target/allure-results-${udid}
                                                            {
                                                              echo "Platform=${platform}"
                                                              echo "DeviceUDID=${udid}"
                                                              echo "Slot=${slot}"
                                                              echo "AppiumServer=${appiumUrl}"
                                                              echo "AppiumPort=${appiumPort}"
                                                              echo "Result=${resultStatus}"
                                                              echo "DurationSec=${durationSec}"
                                                            } > target/allure-results-${udid}/environment.properties

                                                            mkdir -p "${mergeDir}"
                                                            if [ -d "target/allure-results-${udid}" ]; then
                                                              cp -R "target/allure-results-${udid}/." "${mergeDir}/" || true
                                                            fi

                                                            mkdir -p "${dashboardDir}"
                                                        """

                                                        writeJSON(
                                                            file: "${dashboardDir}/summary.json",
                                                            json: d + [status: resultStatus, durationSec: durationSec],
                                                            pretty: 4
                                                        )

                                                        sh """
                                                            set +e
                                                            cp -f appium-${platform}-${shortUdid}.log artifacts/ || true
                                                            cp -R target/surefire-reports artifacts/surefire-reports || true
                                                        """

                                                        archiveArtifacts artifacts: 'artifacts/**', allowEmptyArchive: true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        parallel branches + [failFast: false]
                    }
                }
            }
        }

        /*
         * Gộp kết quả Allure từ từng nhánh song song (allure-merge/<platform>-<udid>/) vào một thư mục phẳng.
         * Plugin Jenkins Allure thường không ăn glob allure-merge/* — cần một resultsDirectory duy nhất chứa mọi *-result.json.
         * pom.xml: allure.results.directory phải trỏ tới property ${allure.results.directory} để -D từ Maven có hiệu lực.
         */
        stage('Aggregate Allure results') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    sh '''
                        set +e
                        rm -rf allure-combined
                        mkdir -p allure-combined

                        if [ ! -d allure-merge ]; then
                          echo "[Allure] Không có thư mục allure-merge — bỏ qua gộp."
                          exit 0
                        fi

                        find allure-merge -type f 2>/dev/null | while IFS= read -r f; do
                          [ -f "$f" ] || continue
                          base=$(basename "$f")
                          dest="allure-combined/$base"
                          if [ -f "$dest" ]; then
                            parent=$(basename "$(dirname "$f")")
                            dest="allure-combined/${parent}__${base}"
                          fi
                          cp -f "$f" "$dest" || true
                        done

                        echo "[Allure] Đã gộp $(ls -1 allure-combined 2>/dev/null | wc -l | tr -d " ") file vào allure-combined."
                        ls -la allure-combined 2>/dev/null | head -80 || true
                    '''
                    archiveArtifacts artifacts: 'allure-merge/**', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'allure-combined/**', allowEmptyArchive: true
                }
            }
        }

        /*
         * Sinh HTML Allure riêng cho từng thư mục allure-merge/<platform>-<udid>/ (cùng cổng đã gán trong tên folder).
         * Bản _MERGED_ALL_DEVICES = cùng nội dung với report plugin Jenkins (allure-combined), tiện tải về / chia sẻ.
         */
        stage('Generate Allure HTML by device') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}"
                ]) {
                    sh '''
                        set +e
                        rm -rf allure-reports-by-device
                        mkdir -p allure-reports-by-device

                        run_allure_generate() {
                          _in="$1"
                          _out="$2"
                          if command -v allure >/dev/null 2>&1; then
                            allure generate "$_in" -o "$_out" --clean
                            return $?
                          fi
                          if command -v npx >/dev/null 2>&1; then
                            npx --yes allure-commandline@2.24.1 generate "$_in" -o "$_out" --clean
                            return $?
                          fi
                          echo "[Allure] Cần allure CLI (npm i -g allure-commandline) hoặc npx trên agent."
                          return 1
                        }

                        if [ -d allure-merge ]; then
                          for d in allure-merge/*/; do
                            [ -d "$d" ] || continue
                            name=$(basename "$d")
                            echo "[Allure] HTML riêng cho thư mục kết quả: $name"
                            run_allure_generate "$d" "allure-reports-by-device/$name" || true
                          done
                        fi

                        if [ -d allure-combined ] && [ -n "$(ls -A allure-combined 2>/dev/null)" ]; then
                          echo "[Allure] HTML MERGED (tất cả thiết bị)"
                          run_allure_generate allure-combined allure-reports-by-device/_MERGED_ALL_DEVICES || true
                        fi

                        ls -la allure-reports-by-device 2>/dev/null || true
                    '''
                    archiveArtifacts artifacts: 'allure-reports-by-device/**', allowEmptyArchive: true
                }
            }
        }

        stage('Build Dashboard Summary') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.FULL_PATH}",
                    "ANDROID_HOME=${env.ANDROID_HOME}",
                    "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
                ]) {
                    script {
                        sh '''
                            mkdir -p device-dashboard
                            echo "platform,udid,shortUdid,slot,appiumPort,extraPort1,extraPort2,status,durationSec" > device-dashboard/device-summary.csv
                        '''

                        def jsonFiles = sh(
                            script: "find device-dashboard -name 'summary.json' 2>/dev/null || true",
                            returnStdout: true
                        ).trim()

                        if (jsonFiles) {
                            for (String file in jsonFiles.split('\n').findAll { it?.trim() }) {
                                def item = readJSON file: file.trim()
                                def extraPort1 = item.platform == 'ios' ? item.wdaLocalPort : item.systemPort
                                def extraPort2 = item.platform == 'ios' ? item.mjpegServerPort : ''
                                sh """
                                    echo "${item.platform},${item.udid},${item.shortUdid},${item.slot},${item.appiumPort},${extraPort1},${extraPort2},${item.status},${item.durationSec}" >> device-dashboard/device-summary.csv
                                """
                            }
                        }

                        archiveArtifacts artifacts: 'device-dashboard/**', allowEmptyArchive: true
                    }
                }
            }
        }
    }

    post {
        always {
            withEnv([
                "HOME=${env.USER_HOME}",
                "PATH=${env.FULL_PATH}",
                "ANDROID_HOME=${env.ANDROID_HOME}",
                "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
            ]) {
                script {
                    sh '''
                        set +e

                        echo "===== GLOBAL CLEANUP PORTS ====="

                        for p in $(seq 4700 10 4900); do
                          lsof -ti tcp:$p | xargs kill -9 2>/dev/null || true
                        done

                        for p in $(seq 8100 10 8300); do
                          lsof -ti tcp:$p | xargs kill -9 2>/dev/null || true
                        done

                        for p in $(seq 10100 10 10300); do
                          lsof -ti tcp:$p | xargs kill -9 2>/dev/null || true
                        done

                        for p in $(seq 8200 10 8400); do
                          lsof -ti tcp:$p | xargs kill -9 2>/dev/null || true
                        done

                        pkill -f appium || true
                        pkill -f WebDriverAgent || true

                        mkdir -p allure-merge allure-combined
                        find allure-combined -type f 2>/dev/null | head -30 || true
                        find allure-merge -type f 2>/dev/null | head -20 || true
                    '''

                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'allure-combined']]
                    ])
                }
            }
        }
    }
}
