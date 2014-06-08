library(dplyr)
library(ggmap)
library(mapproj)

graph.shape <- function(paths, output.path) {
  box <- c(left=min(paths$lon), top=max(paths$lat), right=max(paths$lon), bottom=min(paths$lat))
  box <- widen.box(box)
  map <- get_map(location=box, maptype='roadmap')
  gmap <- ggmap(map)

  gmap <- gmap + geom_path(aes(y=lat, x=lon, group=i),
                         data=paths, colour="red") +
                           geom_point(aes(y=lat, x=lon,
                                          group=i), data=paths,
                                      colour="blue")

  if (output.path != "-") {
    ggsave(gmap, file="out.png")
  }
  else
  {
    print(gmap)
  }

}

widen.box <- function(box, factor=5) {
  diff.lon <- box[['right']] - box[['left']]
  diff.lat <- box[['top']] - box[['bottom']]
  margin.lon <- diff.lon * factor
  margin.lat <- diff.lat * factor
  return(c(left=(box[['left']] - margin.lon/2),
           top=(box[['top']] + margin.lat/2),
           right=(box[['right']] + margin.lon/2),
           bottom=(box[['bottom']] - margin.lat/2)))
  
}

args <- commandArgs(trailingOnly = TRUE)

input.path <- args[[1]]
output.path <- args[[2]]

input.table <- read.table(input.path, header=TRUE)
graph.shape(input.table, output.path)