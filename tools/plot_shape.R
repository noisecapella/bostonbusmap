library(argparse)
source("plot_shape_lib.R")
main <- function() {
  parser <- ArgumentParser()
  parser$add_argument("gtfs.dir")
  parser$add_argument("route")
  args <- parser$parse_args()

  plot.shape(args$gtfs.dir, args$route)
}
main()


