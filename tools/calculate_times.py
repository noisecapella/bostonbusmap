import argparse
import csv
import time

import schema

def make_weekdays(arr):
    """Inputs array of seven days, starting with Monday, where value is 1 or 0. Returns integer of equivalent bits"""
    ret = 0
    for i in xrange(len(arr)):
        if arr[i]:
            ret |= 2**i

    return ret

def parse_time(s):
    """Returns seconds from beginning of day. May go into tomorrow slightly"""
    hour, minute, second = s.split(":")
    hour = int(hour)
    minute = int(minute)
    second = int(second)

    if hour >= 24:
        day = 1
        hour -= 24
    else:
        day = 0

    return second + 60*minute + 60*60*hour + 24*60*60*day

def main():
    parser = argparse.ArgumentParser(description='Figure out what times a particular piece of the transit system is running.')
    parser.add_argument("stop_times", type=str)
    parser.add_argument("trips", type=str)
    parser.add_argument("calendar", type=str)
    args = parser.parse_args()

    trip_to_route = {}
    trip_indexes = {}

    with open(args.trips) as f:
        reader = csv.reader(f)

        header = reader.next()
        for i in xrange(len(header)):
            trip_indexes[header[i]] = i

        route_index = trip_indexes["route_id"]
        service_index = trip_indexes["service_id"]
        trip_index = trip_indexes["trip_id"]
        for row in reader:
            trip = row[trip_index]
            route = row[route_index]
            service = row[service_index]

            trip_to_route[trip] = (route, service)

    calendar = {}
    with open(args.calendar) as f:
        reader = csv.reader(f)

        header = reader.next()
        for i in xrange(len(header)):
            trip_indexes[header[i]] = i
            
        service_index = trip_indexes["service_id"]
        monday_index = trip_indexes["monday"]
        tuesday_index = trip_indexes["tuesday"]
        wednesday_index = trip_indexes["wednesday"]
        thursday_index = trip_indexes["thursday"]
        friday_index = trip_indexes["friday"]
        saturday_index = trip_indexes["saturday"]
        sunday_index = trip_indexes["sunday"]
        for row in reader:
            service_id = row[service_index]
            monday = row[monday_index]
            tuesday = row[tuesday_index]
            wednesday = row[wednesday_index]
            thursday = row[thursday_index]
            friday = row[friday_index]
            saturday = row[saturday_index]
            sunday = row[sunday_index]
            
            calendar[service_id] = (int(monday), int(tuesday), int(wednesday), int(thursday), int(friday), int(saturday), int(sunday))

    trip_intervals = {}


    with open(args.stop_times) as f:
        reader = csv.reader(f)

        header = reader.next()
        for i in xrange(len(header)):
            trip_indexes[header[i]] = i

        trip_index = trip_indexes["trip_id"]
        arrival_index = trip_indexes["arrival_time"]
        departure_index = trip_indexes["departure_time"]
        for row in reader:
            if arrival_index >= len(row) or departure_index >= len(row) or trip_index >= len(row):
                continue
            arrival = parse_time(row[arrival_index])
            departure = parse_time(row[departure_index])

            trip = row[trip_index]
            route, service = trip_to_route[trip]
            if service in calendar:
                key = (route, calendar[service])

                if key in trip_intervals:
                    old_departure, old_arrival = trip_intervals[key]
                    new_departure = None
                    new_arrival = None
                    if departure < old_departure:
                        trip_intervals[key] = (departure, trip_intervals[key][1])
                    if arrival > old_arrival:
                        trip_intervals[key] = (trip_intervals[key][0], arrival)
                else:
                    trip_intervals[key] = (departure, arrival)

        obj = schema.getSchemaAsObject()
        for key, intervals in trip_intervals.items():
            route, days = key
            begin, end = intervals
            obj.bounds.route.value = route
            obj.bounds.weekdays.value = make_weekdays(days)
            obj.bounds.start.value = begin
            obj.bounds.stop.value = end
            obj.bounds.insert()
if __name__ == "__main__":
    main()





