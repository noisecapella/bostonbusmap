library(argparse)
library(plyr)
library(maps)
library(mapproj)

main <- function() {
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
  print(sprintf("Trips: %d", length(unlist(trips.by.route$trip_id))))
  shapes.by.trip <- merge(trips.by.route, shapes, by="shape_id")
  print(sprintf("Shapes: %d", length(unlist(shapes.by.trip$shape_id))))

  shape.ids <- unique(unlist(shapes.by.trip$shape_id))
  all.lat <- shapes.by.trip$shape_pt_lat
  all.lon <- shapes.by.trip$shape_pt_lon

  png("out.png")
  ylim <- range(all.lat)
  xlim <- range(all.lon)

  map('county', col='grey', fill=TRUE, proj="gilbert", orientation=c(90, 0, 225), xlim=xlim, ylim=ylim)

  title('shapes')
  for (i in 1:length(shape.ids)) {
    shape.id <- shape.ids[[i]]
    shapes <- shapes.by.trip[shapes.by.trip$shape_id == shape.id,]
    
    shape.lon <- shapes$shape_pt_lon
    shape.lat <- shapes$shape_pt_lat
    
    coord <- mapproject(shape.lon, shape.lat, proj="gilbert", orientation=c(90, 0, 225))
    lines(coord, col="red", xlim=xlim, ylim=ylim)
    
  }
  
  # group shapes by shape_id so you have a bunch of rows shape_id, lat, lon
  # plot lines on maps
  # profit
}

main()


