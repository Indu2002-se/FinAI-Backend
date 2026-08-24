#!/bin/bash

# FinAI Backend Startup Script

echo "🚀 Starting FinAI Backend..."
echo ""

# Set Java Home
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java not found!"
    echo "Please install: sudo apt install openjdk-21-jdk"
    exit 1
fi

# Check Java version
echo "📌 Java Version:"
java -version
echo ""

# Check if MySQL is running
if ! netstat -tlnp 2>/dev/null | grep -q 3306; then
    echo "⚠️  Warning: MySQL (port 3306) is not running!"
    echo "Please start XAMPP MySQL: sudo /opt/lampp/lampp startmysql"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Get computer IP
COMPUTER_IP=$(hostname -I | awk '{print $1}')
echo "💻 Your Computer IP: $COMPUTER_IP"
echo "📱 Mobile app should use: http://$COMPUTER_IP:8080/api"
echo ""

# Check if port 8080 is already in use
if netstat -tlnp 2>/dev/null | grep -q 8080; then
    echo "⚠️  Warning: Port 8080 is already in use!"
    read -p "Kill existing process? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        PID=$(netstat -tlnp 2>/dev/null | grep 8080 | awk '{print $7}' | cut -d'/' -f1)
        if [ ! -z "$PID" ]; then
            sudo kill -9 $PID
            echo "✓ Killed process $PID"
        fi
    else
        exit 1
    fi
fi

echo "🔥 Starting backend on port 8080..."
echo "📊 Backend will be accessible at: http://localhost:8080"
echo "📱 Mobile device access: http://$COMPUTER_IP:8080"
echo ""
echo "Press Ctrl+C to stop the backend"
echo ""

# Start the backend
./gradlew bootRun --console=plain
