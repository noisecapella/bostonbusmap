library(argparse)
library(plyr)
library(maps)
library(mapproj)

plot.shape <- function() {
  parser <- ArgumentParser()
  parser$add_argument("gtfs.dir")
  parser$add_argument("route")
  args <- parser$parse_args()

  shapes.file <- file.path(args$gtfs.dir, "shapes.txt")
  trips.file <- file.path(args$gtfs.dir, "trips.txt")

  shapes <- read.csv(shapes.file, header=TRUE)
  trips <- read.csv(trips.file, header=TRUE)

  print(sprintf("All trips: %d", length(unlist(trips$trip_id))))
  trips.by.route <- trips[trips$route_id == args$route,c('trip_id', 'shape_id')]
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
  all.lat <- shapes.by.trip$shape_pt_lat
  all.lon <- shapes.by.trip$shape_pt_lon

  png("out.png", width=6000, height=6000)
  ylim <- range(all.lat)
  xlim <- range(all.lon)

  map('county', col='grey', fill=TRUE, proj="gilbert", orientation=c(90, 0, 225), xlim=xlim, ylim=ylim)

  title('shapes')
  if (length(shape.ids) < 1) {
    stop("No shape to plot")
  }
  draw.line <- function(shape.id) {
    shapes <- shapes.by.trip[shapes.by.trip$shape_id == shape.id, ]
    
    shape.lon <- shapes$shape_pt_lon
    shape.lat <- shapes$shape_pt_lat
    
    coord <- mapproject(shape.lon, shape.lat, proj="gilbert", orientation=c(90, 0, 225))
    lines(coord, col="red", xlim=xlim, ylim=ylim)
  }

  lapply(shape.ids, draw.line)
  return()
  # group shapes by shape_id so you have a bunch of rows shape_id, lat, lon
  # plot lines on maps
  # profit
}
