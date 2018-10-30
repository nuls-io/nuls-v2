package kernel

import "github.com/gorilla/websocket"

type WSManagement struct {
	wsServer websocket.Upgrader

}