library(argparse)
source("draw_from_csv_lib.R")
main <- function() {
  parser <- ArgumentParser()
  parser$add_argument("csv.filename")
  args <- parser$parse_args()

  draw.from.csv(args$csv.filename)
}
main()
