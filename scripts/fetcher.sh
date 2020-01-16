CONFIG="$1"
if [ -z "$CONFIG" ]; then
  CONFIG=config/fetcher.ini
fi

./fetcher/build/install/fetcher/bin/fetcher "$CONFIG"
