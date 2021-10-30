package utils

import (
  "encoding/json"
)

// https://stackoverflow.com/questions/22128282/how-to-check-string-is-in-json-format
func IsJSON(str string) bool {
  var js json.RawMessage
  return json.Unmarshal([]byte(str), &js) == nil
}
