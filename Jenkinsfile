pipeline {
    agent any

    environment {
        DOCKER_HUB_REPO = 'booknest'  // Change to your Docker Hub username/repo
    }

    tools {
        maven 'Maven-3'
        jdk 'JDK-17'
    }

    stages {

        // ── Stage 1: Checkout ───────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ── Stage 2: Build All Microservices ────────────────
        stage('Build Microservices') {
            stages {
                stage('Eureka Server') {
                    steps {
                        dir('eureka-server') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Auth Service') {
                    steps {
                        dir('auth-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Book Service') {
                    steps {
                        dir('book-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Cart Service') {
                    steps {
                        dir('cart-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Wallet Service') {
                    steps {
                        dir('wallet-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Review Service') {
                    steps {
                        dir('review-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Wishlist Service') {
                    steps {
                        dir('wishlist-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        // ── Stage 3: Run Unit Tests ─────────────────────────
        stage('Run Tests') {
            stages {
                stage('Test Auth Service') {
                    steps {
                        dir('auth-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'auth-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Book Service') {
                    steps {
                        dir('book-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'book-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Cart Service') {
                    steps {
                        dir('cart-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'cart-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Order Service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'order-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Wallet Service') {
                    steps {
                        dir('wallet-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'wallet-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Review Service') {
                    steps {
                        dir('review-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'review-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Wishlist Service') {
                    steps {
                        dir('wishlist-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'wishlist-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'notification-service/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        // ── Stage 4: Docker Build & Push ────────────────────
        stage('Docker Build & Compose') {
            steps {
                sh 'docker compose build'
            }
        }

        // ── Stage 5: Deploy (Docker Compose Up) ─────────────
        stage('Deploy') {
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d'
            }
        }

        // ── Stage 6: Health Check ───────────────────────────
        stage('Health Check') {
            steps {
                script {
                    // Wait for services to start
                    sleep(time: 30, unit: 'SECONDS')

                    // Check Eureka Server
                    sh 'curl -f http://localhost:8761/actuator/health || echo "Eureka not ready yet"'

                    // Check API Gateway
                    sh 'curl -f http://localhost:8080/actuator/health || echo "Gateway not ready yet"'
                }
            }
        }
    }

    post {
        success {
            echo '✅ BookNest Backend Pipeline completed successfully!'
        }
        failure {
            echo '❌ BookNest Backend Pipeline failed!'
            // Cleanup on failure
            script {
                try {
                    sh 'docker compose down || true'
                } catch (Exception e) {
                    echo "Cleanup failed: ${e.message}"
                }
            }
        }
        always {
            // Clean up workspace (optional)
            script {
                try {
                    cleanWs(cleanWhenNotBuilt: false)
                } catch (Exception e) {
                    echo "Clean workspace failed: ${e.message}"
                }
            }
        }
    }
}