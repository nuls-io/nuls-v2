package rpc

import (
	"github.com/gorilla/rpc/v2"
	"github.com/gorilla/rpc/v2/json"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
)

type RpcService struct {
	upgrader websocket.Upgrader
	s        *rpc.Server
}

func (self *RpcService) StartService() {
	self.s = rpc.NewServer()
	self.s.RegisterService(Say, "account_info")
	self.s.RegisterCodec(json.NewCodec(), "application/json")

	self.upgrader.CheckOrigin = func(r *http.Request) bool {
		return true
	}

	http.HandleFunc("/", self.handleReq)
	go http.ListenAndServe("127.0.0.1:9999", nil)
}

func (self *RpcService) RegisterCommand(name string, receiver interface{}) {

}

func (self *RpcService) handleReq(w http.ResponseWriter, r *http.Request) {
	ws, err := self.upgrader.Upgrade(w, r, nil)
	if err != nil {
		self.s.ServeHTTP(w, r)
		return
	}

	self.handleWS(ws)
}

func (self *RpcService) handleWS(ws *websocket.Conn) {
	defer ws.Close()
	log.Println("WS connect from:", ws.RemoteAddr().String())
	for {

		mt, message, err := ws.ReadMessage()
		if err != nil {
			log.Println("Read err:", err)
			break
		}
		log.Println(mt, string(message))
	}
}

type HelloArgs struct {
	Account     string `json:"account"`
	Strict      string `json:strict`
	LedgerIndex string `json:ledger_index`
}

type HelloReply struct {
	Message string
}

type HelloService struct{}

func Say(r *http.Request, args *HelloArgs, reply *HelloReply) error {
	reply.Message = "Hello, " + args.Account + "!"
	return nil
}
