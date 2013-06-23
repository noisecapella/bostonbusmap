library(hash)
library(rjson)

# borrowed from http://www.r-bloggers.com/great-circle-distance-calculations-in-r/
gcd.slc <- function(lat.lon1, lat.lon2) {
  lat1 <- lat.lon1[[1]] * (pi/180)
  long1 <- lat.lon1[[2]] * (pi/180)
  lat2 <- lat.lon2[[1]] * (pi/180)
  long2 <- lat.lon2[[2]] * (pi/180)
  
  R <- 6371 # Earth mean radius [km]
  d <- acos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2) * cos(long2-long1)) * R
  return(d) # Distance in km
}

calculate.shapes <- function(cr.dir) {
  cr.files <- dir(cr.dir, pattern="^RailLine")
  cr.paths <- mapply(function(file) { file.path(cr.dir, file) },
                     cr.files)
  cr.details <- file.info(cr.paths)
  cr.paths.with.times <- cr.details[with(cr.details,
                                         order(as.POSIXct(ctime))), ]

  trips.to.ignore <- hash()
  
  # for each file, for each trip, add lat, lon to vector
  f <- function(cr.path, output) {
    document <- tryCatch(fromJSON(file=cr.path),
                         error=function(e) { print(e) })
    messages <- document[["Messages"]]
    message.f <- function(msg) {
      trip <- msg[["Trip"]]
      if (!is.null(trips.to.ignore[[trip]])) {
        return()
      }
      lat.string <- msg[["Latitude"]]
      lon.string <- msg[["Longitude"]]
      if (trip != "" && lat.string != "" && lon.string != "") {
        lat.lon <- c(as.numeric(lat.string),
                     as.numeric(lon.string))
        if (is.null(output[[trip]])) {
          output[[trip]] <- lat.lon
        }
        else {
          old.trip <- output[[trip]]
          old.lat.lon <- tail(old.trip, 2)
          distance <- gcd.slc(old.lat.lon, lat.lon)
          if (all(old.lat.lon == lat.lon))
          {
            # skip
          }
          else if (distance > 5) {
            # trip has bad data, ignore
            trips.to.ignore[[trip]] <- TRUE
            output[[trip]] <- NULL
            print(paste("Trip data is not precise enough:", distance))
          }
          else  {
            output[[trip]] <- append(old.trip, lat.lon)
          }
        }
      }
    }
    lapply(messages, message.f)
  }

  h <- hash()
  lapply(rownames(cr.paths.with.times), f, output=h)

  return(h)
}

