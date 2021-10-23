package cmds

import (
	// "fmt"
	// "os"
	// "strings"
	// "github.com/Ziloka/ProxyBroker/services"
	"github.com/urfave/cli/v2"
)

func Grab(*cli.Context) (err error) {
	// proxies := services.Collect()
	// // Display first 10 unchecked proxies
	// for _, proxy := range proxies[:10] {
	// 	fmt.Println("[+] "+ proxy)
	// }

	// if true {
	// 	data := []byte(strings.Join(proxies, "\n"))
	// 	f, fileCreateErr := os.Create("proxies.txt")
	// 	if fileCreateErr != nil {
	// 		panic(fileCreateErr)
	// 	}
	// 	fileWriteErr := os.WriteFile("proxies.txt", data, 0644)
	// 	if fileWriteErr != nil {
	// 		panic(fileWriteErr)
	// 	}
	// 	defer f.Close()

	// }
	return nil
}