package utils

// https://stackoverflow.com/questions/66643946/how-to-remove-duplicates-strings-or-int-from-slice-in-go
// https://stackoverflow.com/a/66751055

func RemoveDuplicateStrValues(strSlice []string) []string {
	allKeys := make(map[string]bool)
	list := []string{}
	for _, item := range strSlice {
			if _, value := allKeys[item]; !value {
					allKeys[item] = true
					list = append(list, item)
			}
	}
	return list
}