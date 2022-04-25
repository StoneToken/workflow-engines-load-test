package app

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
)

func RestTask(name string) (string, error) {

	// Default value "http://localhost:8000/user/"
	url := os.Getenv("TEMPORAL_API_URL")  + name
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		log.Fatalln(err)
	}

	req.Header.Set("Accept", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Fatalln(err)
	}

	defer resp.Body.Close()

	b, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Fatalln(err)
	}

	fmt.Println(string(b))

	return string(b), nil
}
