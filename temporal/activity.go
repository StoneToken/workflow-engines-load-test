package app

import (
	"fmt"
	"io"
	"log"
	"net/http"
)

// type User struct {
//     id int
//     username string
//     firstName string
//     lastName string
//     email string
//     password string
//     phone string
//     userStatus int
// }

func RestTask(name string) (string, error) {

	url := "http://localhost:8000/user/" + name
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
