library(argparse)
source('make_commuter_rail_shapes_lib.R')

main <- function() {
  parser <- ArgumentParser()
  parser$add_argument("cr.dir")
  args <- parser$parse_args()

  shapes <- calculate.shapes(args$cr.dir)
  json.data <- toJSON(as.list(shapes))
  write(json.data, file="shapes_out.json")
}

main()
