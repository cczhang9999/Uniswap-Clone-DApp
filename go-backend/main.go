package main

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// Result standard response wrapper
type Result struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data"`
}

func Success(data interface{}) Result {
	return Result{
		Code: 200,
		Msg:  "success",
		Data: data,
	}
}

// Models
type OrderRequest struct {
	UserId   string  `json:"userId"`
	Symbol   string  `json:"symbol"`
	Side     string  `json:"side"`
	Type     string  `json:"type"`
	Price    float64 `json:"price"`
	Quantity float64 `json:"quantity"`
}

type Order struct {
	UserId    string  `json:"userId"`
	OrderId   string  `json:"orderId"`
	Symbol    string  `json:"symbol"`
	Side      string  `json:"side"`
	Type      string  `json:"type"`
	Price     float64 `json:"price"`
	Quantity  float64 `json:"quantity"`
	Timestamp int64   `json:"timestamp"`
}

type Trade struct {
	TradeId   string  `json:"tradeId"`
	Symbol    string  `json:"symbol"`
	Price     float64 `json:"price"`
	Quantity  float64 `json:"quantity"`
	Timestamp int64   `json:"timestamp"`
}

type OrderBookDTO struct {
	Bids []Order `json:"bids"`
	Asks []Order `json:"asks"`
}

type Wallet struct {
	UserId   string             `json:"userId"`
	Balances map[string]float64 `json:"balances"`
}

type DepositRequest struct {
	UserId   string  `json:"userId"`
	Currency string  `json:"currency"`
	Amount   float64 `json:"amount"`
}

// Mock Data Store
var (
	mockOrderBook = OrderBookDTO{
		Bids: []Order{},
		Asks: []Order{},
	}
	mockWallets = make(map[string]*Wallet)
)

func main() {
	r := gin.Default()

	// CORS middleware
	r.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "http://localhost:3000")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	v1 := r.Group("/api/v1")
	{
		v1.POST("/order", placeOrder)
		v1.GET("/order/book/:symbol", getOrderBook)
		v1.GET("/balance/:userId", getBalance)
		v1.POST("/balance/deposit", deposit)
	}

	r.Run(":8080") // listen and serve on 0.0.0.0:8080
}

func placeOrder(c *gin.Context) {
	var req OrderRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	order := Order{
		UserId:    req.UserId,
		OrderId:   uuid.New().String(),
		Symbol:    req.Symbol,
		Side:      req.Side,
		Type:      req.Type,
		Price:     req.Price,
		Quantity:  req.Quantity,
		Timestamp: time.Now().UnixMilli(),
	}

	// Mock processing: just add to order book for demo
	if req.Side == "BUY" {
		mockOrderBook.Bids = append(mockOrderBook.Bids, order)
	} else {
		mockOrderBook.Asks = append(mockOrderBook.Asks, order)
	}

	// Return empty trades list as mock result
	c.JSON(http.StatusOK, Success([]Trade{}))
}

func getOrderBook(c *gin.Context) {
	// symbol := c.Param("symbol")
	// In a real app, filter by symbol. Here we return the global mock book.
	c.JSON(http.StatusOK, Success(mockOrderBook))
}

func getBalance(c *gin.Context) {
	userId := c.Param("userId")
	wallet, exists := mockWallets[userId]
	if !exists {
		wallet = &Wallet{
			UserId:   userId,
			Balances: make(map[string]float64),
		}
		mockWallets[userId] = wallet
	}
	c.JSON(http.StatusOK, Success(wallet))
}

func deposit(c *gin.Context) {
	var req DepositRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	wallet, exists := mockWallets[req.UserId]
	if !exists {
		wallet = &Wallet{
			UserId:   req.UserId,
			Balances: make(map[string]float64),
		}
		mockWallets[req.UserId] = wallet
	}

	wallet.Balances[req.Currency] += req.Amount
	c.JSON(http.StatusOK, Success("Deposit successful"))
}
