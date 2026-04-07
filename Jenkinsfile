pipeline {
    agent any

    triggers {
        // Run automatically at 7:00 AM every day
        cron('0 7 * * *')
    }

    options {
        // Skip default checkout to apply custom shallow clone logic
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            steps {
                // Fetch only the latest commit from 'main' (shallow clone)
                checkout scmGit(
                    branches: [[name: '*/main']],
                    extensions: [[$class: 'CloneOption', depth: 1, noTags: true, shallow: true]],
                    userRemoteConfigs: [[url: 'https://github.com/ngocllb/Yokara']]
                )
            }
        }

        stage('Detect Devices & Run Parallel Tests') {
            steps {
                script {
                    echo "Checking for physically connected devices..."
                    
                    // Run python script to discover platforms and udids
                    def devicesStr = sh(script: "python3 scripts/get_jenkins_devices.py", returnStdout: true).trim()
                    def devices = readJSON text: devicesStr
                    
                    if (devices.size() == 0) {
                        error("No physically connected devices found via ADB/idevice_id!")
                    }
                    
                    echo "Detected ${devices.size()} devices: ${devices}"
                    
                    def parallelStages = [:]
                    
                    for (int i = 0; i < devices.size(); i++) {
                        def device = devices[i]
                        def platform = device.platform
                        def udid = device.udid
                        def shortUdid = udid.size() > 8 ? udid.substring(udid.size() - 8) : udid
                        
                        parallelStages["Test ${platform.toUpperCase()}-${shortUdid}"] = {
                            stage("Run on ${udid}") {
                                try {
                                    // Make sure results folder is clean
                                    sh "rm -rf target/allure-results-${udid}"
                                    
                                    // Execute Maven test bound to this specific device
                                    sh """
                                        mvn clean test -DsuiteXmlFile="testng-jenkins.xml" \
                                        -Dplatform=${platform} \
                                        -D${platform}.udid="${udid}" \
                                        -Dallure.results.directory=target/allure-results-${udid}
                                    """
                                } finally {
                                    // Create environment.properties for Allure to distinguish the devices in the final report
                                    sh """
                                        echo "Platform=${platform}" > target/allure-results-${udid}/environment.properties
                                        echo "DeviceUDID=${udid}" >> target/allure-results-${udid}/environment.properties
                                    """
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
            // Aggregate all the separate allure-results directories into one comprehensive Allure report
            // The Jenkins Allure Plugin will generate a single link containing data from all devices.
            allure([
                includeProperties: false, 
                jdk: '', 
                properties: [], 
                reportBuildPolicy: 'ALWAYS', 
                results: [[path: 'target/allure-results-*']]
            ])
        }
    }
}
