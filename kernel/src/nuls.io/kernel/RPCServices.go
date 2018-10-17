package kernel

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

type RPCServices struct {
	http.Handler
}

func (self *RPCServices) StartRPC() {
	go http.ListenAndServe("localhost:8080", self)
}

func (self *RPCServices) ServeHTTP(writer http.ResponseWriter, request *http.Request) {
	log.Printf("%s %s", request.Method, request.RequestURI)

	switch strings.ToUpper(request.Method) {
	case "GET":
		self.handleBadReq(writer, request)
	case "POST":
		requestBody, err := ioutil.ReadAll(request.Body)
		if err != nil {
			self.handleBadReq(writer, request)
			return
		}
		cmd := &Command{}
		err = json.Unmarshal(requestBody, cmd)
		if err != nil {
			self.handleBadReq(writer, request)
			return
		}
		self.handleCMD(cmd, writer, request)
	}
}

func (self *RPCServices) handleCMD(cmd *Command, writer http.ResponseWriter, request *http.Request) {

}

func (self *RPCServices) handleBadReq(writer http.ResponseWriter, request *http.Request) {
	resp := `{
  "jsonrpc": "1.0",
  "id": null,
  "error": {
    "code": 3,
    "data": {
      "code": 102,
      "message": "Innsufficient gas"
    }
  }
}`
	writer.Write([]byte(resp))
}
