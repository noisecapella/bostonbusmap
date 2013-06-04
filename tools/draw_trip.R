library(argparse)
source("draw_trip_lib.R")

main <- function() {
  parser <- ArgumentParser()
  parser$add_argument("shapes.json")
  parser$add_argument("gtfs.dir")
  parser$add_argument("route")
  args <- parser$parse_args()

  draw.trip(args$shapes.json, args$gtfs.dir, args$route)
}

main()
