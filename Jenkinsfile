pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {
        stage('Test Webhook') {
            steps {
                script {
                    echo "=== WEBHOOK TRIGGERED SUCCESSFULLY ==="
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Git Branch: ${env.GIT_BRANCH}"
                    echo "Git Commit: ${env.GIT_COMMIT}"
                    echo "Workspace: ${env.WORKSPACE}"

                    // ดู build causes
                    def causes = currentBuild.getBuildCauses()
                    echo "Build Causes: ${causes}"

                    // ดูว่ามี source code หรือไม่
                    sh 'pwd'
                    sh 'ls -la'
                    sh 'git log --oneline -3'
                }
            }
        }

        stage('Simple Build Test') {
            steps {
                echo "Testing simple build..."
                sh 'echo "Pipeline is working!"'
            }
        }
    }

    post {
        always {
            echo "Pipeline finished - Build #${env.BUILD_NUMBER}"
        }
    }
}