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
        timeout(time: 120, unit: 'MINUTES')
    }

    environment {
        USER_HOME = '/Users/quhuy'
        ANDROID_HOME = '/Users/quhuy/Library/Android/sdk'
        ANDROID_SDK_ROOT = '/Users/quhuy/Library/Android/sdk'

        NODE_BIN = '/Users/quhuy/.nvm/versions/node/v22.22.2/bin'
        PYTHON_BIN = '/opt/homebrew/bin/python3'
        IDEVICE_ID_BIN = '/opt/homebrew/bin/idevice_id'
        ALLURE_BIN = '/opt/homebrew/bin/allure'
        APPIUM_BIN = '/Users/quhuy/.nvm/versions/node/v22.22.2/bin/appium'

        FULL_PATH = '/Users/quhuy/.local/bin:/opt/homebrew/bin:/usr/local/bin:/Users/quhuy/Library/Android/sdk/platform-tools:/Users/quhuy/Library/Android/sdk/emulator:/Users/quhuy/Library/Android/sdk/cmdline-tools/latest/bin:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin'

        APPIUM_BASE_PORT = '4700'
        IOS_WDA_BASE_PORT = '8100'
        IOS_MJPEG_BASE_PORT = '10100'
        ANDROID_SYSTEM_BASE_PORT = '8200'
        PORT_STEP = '10'
    }

    stages {
        stage('Checkout') {
            steps {
                withEnv(commonEnv()) {
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
                withEnv(commonEnv()) {
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
                        which allure || true

                        echo "----- VERSIONS -----"
                        python3 --version || true
                        adb version || true
                        appium --version || true
                        mvn -v || true
                        node -v || true
                        allure --version || true
                        tidevice version || true

                        echo "----- CONNECTED DEVICES -----"
                        adb devices || true
                        idevice_id -l || true
                        tidevice list || true
                    '''
                }
            }
        }

        stage('Detect Devices') {
            steps {
                withEnv(commonEnv()) {
                    script {
                        sh '''
                            rm -rf allure-merge allure-combined reports device-dashboard run-meta artifacts
                            mkdir -p allure-merge allure-combined reports/device reports/combined device-dashboard run-meta artifacts
                        '''

                        def raw = sh(
                            script: "${env.PYTHON_BIN} scripts/get_jenkins_devices.py",
                            returnStdout: true
                        ).trim()

                        echo "===== RAW DEVICE OUTPUT ====="
                        echo(raw ?: "(empty)")

                        if (!raw) {
                            error("No physically connected devices found from get_jenkins_devices.py")
                        }

                        def lines = raw.split('\n').findAll { it?.trim() && it.contains('|') }
                        if (lines.isEmpty()) {
                            error("Device script returned no valid records. Expected lines like: ios|UDID or android|UDID")
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
                            error("No supported devices detected after parsing script output.")
                        }

                        // tránh CPS sort closure warning
                        allDevices = allDevices.sort(false) { a, b -> a.udid <=> b.udid }

                        def appiumBase = env.APPIUM_BASE_PORT.toInteger()
                        def iosWdaBase = env.IOS_WDA_BASE_PORT.toInteger()
                        def iosMjpegBase = env.IOS_MJPEG_BASE_PORT.toInteger()
                        def androidSystemBase = env.ANDROID_SYSTEM_BASE_PORT.toInteger()
                        def step = env.PORT_STEP.toInteger()

                        def deviceMatrix = []

                        for (int idx = 0; idx < allDevices.size(); idx++) {
                            def dev = allDevices[idx]
                            def shortUdid = dev.udid.size() > 8 ? dev.udid.substring(dev.udid.size() - 8) : dev.udid
                            def branchName = "${dev.platform}-${shortUdid}"

                            def item = [
                                slot       : idx,
                                platform   : dev.platform,
                                udid       : dev.udid,
                                shortUdid  : shortUdid,
                                branchName : branchName,
                                appiumPort : appiumBase + (idx * step)
                            ]

                            if (dev.platform == 'ios') {
                                item.wdaLocalPort = iosWdaBase + (idx * step)
                                item.mjpegServerPort = iosMjpegBase + (idx * step)
                                item.derivedDataPath = "/tmp/wda-${shortUdid}-${env.BUILD_NUMBER}"
                            } else {
                                item.systemPort = androidSystemBase + (idx * step)
                            }

                            deviceMatrix << item
                        }

                        echo "===== DEVICE MATRIX ====="
                        echo "TOTAL DEVICES = ${deviceMatrix.size()}"
                        deviceMatrix.each { item ->
                            echo "${item}"
                        }

                        writeJSON file: 'run-meta/device-matrix.json', json: deviceMatrix, pretty: 4
                        writeFile file: 'run-meta/device-count.txt', text: "${deviceMatrix.size()}\n"

                        stash name: 'device-matrix', includes: 'run-meta/device-matrix.json,run-meta/device-count.txt'
                        archiveArtifacts artifacts: 'run-meta/**', allowEmptyArchive: true
                    }
                }
            }
        }

        stage('Run Parallel Tests') {
            steps {
                withEnv(commonEnv()) {
                    script {
                        unstash 'device-matrix'
                        def deviceMatrix = readJSON file: 'run-meta/device-matrix.json'
                        def branches = [:]

                        for (int i = 0; i < deviceMatrix.size(); i++) {
                            def d = deviceMatrix[i]
                            branches["${d.platform.toUpperCase()}-${d.shortUdid}"] = {
                                timeout(time: 45, unit: 'MINUTES') {
                                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                        runDeviceBranch(d)
                                    }
                                }
                            }
                        }

                        parallel branches + [failFast: false]
                    }
                }
            }
        }

        stage('Archive Branch Artifacts') {
            steps {
                archiveArtifacts artifacts: 'artifacts/**', allowEmptyArchive: true
            }
        }

        stage('Generate Per-device Reports') {
            steps {
                withEnv(commonEnv()) {
                    script {
                        unstash 'device-matrix'
                        def deviceMatrix = readJSON file: 'run-meta/device-matrix.json'

                        deviceMatrix.each { d ->
                            def branchName = d.branchName
                            sh """
                                set +e
                                mkdir -p "reports/device/${branchName}"

                                if [ -d "allure-merge/${branchName}" ] && [ "\$(find "allure-merge/${branchName}" -type f | wc -l | tr -d ' ')" != "0" ]; then
                                  ${env.ALLURE_BIN} generate "allure-merge/${branchName}" --clean -o "reports/device/${branchName}" || true
                                else
                                  cat > "reports/device/${branchName}/index.html" <<EOF
<!doctype html>
<html>
<head><meta charset="utf-8"><title>${branchName}</title></head>
<body>
<h2>No Allure results for ${branchName}</h2>
<p>This device branch may have failed before any test result files were created.</p>
</body>
</html>
EOF
                                fi
                            """
                        }

                        archiveArtifacts artifacts: 'reports/device/**', allowEmptyArchive: true
                    }
                }
            }
        }

        stage('Aggregate Allure results') {
            steps {
                withEnv(commonEnv()) {
                    sh '''
                        set +e
                        rm -rf allure-combined
                        mkdir -p allure-combined

                        if [ ! -d allure-merge ]; then
                          echo "[Allure] allure-merge does not exist"
                          exit 0
                        fi

                        # Copy mọi file trừ environment.properties để không bị iOS chiếm metadata combined
                        find allure-merge -type f ! -name 'environment.properties' 2>/dev/null | while IFS= read -r f; do
                          [ -f "$f" ] || continue
                          base=$(basename "$f")
                          parent=$(basename "$(dirname "$f")")
                          dest="allure-combined/$base"

                          if [ -f "$dest" ]; then
                            dest="allure-combined/${parent}__${base}"
                          fi

                          cp -f "$f" "$dest" || true
                        done

                        # Tạo metadata combined riêng
                        total_devices=$(find allure-merge -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
                        cat > allure-combined/environment.properties <<EOF
ReportScope=combined
TotalDeviceBranches=${total_devices}
GeneratedBy=Jenkins
EOF

                        echo "[Allure] Combined file count: $(find allure-combined -type f 2>/dev/null | wc -l | tr -d ' ')"
                        ls -la allure-combined | head -80 || true
                    '''

                    archiveArtifacts artifacts: 'allure-merge/**', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'allure-combined/**', allowEmptyArchive: true
                }
            }
        }

        stage('Generate Combined Report') {
            steps {
                withEnv(commonEnv()) {
                    sh """
                        set +e
                        rm -rf reports/combined
                        mkdir -p reports/combined

                        if [ -d "allure-combined" ] && [ "\$(find allure-combined -type f | wc -l | tr -d ' ')" != "0" ]; then
                          ${env.ALLURE_BIN} generate allure-combined --clean -o reports/combined || true
                        else
                          cat > reports/combined/index.html <<EOF
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Combined report</title></head>
<body>
<h2>No combined Allure results</h2>
</body>
</html>
EOF
                        fi
                    """
                    archiveArtifacts artifacts: 'reports/combined/**', allowEmptyArchive: true
                }
            }
        }

        stage('Build Dashboard Summary') {
            steps {
                withEnv(commonEnv()) {
                    script {
                        def rows = []
                        def jsonFiles = sh(
                            script: "find device-dashboard -name summary.json 2>/dev/null || true",
                            returnStdout: true
                        ).trim()

                        sh '''
                            mkdir -p device-dashboard
                            echo "platform,udid,shortUdid,slot,branchName,appiumPort,extraPort1,extraPort2,status,durationSec,sessionCreated,testExecuted" > device-dashboard/device-summary.csv
                        '''

                        if (jsonFiles) {
                            jsonFiles.split('\n').findAll { it?.trim() }.each { file ->
                                def item = readJSON file: file.trim()
                                rows << item

                                def extraPort1 = item.platform == 'ios' ? item.wdaLocalPort : item.systemPort
                                def extraPort2 = item.platform == 'ios' ? item.mjpegServerPort : ''
                                def sessionCreated = item.sessionCreated != null ? item.sessionCreated.toString() : ''
                                def testExecuted = item.testExecuted != null ? item.testExecuted.toString() : ''

                                sh """
                                    echo "${item.platform},${item.udid},${item.shortUdid},${item.slot},${item.branchName},${item.appiumPort},${extraPort1},${extraPort2},${item.status},${item.durationSec},${sessionCreated},${testExecuted}" >> device-dashboard/device-summary.csv
                                """
                            }
                        }

                        rows = rows.sort(false) { a, b -> a.branchName <=> b.branchName }

                        def html = ""
                        html += "<!doctype html><html><head><meta charset='utf-8'>"
                        html += "<title>Device Dashboard</title>"
                        html += "<style>"
                        html += "body{font-family:Arial,sans-serif;margin:24px;background:#fafafa;color:#222;}"
                        html += "table{border-collapse:collapse;width:100%;margin-top:16px;background:#fff;}"
                        html += "th,td{border:1px solid #ddd;padding:10px;text-align:left;font-size:14px;vertical-align:top;}"
                        html += "th{background:#f1f3f4;}"
                        html += ".ok{color:#137333;font-weight:bold;}"
                        html += ".bad{color:#d93025;font-weight:bold;}"
                        html += ".warn{color:#b26a00;font-weight:bold;}"
                        html += "a{color:#0b57d0;text-decoration:none;}"
                        html += "code{background:#f6f8fa;padding:2px 4px;border-radius:4px;}"
                        html += "</style></head><body>"
                        html += "<h1>Jenkins Device Dashboard</h1>"
                        html += "<p><a href='../reports/combined/index.html'>Open Combined Allure Report</a></p>"
                        html += "<table>"
                        html += "<tr><th>Branch</th><th>Platform</th><th>UDID</th><th>Status</th><th>Duration(s)</th><th>Session Created</th><th>Test Executed</th><th>Ports</th><th>Report</th></tr>"

                        rows.each { item ->
                            def ports = item.platform == 'ios'
                                ? "appium=${item.appiumPort}<br>wda=${item.wdaLocalPort}<br>mjpeg=${item.mjpegServerPort}"
                                : "appium=${item.appiumPort}<br>system=${item.systemPort}"

                            def statusClass = "warn"
                            if (item.status == 'PASSED') {
                                statusClass = "ok"
                            } else if (item.status == 'FAILED_SETUP' || item.status == 'FAILED_TEST') {
                                statusClass = "bad"
                            }

                            html += "<tr>"
                            html += "<td><code>${item.branchName}</code></td>"
                            html += "<td>${item.platform}</td>"
                            html += "<td>${item.udid}</td>"
                            html += "<td class='${statusClass}'>${item.status}</td>"
                            html += "<td>${item.durationSec}</td>"
                            html += "<td>${item.sessionCreated}</td>"
                            html += "<td>${item.testExecuted}</td>"
                            html += "<td>${ports}</td>"
                            html += "<td><a href='../reports/device/${item.branchName}/index.html'>Open device report</a></td>"
                            html += "</tr>"
                        }

                        html += "</table></body></html>"

                        writeFile file: 'device-dashboard/index.html', text: html
                        archiveArtifacts artifacts: 'device-dashboard/**', allowEmptyArchive: true
                    }
                }
            }
        }
    }

    post {
        always {
            withEnv(commonEnv()) {
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

/* ===================== HELPERS ===================== */

def commonEnv() {
    return [
        "HOME=${env.USER_HOME}",
        "PATH=${env.FULL_PATH}",
        "ANDROID_HOME=${env.ANDROID_HOME}",
        "ANDROID_SDK_ROOT=${env.ANDROID_SDK_ROOT}"
    ]
}

def runDeviceBranch(Map d) {
    def platform = d.platform
    def udid = d.udid
    def shortUdid = d.shortUdid
    def branchName = d.branchName
    def slot = d.slot
    def appiumPort = d.appiumPort as Integer
    def appiumUrl = "http://127.0.0.1:${appiumPort}"

    def branchWs = "${env.WORKSPACE}@${branchName}"
    def mergeDir = "${env.WORKSPACE}/allure-merge/${branchName}"
    def dashboardDir = "${env.WORKSPACE}/device-dashboard/${branchName}"
    def artifactDir = "${env.WORKSPACE}/artifacts/${branchName}"

    ws(branchWs) {
        withEnv(commonEnv()) {
            deleteDir()
            unstash 'repo-source'

            def appiumPid = ''
            def resultStatus = 'UNKNOWN'
            def startTs = System.currentTimeMillis()
            def sessionCreated = false
            def testExecuted = false

            try {
                sh """
                    set +e
                    mkdir -p "target/allure-results-${udid}" artifacts
                    echo "===== BRANCH INFO ====="
                    echo "BranchName=${branchName}"
                    echo "Platform=${platform}"
                    echo "UDID=${udid}"
                    echo "ShortUDID=${shortUdid}"
                    echo "Slot=${slot}"
                    echo "Workspace=\$(pwd)"
                    echo "AppiumUrl=${appiumUrl}"
                    echo "PATH=\$PATH"
                """

                verifyDeviceReady(d)
                cleanupPortsBeforeStart(d)

                sh """
                    set -e
                    nohup env \\
                      HOME="${env.USER_HOME}" \\
                      PATH="${env.FULL_PATH}" \\
                      ANDROID_HOME="${env.ANDROID_HOME}" \\
                      ANDROID_SDK_ROOT="${env.ANDROID_SDK_ROOT}" \\
                      "${env.APPIUM_BIN}" server \\
                      --address 127.0.0.1 \\
                      --port ${appiumPort} \\
                      --log-level info \\
                      > "appium-${branchName}.log" 2>&1 &
                    echo \$! > "appium-${branchName}.pid"
                """

                appiumPid = sh(
                    script: "cat appium-${branchName}.pid",
                    returnStdout: true
                ).trim()

                sh """
                    set -e
                    echo "Waiting Appium on ${appiumUrl}/status"
                    for i in \$(seq 1 50); do
                      if curl -sf "${appiumUrl}/status" >/dev/null 2>&1; then
                        echo "Appium is ready on ${appiumUrl}"
                        exit 0
                      fi
                      sleep 1
                    done
                    echo "Appium did not become ready in time"
                    cat "appium-${branchName}.log" || true
                    exit 1
                """

                def mvnExit = 0
                if (d.platform == 'ios') {
                    // returnStatus: mã thoát phải là của mvn — không dùng echo $? làm lệnh cuối (luôn 0).
                    mvnExit = sh(
                        script: """
                            set +e
                            mvn clean test \\
                              -DsuiteXmlFile=testng-jenkins.xml \\
                              -Dplatform=ios \\
                              -DappiumServer=${appiumUrl} \\
                              -Dios.udid="${d.udid}" \\
                              -Dios.wdaLocalPort.base=${d.wdaLocalPort} \\
                              -Dios.mjpegServerPort.base=${d.mjpegServerPort} \\
                              -Dios.derivedDataPath="${d.derivedDataPath}" \\
                              -Dallure.results.directory=target/allure-results-${d.udid}
                            MVN_EXIT=\$?
                            echo "\$MVN_EXIT" > .mvn_exit_code
                            exit \$MVN_EXIT
                        """,
                        returnStatus: true
                    )
                } else {
                    mvnExit = sh(
                        script: """
                            set +e
                            mvn clean test \\
                              -DsuiteXmlFile=testng-jenkins.xml \\
                              -Dplatform=android \\
                              -DappiumServer=${appiumUrl} \\
                              -Dandroid.udid="${d.udid}" \\
                              -Dandroid.systemPort.base=${d.systemPort} \\
                              -Dallure.results.directory=target/allure-results-${d.udid}
                            MVN_EXIT=\$?
                            echo "\$MVN_EXIT" > .mvn_exit_code
                            exit \$MVN_EXIT
                        """,
                        returnStatus: true
                    )
                }

                testExecuted = true

                def surefireTxt = sh(
                    script: "find target/surefire-reports -type f -maxdepth 1 2>/dev/null | xargs cat 2>/dev/null || true",
                    returnStdout: true
                )

                if (surefireTxt?.contains("Session ID:")) {
                    sessionCreated = true
                }

                if (mvnExit != 0) {
                    if (!sessionCreated && platform == 'ios') {
                        resultStatus = 'FAILED_SETUP'
                    } else {
                        resultStatus = 'FAILED_TEST'
                    }
                    error("Maven test failed for ${branchName} with status ${resultStatus}")
                } else {
                    resultStatus = 'PASSED'
                }

            } catch (err) {
                if (resultStatus == 'UNKNOWN') {
                    resultStatus = 'FAILED_SETUP'
                }
                throw err
            } finally {
                def durationSec = ((System.currentTimeMillis() - startTs) / 1000L) as Long

                sh """
                    set +e

                    if [ -n "${appiumPid}" ]; then
                      kill ${appiumPid} || true
                    fi

                    if [ -f "appium-${branchName}.pid" ]; then
                      kill \$(cat "appium-${branchName}.pid") || true
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

                    mkdir -p "target/allure-results-${udid}"
                    cat > "target/allure-results-${udid}/environment.properties" <<EOF
Platform=${platform}
DeviceUDID=${udid}
ShortUDID=${shortUdid}
BranchName=${branchName}
Slot=${slot}
AppiumServer=${appiumUrl}
AppiumPort=${appiumPort}
Result=${resultStatus}
DurationSec=${durationSec}
SessionCreated=${sessionCreated}
TestExecuted=${testExecuted}
EOF

                    mkdir -p "${mergeDir}"
                    cp -R "target/allure-results-${udid}/." "${mergeDir}/" || true

                    mkdir -p "${dashboardDir}"
                    mkdir -p "${artifactDir}"
                    cp -f "appium-${branchName}.log" "${artifactDir}/" || true
                    cp -R target/surefire-reports "${artifactDir}/surefire-reports" || true
                """

                writeJSON(
                    file: "${dashboardDir}/summary.json",
                    json: d + [
                        status        : resultStatus,
                        durationSec   : durationSec,
                        sessionCreated: sessionCreated,
                        testExecuted  : testExecuted
                    ],
                    pretty: 4
                )
            }
        }
    }
}

def verifyDeviceReady(Map d) {
    if (d.platform == 'ios') {
        def iosAlive = sh(
            script: "${env.IDEVICE_ID_BIN} -l | grep '${d.udid}' || true",
            returnStdout: true
        ).trim()

        if (!iosAlive) {
            error("iOS device ${d.udid} is not connected.")
        }

        sh """
            set +e
            rm -rf "${d.derivedDataPath}" || true
            mkdir -p "${d.derivedDataPath}"
        """
    } else {
        def androidAlive = sh(
            script: "adb -s '${d.udid}' get-state 2>/dev/null || true",
            returnStdout: true
        ).trim()

        if (androidAlive != 'device') {
            error("Android device ${d.udid} is not ready. State=${androidAlive}")
        }
    }
}

def cleanupPortsBeforeStart(Map d) {
    sh """
        set +e
        lsof -ti tcp:${d.appiumPort} | xargs kill -9 2>/dev/null || true
    """

    if (d.platform == 'ios') {
        sh """
            set +e
            lsof -ti tcp:${d.wdaLocalPort} | xargs kill -9 2>/dev/null || true
            lsof -ti tcp:${d.mjpegServerPort} | xargs kill -9 2>/dev/null || true
        """
    } else {
        sh """
            set +e
            lsof -ti tcp:${d.systemPort} | xargs kill -9 2>/dev/null || true
        """
    }
}