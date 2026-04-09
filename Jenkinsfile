pipeline {
    agent any

    triggers {
        cron('0 7 * * *')
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        USER_HOME = '/Users/quhuy'
        ANDROID_HOME = '/Users/quhuy/Library/Android/sdk'
        ANDROID_SDK_ROOT = '/Users/quhuy/Library/Android/sdk'
        MOBILE_EXTRA_PATH = '/Users/quhuy/.local/bin:/opt/homebrew/bin:/usr/local/bin:/Users/quhuy/Library/Android/sdk/platform-tools:/Users/quhuy/Library/Android/sdk/emulator'
        IDEVICE_ID_BIN = '/opt/homebrew/bin/idevice_id'
        APPIUM_BIN = '/Users/quhuy/.nvm/versions/node/v22.22.2/bin/appium'
        PYTHON_BIN = '/opt/homebrew/bin/python3'
    }

    stages {
        stage('Checkout') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.MOBILE_EXTRA_PATH}:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin"
                ]) {
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
                    "PATH=${env.MOBILE_EXTRA_PATH}:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin"
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

                        echo "----- VERSIONS -----"
                        python3 --version || true
                        adb version || true
                        appium --version || true
                        tidevice version || true
                        mvn -v || true
                    '''
                }
            }
        }

        stage('Detect Devices & Run Parallel Tests') {
            steps {
                withEnv([
                    "HOME=${env.USER_HOME}",
                    "PATH=${env.MOBILE_EXTRA_PATH}:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin"
                ]) {
                    script {
                        sh 'rm -rf allure-merge && mkdir -p allure-merge'

                        def devicesStr = sh(
                            script: "${env.PYTHON_BIN} scripts/get_jenkins_devices.py",
                            returnStdout: true
                        ).trim()

                        if (!devicesStr) {
                            error("No physically connected devices found via adb / idevice_id / tidevice!")
                        }

                        def lines = devicesStr.split('\n')
                        echo "Detected ${lines.size()} devices:\n${devicesStr}"

                        def branches = [:]

                        for (int i = 0; i < lines.size(); i++) {
                            def idx = i
                            def line = lines[i].trim()
                            if (!line || !line.contains('|')) {
                                continue
                            }

                            def parts = line.split('\\|')
                            def platform = parts[0].trim().toLowerCase()
                            def udid = parts[1].trim()
                            def shortUdid = udid.size() > 8 ? udid.substring(udid.size() - 8) : udid

                            def branchName = "${platform}-${shortUdid}"
                            def branchWs = "${env.WORKSPACE}@${branchName}"
                            def mergeDir = "${env.WORKSPACE}/allure-merge/${branchName}"

                            def appiumPort = 4723 + idx
                            def appiumUrl = "http://127.0.0.1:${appiumPort}"

                            def androidSystemPortBase = 8300 + (idx * 100)
                            def iosWdaLocalPortBase = 8100 + (idx * 100)
                            def iosMjpegServerPortBase = 10100 + (idx * 100)

                            branches["Test ${platform.toUpperCase()}-${shortUdid}"] = {
                                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                    ws(branchWs) {
                                        withEnv([
                                            "HOME=${env.USER_HOME}",
                                            "PATH=${env.MOBILE_EXTRA_PATH}:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin"
                                        ]) {
                                            deleteDir()
                                            unstash 'repo-source'

                                            def appiumPid = ''

                                            try {
                                                sh """
                                                    set +e
                                                    echo "===== BRANCH INFO ====="
                                                    echo "Platform=${platform}"
                                                    echo "UDID=${udid}"
                                                    echo "Workspace=\$(pwd)"
                                                    echo "AppiumUrl=${appiumUrl}"
                                                    echo "PATH=\$PATH"
                                                    echo "ANDROID_HOME=\$ANDROID_HOME"
                                                    echo "ANDROID_SDK_ROOT=\$ANDROID_SDK_ROOT"
                                                    mkdir -p target/allure-results-${udid}
                                                    rm -rf target/surefire-reports || true
                                                """

                                                if (platform == 'android') {
                                                    sh """
                                                        set +e
                                                        lsof -ti tcp:${appiumPort} | xargs kill -9 || true
                                                        lsof -ti tcp:${androidSystemPortBase} | xargs kill -9 || true
                                                    """
                                                } else if (platform == 'ios') {
                                                    sh """
                                                        set +e
                                                        lsof -ti tcp:${appiumPort} | xargs kill -9 || true
                                                        lsof -ti tcp:${iosWdaLocalPortBase} | xargs kill -9 || true
                                                        lsof -ti tcp:${iosMjpegServerPortBase} | xargs kill -9 || true
                                                    """
                                                } else {
                                                    error("Unsupported platform: ${platform}")
                                                }

                                                sh """
                                                    which appium
                                                    ${env.APPIUM_BIN} --version

                                                    nohup ${env.APPIUM_BIN} --address 127.0.0.1 --port ${appiumPort} > appium-${platform}-${shortUdid}.log 2>&1 &
                                                    echo \$! > appium-${platform}-${shortUdid}.pid
                                                """

                                                appiumPid = sh(
                                                    script: "cat appium-${platform}-${shortUdid}.pid",
                                                    returnStdout: true
                                                ).trim()

                                                sh """
                                                    echo "Waiting Appium on ${appiumUrl}"
                                                    for i in \$(seq 1 30); do
                                                      if curl -s ${appiumUrl}/status >/dev/null 2>&1; then
                                                        echo "Appium is ready"
                                                        exit 0
                                                      fi
                                                      sleep 1
                                                    done
                                                    echo "Appium did not become ready in time"
                                                    echo "===== appium log ====="
                                                    cat appium-${platform}-${shortUdid}.log || true
                                                    exit 1
                                                """

                                                if (platform == 'android') {
                                                    sh """
                                                        adb devices || true

                                                        mvn clean test \
                                                          -DsuiteXmlFile=testng-jenkins.xml \
                                                          -Dplatform=android \
                                                          -DappiumServer=${appiumUrl} \
                                                          -Dandroid.systemPort.base=${androidSystemPortBase} \
                                                          -Dandroid.udid="${udid}" \
                                                          -Dallure.results.directory=target/allure-results-${udid}
                                                    """
                                                } else if (platform == 'ios') {
                                                    sh """
                                                        ${env.IDEVICE_ID_BIN} -l || true

                                                        mvn clean test \
                                                          -DsuiteXmlFile=testng-jenkins.xml \
                                                          -Dplatform=ios \
                                                          -DappiumServer=${appiumUrl} \
                                                          -Dios.wdaLocalPort.base=${iosWdaLocalPortBase} \
                                                          -Dios.mjpegServerPort.base=${iosMjpegServerPortBase} \
                                                          -Dios.udid="${udid}" \
                                                          -Dallure.results.directory=target/allure-results-${udid}
                                                    """
                                                } else {
                                                    error("Unsupported platform: ${platform}")
                                                }
                                            } finally {
                                                sh """
                                                    set +e
                                                    if [ -n "${appiumPid}" ]; then
                                                      kill ${appiumPid} || true
                                                    fi

                                                    if [ -f appium-${platform}-${shortUdid}.pid ]; then
                                                      kill \$(cat appium-${platform}-${shortUdid}.pid) || true
                                                    fi

                                                    mkdir -p target/allure-results-${udid}
                                                    echo "Platform=${platform}" > target/allure-results-${udid}/environment.properties
                                                    echo "DeviceUDID=${udid}" >> target/allure-results-${udid}/environment.properties
                                                    echo "AppiumServer=${appiumUrl}" >> target/allure-results-${udid}/environment.properties

                                                    mkdir -p "${mergeDir}"
                                                    if [ -d "target/allure-results-${udid}" ]; then
                                                      cp -R "target/allure-results-${udid}/." "${mergeDir}/" || true
                                                    fi

                                                    mkdir -p artifacts
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

                        parallel branches
                    }
                }
            }
        }
    }

    post {
        always {
            withEnv([
                "HOME=${env.USER_HOME}",
                "PATH=${env.MOBILE_EXTRA_PATH}:/bin:/usr/bin:/usr/sbin:/sbin:/Users/quhuy/.nvm/versions/node/v22.22.2/bin"
            ]) {
                script {
                    sh 'mkdir -p allure-merge'
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'allure-merge/*']]
                    ])
                }
            }
        }
    }
}