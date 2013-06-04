library(argparse)
library(rjson)
library(hash)
library(plyr)
source('plot_shape_lib.R')

draw.trip <- function() {
  parser <- ArgumentParser()
  parser$add_argument("shapes.json")
  parser$add_argument("gtfs.dir")
  parser$add_argument("route")
  args <- parser$parse_args()

  shapes <- hash(fromJSON(file=args$shapes.json))

  trips.file <- file.path(args$gtfs.dir, "trips.txt")

  trips <- read.csv(trips.file, header=TRUE)
  trip.ids.for.route <- as.vector(trips[trips$route_id == args$route,c('trip_id')])
#  trip.ids.for.route <- trip.ids.for.route[[2]]
  
  each.trip <- function(trip.id) {
    trip.tag <- tail(strsplit(trip.id, "-")[[1]], 1)
    lat.lons <- shapes[[trip.tag]]
    if (!is.null(lat.lons)) {
      lat.lon.pairs <- split(lat.lons, c('shape_pt_lat', 'shape_pt_lon'))
      as.data.frame(lat.lon.pairs)
    }
    # else NULL
  }

  cr.shapes <- mapply(each.trip, trip.ids.for.route)
  cr.shapes <- Filter(function (x) { !is.null(x) }, cr.shapes)

  count <- 0
  cbind.func <- function(x) {
    ret <- cbind(shape_id=count, x)
    count <- count + 1
    ret
  }
  
  cr.shapes.with.id <- mapply(cbind.func, cr.shapes, SIMPLIFY=F)
  cr.shapes.with.id <- rbind.fill(cr.shapes.with.id)
  graph.shape(cr.shapes.with.id)
}
