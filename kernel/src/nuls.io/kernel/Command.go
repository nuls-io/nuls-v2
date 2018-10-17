package kernel

import "encoding/json"

type Command struct {
	JsonRPC string           `json:"jsonrpc"`
	Method  string           `json:"method"`
	Params  *json.RawMessage `json:"params"`
	ID      uint64            `json:"id"`
}
