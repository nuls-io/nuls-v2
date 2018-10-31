package rpc

import (
	"github.com/gorilla/websocket"
	"log"
	"net/http"
)

type RpcService struct {
	upgrader websocket.Upgrader
}

func (self *RpcService) StartService() {

	self.upgrader.CheckOrigin = func(r *http.Request) bool {
		return true
	}

	http.HandleFunc("/", self.handleReq)
	go http.ListenAndServe("127.0.0.1:9999", nil)
}

func (self *RpcService) handleReq(w http.ResponseWriter, r *http.Request) {
	ws, err := self.upgrader.Upgrade(w, r, nil)
	if err != nil {
		self.handleRPC(w, r)
		return
	}

	self.handleWS(ws)
}

func (self *RpcService) handleRPC(w http.ResponseWriter, r *http.Request) {

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
