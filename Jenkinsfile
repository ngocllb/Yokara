pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/ngocllb/Yokara'
                stash name: 'source', includes: '**/*'
            }
        }

        stage('Run Parallel Tests') {
            steps {
                script {
                    def devices = sh(
                        script: "/opt/homebrew/bin/python3 scripts/get_jenkins_devices.py",
                        returnStdout: true
                    ).trim().split("\n")

                    def branches = [:]

                    devices.each { line ->
                        def (platform, udid) = line.split("\\|")
                        def shortUdid = udid.takeRight(8)

                        branches["${platform}-${shortUdid}"] = {
                            node {
                                deleteDir()
                                unstash 'source'

                                def allureDir = "target/allure-results-${udid}"
                                def appiumPort = platform == 'android' ? '4723' : '4724'
                                def extraArgs = platform == 'android'
                                        ? "-Dandroid.udid=${udid} -Dandroid.systemPort.base=8300"
                                        : "-Dios.udid=${udid} -Dios.wdaLocalPort.base=8200 -Dios.mjpegServerPort.base=10200"

                                sh "mkdir -p ${allureDir}"

                                try {
                                    sh """
                                        mvn clean test \
                                        -DsuiteXmlFile=testng-jenkins.xml \
                                        -Dplatform=${platform} \
                                        -DappiumServer=http://127.0.0.1:${appiumPort} \
                                        ${extraArgs} \
                                        -Dallure.results.directory=${allureDir}
                                    """
                                } finally {
                                    sh """
                                        mkdir -p ${WORKSPACE}/allure-merge
                                        cp -R ${allureDir}/* ${WORKSPACE}/allure-merge/ || true
                                    """
                                    archiveArtifacts artifacts: "${allureDir}/**", allowEmptyArchive: true
                                }
                            }
                        }
                    }

                    parallel branches
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                sh """
                    rm -rf allure-report
                    allure generate allure-merge -o allure-report --clean
                """
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
