#!/bin/bash

echo "Stopping TRK Blockchain Application..."

pkill -f "spring-boot:run" 2>/dev/null
pkill -f "ng serve" 2>/dev/null
pkill -f "blockchain-1.0.0.jar" 2>/dev/null

lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:4200 | xargs kill -9 2>/dev/null

echo "Application stopped."
