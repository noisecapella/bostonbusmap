library(ggmap)

data <- data.frame(lon=c(-71.25, -71.20), lat=c(42, 41.95), id=c(1,1))
box <- c(-71.49096,  41.58168, -71.05480,  42.35370)
map <- get_map(location=box)
ggmap(map) + geom_path(aes(x=lon, y=lat
