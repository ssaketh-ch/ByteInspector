#!/bin/bash

# Set the working directory
cd /home/saketh/Desktop/java/project9

# Compile analyzer files first
make -f Makefile.mak -n compile

# Launch GUI in background
make -f Makegui.mak compile
make -f Makegui.mak run &

# Monitor uploads folder for new files
while true; do
    inotifywait -e create,moved_to /home/saketh/Desktop/java/project9/uploads
    echo "New file detected in uploads folder"
    
    # Wait a moment to ensure file is fully uploaded
    sleep 1
    
    # Run FileProcessor
    make -f Make.mak compile
    make -f Make.mak run
done