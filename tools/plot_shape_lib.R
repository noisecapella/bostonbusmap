library(plyr)
library(ggmap)
library(mapproj)

plot.shape <- function(gtfs.dir, route) {
  shapes.file <- file.path(gtfs.dir, "shapes.txt")
  trips.file <- file.path(gtfs.dir, "trips.txt")

  shapes <- read.csv(shapes.file, header=TRUE)
  trips <- read.csv(trips.file, header=TRUE)

  print(sprintf("All trips: %d", length(unlist(trips$trip_id))))
  trips.by.route <- trips[trips$route_id == route,c('trip_id', 'shape_id')]
  trip.length <- length(unlist(trips.by.route$trip_id))
  print(sprintf("Trips: %d", trip.length))
  if (trip.length == 0) {
    stop("No trips found. Maybe try adding a '0' before the route if it's a single digit?")
  }
#  shapes.by.trip <- merge(trips.by.route, shapes, by="shape_id")
  trip.shapes <- unique(unlist(trips.by.route[,c('shape_id')]))
  shapes.by.trip <- shapes[shapes$shape_id %in% trip.shapes, ]

  print(sprintf("Shapes: %d", length(unlist(shapes.by.trip$shape_id))))

  graph.shape(shapes.by.trip)
}

graph.shape <- function(shapes.by.trip) {
  shape.ids <- unique(unlist(shapes.by.trip$shape_id))
  if (is.null(shape.ids)) {
    stop("shape.ids is null")
  }
  if (length(shape.ids) < 1) {
    stop("No shape to plot")
  }
  all.lat <- shapes.by.trip$shape_pt_lat
  all.lon <- shapes.by.trip$shape_pt_lon

  ylim <- range(all.lat)
  xlim <- range(all.lon)

  #map('county', col='grey', fill=TRUE, proj="gilbert", orientation=c(90, 0, 225), xlim=xlim, ylim=ylim)
  box <- c(xlim[1], ylim[1], xlim[2], ylim[2])
  map <- get_map(location=box)
  ggmap(map) + geom_path(aes(y=shape_pt_lat, x=shape_pt_lon, group=shape_id),
                         data=shapes.by.trip, colour="red", size=1)

  ggsave(file="out.png")

  return()
  # group shapes by shape_id so you have a bunch of rows shape_id, lat, lon
  # plot lines on maps
  # profit
}
