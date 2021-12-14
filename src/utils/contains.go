package utils

func StringContains(arr []string, str string) bool {
  for _, a := range arr {
    if a == str {
      return true
    }
  }
  return false
}

func IntContains(arr []int, str int) bool {
  for _, a := range arr {
    if a == str {
      return true
    }
  }
  return false
}