# API Testing Guide

This document outlines scenarios for manually testing the Finora backend endpoints using [Swagger UI](http://localhost:8080/swagger-ui.html) or an HTTP client like Postman or cURL.

---

## 1. Investments (`/api/investments`)

### Scenario 1.1: Create a new Investment
- **Method**: `POST /api/investments`
- **Description**: Add a new stock, bond, or cash asset to the portfolio.
- **Input (JSON)**:
  ```json
  {
    "symbol": "AAPL",
    "name": "Apple Inc.",
    "type": "STOCK",
    "currency": "USD",
    "metadata": {
      "sector": "Technology"
    }
  }
  ```
- **Expected Output (201 Created)**: Returns the newly created investment with a generated `id` (UUID), `createdAt`, and `updatedAt`. Save the `id` for subsequent requests.

### Scenario 1.2: Retrieve all Investments
- **Method**: `GET /api/investments`
- **Description**: Fetch all investments in the portfolio.
- **Input**: None
- **Expected Output (200 OK)**: A JSON array of all investment objects.

### Scenario 1.3: Retrieve a specific Investment
- **Method**: `GET /api/investments/{id}`
- **Description**: Fetch details for a specific investment.
- **Input**: `{id}` in the path.
- **Expected Output (200 OK)**: The JSON representation of the requested investment.
- **Error Output (404 Not Found)**: If the UUID does not exist in the database.

### Scenario 1.4: Update an Investment
- **Method**: `PUT /api/investments/{id}`
- **Description**: Modify the details (like name or metadata) of an existing investment.
- **Input (JSON)**:
  ```json
  {
    "name": "Apple Incorporated",
    "metadata": {
      "sector": "Technology",
      "dividend_yield": "0.5%"
    }
  }
  ```
- **Expected Output (200 OK)**: Returns the updated investment object with a modified `updatedAt` timestamp.

### Scenario 1.5: Delete an Investment
- **Method**: `DELETE /api/investments/{id}`
- **Description**: Remove an investment from the portfolio.
- **Input**: `{id}` in the path.
- **Expected Output (204 No Content)**: Returns nothing on success. (Note: In a real environment, you must delete associated transactions first due to foreign key constraints).

### Scenario 1.6: Prevent creating invalid Investment
- **Method**: `POST /api/investments`
- **Input (JSON)**:
  ```json
  {
    "symbol": "",
    "type": "INVALID_TYPE",
    "currency": "US"
  }
  ```
- **Expected Output (400 Bad Request)**: Spring Validation triggers errors because `symbol` is empty, `type` isn't in the Enum, and `currency` isn't exactly 3 characters.

---

## 2. Transactions (`/api/investments/{id}/transactions`)

### Scenario 2.1: Add a BUY Transaction
- **Method**: `POST /api/investments/{investmentId}/transactions`
- **Description**: Record purchasing shares for an investment.
- **Input (JSON)**:
  ```json
  {
    "type": "BUY",
    "quantity": 10.5,
    "price": 150.00,
    "currency": "USD",
    "txnDate": "2023-10-01"
  }
  ```
- **Expected Output (201 Created)**: Returns the saved transaction. The system automatically converts standard JSON numbers into strictly scaled BigDecimals.

### Scenario 2.2: Add a SELL Transaction
- **Method**: `POST /api/investments/{investmentId}/transactions`
- **Description**: Record selling a portion of shares.
- **Input (JSON)**:
  ```json
  {
    "type": "SELL",
    "quantity": 5.0,
    "price": 160.00,
    "currency": "USD",
    "txnDate": "2023-10-15"
  }
  ```
- **Expected Output (201 Created)**: Returns the saved transaction. This affects cost-basis calculations later.

### Scenario 2.3: List Transactions chronologically
- **Method**: `GET /api/investments/{investmentId}/transactions`
- **Input**: None
- **Expected Output (200 OK)**: A JSON array containing all transactions for this investment, strictly ordered by `txnDate` ascending.

### Scenario 2.4: Delete a Transaction
- **Method**: `DELETE /api/investments/{investmentId}/transactions/{id}`
- **Description**: Remove a specific transaction record.
- **Input**: Both `{investmentId}` and `{id}` (transaction ID) in the path.
- **Expected Output (204 No Content)**: Returns nothing on successful deletion.

---

## 3. Dividends (`/api/investments/{id}/dividends`)

### Scenario 3.1: Simulate a Cash (Distributive) Dividend
- **Method**: `POST /api/investments/{investmentId}/dividends/simulate`
- **Description**: See how much cash a dividend would yield based on current holdings.
- **Input (JSON)**:
  ```json
  {
    "dividendPerShare": 1.50,
    "mode": "DISTRIBUTIVE"
  }
  ```
- **Expected Output (200 OK)**: Assuming you bought 10.5 shares and sold 5 (leaving 5.5 shares), the output shows `totalCashGenerated: 8.25` and `newSharesAcquired: 0`.

### Scenario 3.2: Simulate an Accumulative Dividend (DRIP)
- **Method**: `POST /api/investments/{investmentId}/dividends/simulate`
- **Description**: See how many fractional shares you would acquire if the dividend is reinvested.
- **Input (JSON)**:
  ```json
  {
    "dividendPerShare": 1.50,
    "mode": "ACCUMULATIVE",
    "reinvestmentPrice": 150.00
  }
  ```
- **Expected Output (200 OK)**: Will show `totalCashGenerated: 0` and `newSharesAcquired: 0.055` (8.25 / 150).

---

## 4. Dashboard (`/api/dashboard`)

### Scenario 4.1: Retrieve Portfolio Summary
- **Method**: `GET /api/dashboard/summary`
- **Description**: Aggregates the total portfolio value using real-time prices and Cost Basis.
- **Input**: Optional Query Param `homeCurrency=USD`
- **Expected Output (200 OK)**:
  ```json
  {
    "totalValue": 880.00,
    "totalCostBasis": 825.00,
    "totalRealisedPnl": 50.00,
    "totalUnrealisedPnl": 55.00
  }
  ```

### Scenario 4.2: Retrieve Portfolio Allocation
- **Method**: `GET /api/dashboard/allocation`
- **Description**: Groups asset allocations by investment type.
- **Input**: Optional Query Param `homeCurrency=USD`
- **Expected Output (200 OK)**: Array of asset classes (`STOCK`, `BOND`, etc.) with their aggregated `totalValue` and percentage of the total portfolio.

### Scenario 4.3: Retrieve Portfolio Trend
- **Method**: `GET /api/dashboard/trend`
- **Description**: Fetches historical portfolio value over a rolling window.
- **Input**: Optional Query Params `homeCurrency=USD` and `days=30`
- **Expected Output (200 OK)**: An array containing daily portfolio valuation over the specified number of days.

---

## 5. Chatbot Engine (`/api/chat`)

### Scenario 5.1: General Assistance
- **Method**: `POST /api/chat`
- **Description**: Send a prompt to the LLM.
- **Input (JSON)**:
  ```json
  {
    "message": "Explain what a cost basis is in investing."
  }
  ```
- **Expected Output (200 OK)**: 
  ```json
  {
    "response": "Cost basis represents the original value of an asset..."
  }
  ```

### Scenario 5.2: Tool Triggering
- **Method**: `POST /api/chat`
- **Description**: Ask a question that forces the LLM to use a registered `PortfolioTool`.
- **Input (JSON)**:
  ```json
  {
    "message": "What should I focus on today?"
  }
  ```
- **Expected Output (200 OK)**: The backend intercepts the LLM's function call, runs `GetTodayFocusTool`, and feeds the data back to the LLM. You'll receive a localized answer like:
  ```json
  {
    "response": "Based on my data, you should focus on reviewing your tech sector allocation today."
  }
  ```
