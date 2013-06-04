library(hash)
library(rjson)

calculate.shapes <- function(cr.dir) {
  cr.files <- dir(cr.dir, pattern="^RailLine")
  cr.paths <- mapply(function(file) { file.path(args$cr.dir, file) },
                     cr.files)
  cr.details <- file.info(cr.paths)
  cr.paths.with.times <- cr.details[with(cr.details,
                                         order(as.POSIXct(ctime))), ]
  
  # for each file, for each trip, add lat, lon to vector
  f <- function(cr.path, output) {
    inner.f <- function() {
      document <- fromJSON(file=cr.path)
      messages <- document[["Messages"]]
      message.f <- function(msg) {
        trip <- msg[["Trip"]]
        lat.string <- msg[["Latitude"]]
        lon.string <- msg[["Longitude"]]
        if (trip != "" && lat.string != "" && lon.string != "") {
          lat.lon <- c(as.numeric(lat.string),
                       as.numeric(lon.string))
          if (is.null(output[[trip]])) {
            output[[trip]] <- lat.lon
          }
          else {
            old.trip = output[[trip]]
            old.lat.lon = tail(old.trip, 2)
            if (old.lat.lon != lat.lon) {
              output[[trip]] <- append(old.trip, lat.lon)
            }
          }
        }
      }
      lapply(messages, message.f)
    }
    tryCatch(inner.f(), error=function(e) {
      print(e)
    })
  }

  h <- hash()
  lapply(rownames(cr.paths.with.times), f, output=h)

  return(h)
}

