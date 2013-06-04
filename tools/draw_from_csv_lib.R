library(ggmap)
source('plot_shape_lib.R')

draw.from.csv <- function(csv.filename) {
  paths <- read.csv(csv.filename)
  colnames(paths) <- c('shape_id', 'shape_pt_lat', 'shape_pt_lon')
  graph.shape(paths)
}
