import requests
import sys
import gtfs_realtime_pb2

def main():
    data = requests.get("https://cdn.mbta.com/realtime/Alerts.pb").content
    update = gtfs_realtime_pb2.FeedMessage()
    update.ParseFromString(data)

    print(update)

if __name__ == "__main__":
    main()
