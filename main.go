// https://medium.com/analytics-vidhya/golang-command-line-git-parser-2f4e85ac8fc6
// https://gist.github.com/Lebonesco/c9f334b517ce7bb5484ae7cb2045c314#file-main-go
package main

import (
	"log"
	"os"
	"time"
	"fmt"
	"bytes"
	"io"
	"os/exec"
	"encoding/json"
	"encoding/xml"
	"path/filepath"
	"github.com/mitchellh/go-homedir"
	"github.com/urfave/cli"
)

func main() {
	app := cli.NewApp()
	info(app)
	flags(app)
	commands(app)

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

// add meta info to cli
func info(app *cli.App) {
	app.Name = "Daily Standup Helper CLI"
	app.Usage = "Reports git history"
	app.Author = "github.com/Lebonesco"
	app.Version = "1.0.0"
}

// add flags to cli
func flags(app *cli.App) {
	dir, _ := homedir.Dir()

	app.Flags = []cli.Flag{
		cli.StringFlag{
			Name:  "user, u",
			Value: "",
			Usage: "git user name",
		},
		cli.StringFlag{
			Name:  "dir, d",
			Value: dir,
			Usage: "parent directory to start recursively searching for *.git files",
		},
		cli.StringFlag{
			Name:  "after, a",
			Value: time.Now().Add(-24 * time.Hour).Format("2006-01-02T15:04:05"),
			Usage: "when to start looking at commit history",
		},
	}
}

// add command to cli
func commands(app *cli.App) {
	app.Action = func(c *cli.Context) error {
		dir := c.String("dir")
		after := c.String("after")

		user := c.String("user")
		if len(user) == 0 {
			return fmt.Errorf("no 'user' flag value provided")
		}

		err := runClient(dir, user, after)
		if err != nil {
			return err
		}
		return nil
	}
}

func runClient(dir, user, after string) error {
	commits, err := getGitHistory(dir, user, after)
	if err != nil {
		return err
	}

	f, err := os.Create("standup.json")
	if err != nil {
		return err
	}

	prettyJSON, err := json.MarshalIndent(commits, "", "  ")
	if err != nil {
		return err
	}

	_, err = f.Write(prettyJSON)
	if err != nil {
		return err
	}
	return nil
}

func getGitHistory(dir, user, after string) ([]commit, error) {
	var commits []commit
	err := filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		if info.Name() == ".git" {
			b, err := getCommits(path, user, after)
			if err != nil {
				return err
			}

			if len(b) == 0 {
				return nil
			}

			d := xml.NewDecoder(bytes.NewBuffer(b))
			for {
				var c commit
				err := d.Decode(&c)
				if err != nil {
					if err == io.EOF {
						break
					}
					return err
				}

				commits = append(commits, c)
			}

		}

		return nil
	})

	return commits, err
}

type commit struct {
	Author  string `xml:"author"`
	Date    string `xml:"date"`
	Message string `xml:"message"`
}

func getCommits(path, user, after string) ([]byte, error) {
	format := `
			<entry>
				<author>%an</author>
				<date>%cd</date>
				<message>%B</message>
			</entry>`
	cmd := exec.Command("git", "log", "--author="+user, "--pretty=format:"+format, "--after="+after)
	cmd.Dir = path
	out, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	return out, nil
}