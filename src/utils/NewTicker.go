package utils

import (
  "time"
)

// Inital Answer
// https://stackoverflow.com/questions/16466320/is-there-a-way-to-do-repetitive-tasks-at-intervals
// https://stackoverflow.com/a/16466581

// Actual Answer
// https://stackoverflow.com/questions/32705582/how-to-get-time-tick-to-tick-immediately
// https://stackoverflow.com/a/32707343

func NewTicker(delay, repeat time.Duration) *time.Ticker {
  ticker := time.NewTicker(repeat)
  oc := ticker.C
  nc := make(chan time.Time, 1)
  go func() {
      nc <- time.Now()
      for tm := range oc {
          nc <- tm
      }
  }()
  ticker.C = nc
  return ticker
}