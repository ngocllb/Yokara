pipeline {
    agent none

    triggers {
        // Run automatically at 7:00 AM every day
        cron('0 7 * * *')
    }

    options {
        // Skip default checkout to apply custom shallow clone logic
        skipDefaultCheckout(true)
        // Tránh 2 build cùng job đè workspace/resources của nhau
        disableConcurrentBuilds()
    }

    environment {
        // Ensure Jenkins can find 'idevice_id', 'tidevice', and 'adb' in common mac paths
        PATH = "/opt/homebrew/bin:/usr/local/bin:${env.HOME}/Library/Android/sdk/platform-tools:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            agent any
            steps {
                // Fetch only the latest commit from 'main' (shallow clone)
                checkout scmGit(
                    branches: [[name: '*/main']],
                    extensions: [[$class: 'CloneOption', depth: 1, noTags: true, shallow: true]],
                    userRemoteConfigs: [[url: 'https://github.com/ngocllb/Yokara']]
                )
                // Stash source để các nhánh parallel chạy trên workspace tách biệt
                stash name: 'repo-source', includes: '**/*', useDefaultExcludes: false
            }
        }

        stage('Detect Devices & Run Parallel Tests') {
            agent any
            steps {
                script {
                    echo "Checking for physically connected devices..."
                    sh "rm -rf allure-merge && mkdir -p allure-merge"
                    
                    // Run python script to discover platforms and udids
                    def devicesStr = sh(script: "python3 scripts/get_jenkins_devices.py", returnStdout: true).trim()
                    
                    if (!devicesStr) {
                        error("No physically connected devices found via ADB/idevice_id!")
                    }
                    
                    def lines = devicesStr.split('\n')
                    echo "Detected ${lines.size()} devices: \n${devicesStr}"
                    
                    def parallelStages = [:]
                    
                    for (int i = 0; i < lines.size(); i++) {
                        def idx = i
                        def line = lines[i].trim()
                        if (!line || !line.contains('|')) continue
                        
                        def parts = line.split('\\|')
                        def platform = parts[0]
                        def udid = parts[1]
                        def shortUdid = udid.size() > 8 ? udid.substring(udid.size() - 8) : udid
                        def branchWs = ".jenkins-ws/${env.BUILD_TAG}/${platform}-${shortUdid}"
                        def mergeDir = "${env.WORKSPACE}/allure-merge/${platform}-${shortUdid}"
                        // Mỗi branch có appium server riêng để tránh tranh chấp session/socket.
                        def appiumPort = 4723 + idx
                        def appiumUrl = "http://127.0.0.1:${appiumPort}"
                        // Mỗi process Maven chạy riêng nên cần base port riêng theo branch để tránh đụng.
                        def androidSystemPortBase = 8300 + (idx * 100)
                        def iosWdaLocalPortBase = 8100 + (idx * 100)
                        def nodeLabel = (platform == 'ios')
                                ? (env.IOS_NODE_LABEL ?: env.NODE_NAME)
                                : (env.ANDROID_NODE_LABEL ?: env.NODE_NAME)
                        
                        parallelStages["Test ${platform.toUpperCase()}-${shortUdid}"] = {
                            lock(resource: "mobile-${udid}", inversePrecedence: true) {
                            node(nodeLabel) {
                                stage("Run on ${udid}") {
                                    ws(branchWs) {
                                        deleteDir()
                                        unstash 'repo-source'
                                        String appiumPid = ''
                                        try {
                                            // Make sure results folder is clean in isolated workspace
                                            sh "rm -rf target/allure-results-${udid}"
                                            // Start dedicated Appium server for this branch
                                            sh """
                                                nohup appium --address 127.0.0.1 --port ${appiumPort} > appium-${platform}-${shortUdid}.log 2>&1 &
                                                echo \$! > appium-${platform}-${shortUdid}.pid
                                            """
                                            appiumPid = sh(script: "cat appium-${platform}-${shortUdid}.pid", returnStdout: true).trim()

                                            // Execute Maven test bound to this specific device
                                            sh """
                                                mvn clean test -DsuiteXmlFile=\"testng-jenkins.xml\" \
                                                -Dplatform=${platform} \
                                                -DappiumServer=${appiumUrl} \
                                                -Dandroid.systemPort.base=${androidSystemPortBase} \
                                                -Dios.wdaLocalPort.base=${iosWdaLocalPortBase} \
                                                -D${platform}.udid=\"${udid}\" \
                                                -Dallure.results.directory=target/allure-results-${udid}
                                            """
                                        } finally {
                                            // Stop branch Appium server if started
                                            if (appiumPid?.trim()) {
                                                sh "kill ${appiumPid} || true"
                                            } else {
                                                sh "if [ -f appium-${platform}-${shortUdid}.pid ]; then kill \$(cat appium-${platform}-${shortUdid}.pid) || true; fi"
                                            }
                                            // Create environment.properties for Allure to distinguish the devices in the final report
                                            sh """
                                                mkdir -p target/allure-results-${udid}
                                                echo "Platform=${platform}" > target/allure-results-${udid}/environment.properties
                                                echo "DeviceUDID=${udid}" >> target/allure-results-${udid}/environment.properties
                                                echo "AppiumServer=${appiumUrl}" >> target/allure-results-${udid}/environment.properties

                                                mkdir -p "${mergeDir}"
                                                if [ -d "target/allure-results-${udid}" ]; then
                                                  cp -R "target/allure-results-${udid}/." "${mergeDir}/" || true
                                                fi
                                            """
                                        }
                                    }
                                }
                            }
                            }
                        }
                    }
                    
                    // Execute all generated stages concurrently
                    parallel parallelStages
                }
            }
        }
    }

    post {
        always {
            // Aggregate all the separate allure-results directories copied from isolated branch workspaces.
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
