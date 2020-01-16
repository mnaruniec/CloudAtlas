CONFIG="$1"
if [ -z "$CONFIG" ]; then
  CONFIG=config/agent.ini
fi

./agent/build/install/agent/bin/agent "$CONFIG"
