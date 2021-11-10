package utils

import (
	"archive/zip"
	"bytes"
	"io/ioutil"
	"log"
)

// https://stackoverflow.com/questions/50539118/golang-unzip-response-body/50539327
// https://stackoverflow.com/a/50539327
func ReadZIP(body []byte) []byte {

	zipReader, err := zip.NewReader(bytes.NewReader(body), int64(len(body)))
	if err != nil {
		log.Fatal(err)
	}

	unzipped := make([]byte, 0)
	// Read all the files from zip archive
	for _, zipFile := range zipReader.File {
		unzippedFileBytes, err := readZipFile(zipFile)
		if err != nil {
			log.Println(err)
			continue
		}
		unzipped = unzippedFileBytes

	}
	return unzipped
}

func readZipFile(zf *zip.File) ([]byte, error) {
	f, err := zf.Open()
	if err != nil {
		return nil, err
	}
	defer f.Close()
	return ioutil.ReadAll(f)
}
